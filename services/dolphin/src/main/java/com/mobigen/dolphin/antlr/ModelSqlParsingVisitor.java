package com.mobigen.dolphin.antlr;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.util.Functions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ModelSqlParsingVisitor extends ModelSqlBaseVisitor<String> {
    private final OpenMetadataRepository openMetadataRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final List<ExecuteDto.ReferenceModel> referenceModels;

    private final char SPECIAL_CHAR = '"';
    private final Map<String, String> models = new HashMap<>();

    @Override
    public String visitErrorNode(ErrorNode node) {
        throw new SqlParseException(ErrorCode.INVALID_SQL, "ERROR: error node : " + node);
    }

    @Override
    public String visit(ParseTree tree) {
        return super.visit(tree);
    }

    @Override
    public String visitParse(ModelSqlParser.ParseContext ctx) {
        return visitSql_stmt(ctx.sql_stmt());
    }

    @Override
    public String visitSql_stmt(ModelSqlParser.Sql_stmtContext ctx) {
        String explain = ctx.K_EXPLAIN() == null ? "" : ctx.K_EXPLAIN().getText();
        String query = ctx.K_QUERY() == null ? "" : " " + ctx.K_QUERY().getText();
        String plan = ctx.K_PLAN() == null ? "" : " " + ctx.K_PLAN().getText();

        return explain + query + plan + visitSelect_stmt(ctx.select_stmt());
    }

    @Override
    public String visitSelect_stmt(ModelSqlParser.Select_stmtContext ctx) {
        var selectCoreBuilder = new StringBuilder(visitSelect_core(ctx.select_core(0)));
        for (var i = 0; i < ctx.compound_operator().size(); i++) {
            selectCoreBuilder.append(" ").append(visitCompound_operator(ctx.compound_operator(i))).append(" ")
                    .append(visitSelect_core(ctx.select_core(i + 1)));
        }
        var selectCore_ = selectCoreBuilder.toString();
        var orderBy_ = visitOrder_by_(ctx.order_by_());
        var limit_ = visitLimit_(ctx.limit_());
        return selectCore_ + orderBy_ + limit_;
    }

    @Override
    public String visitCompound_operator(ModelSqlParser.Compound_operatorContext ctx) {
        String operator;
        if (ctx.K_UNION() != null) {
            operator = ctx.K_UNION().getText();
            if (ctx.K_ALL() != null) {
                operator = operator + " " + ctx.K_ALL().getText();
            }
        } else if (ctx.K_INTERSECT() != null) {
            operator = ctx.K_INTERSECT().getText();
        } else {
            operator = ctx.K_EXCEPT().getText();
        }
        return operator;
    }

    @Override
    public String visitSelect_core(ModelSqlParser.Select_coreContext ctx) {
        // visit 순서 제어
        String select = visitSelect_(ctx.select_());
        String from_ = visitFrom_(ctx.from_());
        String where_ = visitWhere_(ctx.where_());
        String groupBy_ = visitGroup_by_(ctx.group_by_());
        return select + from_ + where_ + groupBy_;
    }

    @Override
    public String visitSelect_(ModelSqlParser.Select_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " select " + ctx.result_column().stream().map(this::visitResult_column).collect(Collectors.joining(","));
    }

    @Override
    public String visitFrom_(ModelSqlParser.From_Context ctx) {
        if (ctx == null) {
            return "";
        } else if (ctx.table_or_subquery() != null) {
            return " from " + visitTable_or_subquery(ctx.table_or_subquery());
        } else if (ctx.join_clause() != null) {
            return " from " + visitJoin_clause(ctx.join_clause());
        }
        throw new SqlParseException(ErrorCode.INVALID_SQL, "ERROR: error rule : " + ModelSqlParser.ruleNames[ctx.getRuleIndex()] + ", parts: " + ctx.getText());
    }

    @Override
    public String visitWhere_(ModelSqlParser.Where_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " where " + ctx.expr().getText();
    }

    @Override
    public String visitGroup_by_(ModelSqlParser.Group_by_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " group by " + ctx.expr().stream().map(RuleContext::getText).collect(Collectors.joining(", "));
    }

    @Override
    public String visitOrder_by_(ModelSqlParser.Order_by_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " order by " + ctx.ordering_term().stream().map(RuleContext::getText).collect(Collectors.joining(", "));
    }

    @Override
    public String visitLimit_(ModelSqlParser.Limit_Context ctx) {
        if (ctx == null) {
            return "";
        }
        return " limit " + ctx.INTEGER_LITERAL().stream().map(ParseTree::getText).collect(Collectors.joining(", "));
    }

    @Override
    public String visitJoin_clause(ModelSqlParser.Join_clauseContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitJoin_operator(ModelSqlParser.Join_operatorContext ctx) {
        return ctx.children.stream()
                .map(x -> x.accept(this))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String visitResult_column(ModelSqlParser.Result_columnContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitTable_or_subquery(ModelSqlParser.Table_or_subqueryContext ctx) {
        String result = null;
        if (ctx.model_name() != null) {  // 심플 모델명
            if (ctx.schema_name() != null && ctx.catalog_name() == null) {
                throw new SqlParseException(ErrorCode.INVALID_SQL, "schema 를 사용한 경우 catalog 를 반드시 설정 해야 합니다.");
            }
            for (var referenceModel : referenceModels) {
                if (ctx.model_name().getText().equalsIgnoreCase(referenceModel.getName())) {
                    var tableInfo = referenceModel.getId() == null ?
                            openMetadataRepository.getTable(referenceModel.getFullyQualifiedName()) :
                            openMetadataRepository.getTable(referenceModel.getId());
                    var catalogName = Functions.getCatalogName(tableInfo.getService().getId());
                    String schemaName;
                    if ("postgres".equalsIgnoreCase(tableInfo.getServiceType())) {
                        schemaName = tableInfo.getDatabaseSchema().getName();
                    } else {
                        schemaName = tableInfo.getDatabase().getName();
                    }
                    result = catalogName + "." + schemaName + "." + tableInfo.getName();
                    break;
                }
            }
            if (result == null) {
                var catalogName = visitCatalog_name(ctx.catalog_name());
                var schemaName = visitSchema_name(ctx.schema_name());
                var modelName = dolphinConfiguration.getModel().convertKeywordName(ctx.model_name().getText());
                log.info("catalog : {} schema : {} modelName : {}", catalogName, schemaName, modelName);
                models.put(ctx.toString(), catalogName + "." + schemaName + "." + modelName);
                result = catalogName + "." + schemaName + "." + modelName;
            }
        } else if (ctx.select_stmt() != null) {
            result = "(" + visitSelect_stmt(ctx.select_stmt()) + ")";
        } else {
            result = "(" + visitJoin_clause(ctx.join_clause()) + ")";
        }
        if (ctx.table_alias() != null) {
            result = result + " as " + convertKeywordName(ctx.table_alias().getText());
        }
        return result;
    }

    @Override
    public String visitCatalog_name(ModelSqlParser.Catalog_nameContext ctx) {
        if (ctx == null) {
            return dolphinConfiguration.getModel().getCatalog();
        }
        return visitAny_name(ctx.any_name());
    }

    @Override
    public String visitSchema_name(ModelSqlParser.Schema_nameContext ctx) {
        if (ctx == null) {
            return dolphinConfiguration.getModel().getSchema();
        }
        return visitAny_name(ctx.any_name());
    }

    @Override
    public String visitAny_name(ModelSqlParser.Any_nameContext ctx) {
        return dolphinConfiguration.getModel().convertKeywordName(ctx.getText());
    }

    private String convertKeywordName(String name) {
        if (name.startsWith("`")) {
            name = SPECIAL_CHAR + name.substring(1, name.length() - 1) + SPECIAL_CHAR;
        } else if (Arrays.asList(((VocabularyImpl) ModelSqlParser.VOCABULARY).getSymbolicNames())
                .contains("K_" + name.toUpperCase())) {
            name = SPECIAL_CHAR + name + SPECIAL_CHAR;
        }
        return name;
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return node.getText();
    }

}
