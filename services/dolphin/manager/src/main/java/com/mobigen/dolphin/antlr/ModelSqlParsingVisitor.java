package com.mobigen.dolphin.antlr;

import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.exception.ErrorCode;
import com.mobigen.dolphin.exception.SqlParseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
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
    private final char SPECIAL_CHAR = '"';
    private final DolphinConfiguration dolphinConfiguration;
    private final Map<String, String> models = new HashMap<>();
    private JSONObject jsonTree;
    private final QueryTreeJsonSerializer serializer = new QueryTreeJsonSerializer();

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
        jsonTree = serializer.serialize(ctx);
        return visitSql_stmt(ctx.sql_stmt()) + ";";
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
        var selectCore_ = ctx.select_core().stream()
                .map(this::visitSelect_core)
                .collect(Collectors.joining(" union "));
        var orderBy_ = visitOrder_by_(ctx.order_by_());
        var limit_ = visitLimit_(ctx.limit_());
        return selectCore_ + orderBy_ + limit_;
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
        String result;
        if (ctx.model_name() != null) {  // 심플 모델명
            String catalogName = dolphinConfiguration.getModel().getCatalog();
            if (ctx.catalog_name() != null) {
                catalogName = ctx.catalog_name().getText();
            }
            catalogName = convertKeywordName(catalogName);
            String schemaName = dolphinConfiguration.getModel().getSchema();
            if (ctx.schema_name() != null) {
                schemaName = ctx.schema_name().getText();
            }
            schemaName = convertKeywordName(schemaName);
            var modelName = convertKeywordName(ctx.model_name().getText());
            log.info("catalog : " + catalogName + " schema : " + schemaName + " modelName : " + modelName);
            // TODO modelName 을 이용해 모델의 실제 데이터 소스 가져 오기
            models.put(ctx.toString(), catalogName + "." + schemaName + "." + modelName);
            result = catalogName + "." + schemaName + "." + modelName;
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
