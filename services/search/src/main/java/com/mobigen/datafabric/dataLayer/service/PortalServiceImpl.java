package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.share.protobuf.Portal.*;
import com.mobigen.libs.grpc.PortalCallBack;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class PortalServiceImpl implements PortalCallBack {
    private final PortalRepository portalRepository;
    private final DBConfig dbConfig;

    public PortalServiceImpl(PortalRepository portalRepository, DBConfig dbConfig) {
        this.portalRepository = portalRepository;
        this.dbConfig = dbConfig;
    }

    @Override
    public ResSearch search(ReqSearch request) {
        var input = request.getKeyword();
        var details = request.getDetailSearchMap();
        var filters = request.getFilterSearchMap();
        var pageable = request.getPageable();
//        var userId = request.getUserId();

//        portalRepository.saveRecentSearches(input, userId); // todo
//
//        var dataCatalogIds = portalRepository.search(
//                mainQueryBuilder(input),
//                details.isEmpty() ? null : mustQueryBuilder(details),
//                filters.isEmpty() ? null : shouldQueryBuilder(filters)); // todo must, should
//
//        var storageIds = portalRepository.search(mainQueryBuilder(input));

        return null;
    }

    @Override
    public ResRecentSearches recentSearches(Empty request) {
        return null;
    }

    public void executeUpdate(String sql)
            throws OpenSearchException, IOException {
        log.info("[executeUpdate] start");
        var dataSetTableName = dbConfig.getDataCatalog();
        var storageTableName = dbConfig.getStorage();
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
        }

        if (statement instanceof Insert insertStatement) {
            if (insertStatement.getTable().getName().equals(dataSetTableName) ||
                    insertStatement.getTable().getName().equals(storageTableName)) {
                portalRepository.insertDocument(sqlToModel(insertStatement));
            }
        } else if (statement instanceof Delete deleteStatement) {
            if (deleteStatement.getTable().getName().equals(dataSetTableName) ||
                    deleteStatement.getTable().getName().equals(storageTableName)) {
                portalRepository.deleteDocument(getId(deleteStatement));
            }
        } else if (statement instanceof Update updateStatement) {
            if (updateStatement.getTable().getName().equals(dataSetTableName) ||
                    updateStatement.getTable().getName().equals(storageTableName)) {
                portalRepository.updateDocument(sqlToModel(updateStatement), getId(updateStatement));
            }
        }
    }

    public void executeBatchUpdate(String[] sqls)
            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
        for (var sql : sqls) {
            executeUpdate(sql);
        }
    }

    public List<String> searchAll() throws IOException, OpenSearchException {
        return portalRepository.searchAll();
    }


    private DataCatalogModel sqlToModel(Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();

        var dataCatalogModel = new DataCatalogModel();
        setDataCatalogModel(dataCatalogModel, columns, values);

        return dataCatalogModel;
    }

    private DataCatalogModel sqlToModel(Update update) throws OpenSearchException, IOException, IndexOutOfBoundsException {
        // todo getId(update)가 진짜로 id를 가져오는지 확인
        var dataCatalogModel = portalRepository.getDocuemnt(getId(update));

        for (var set: update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();

            setDataCatalogModel(dataCatalogModel, columns, values);
        }

        return dataCatalogModel;
    }

    private String getId(Delete delete) {
        return ((EqualsTo) delete.getWhere()).getRightExpression().toString();
    }

    private String getId(Update update) {
        return ((EqualsTo) update.getWhere()).getRightExpression().toString();
    }

    private void setDataCatalogModel(DataCatalogModel dataCatalogModel
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            // todo meta, tags, createdAt
            switch (columns.get(i).toString()) {
                case "id":
                    dataCatalogModel.setId(values.get(i).toString());
                    break;
                case "name":
                    dataCatalogModel.setName(values.get(i).toString());
                    break;
                case "description":
                    dataCatalogModel.setDescription(values.get(i).toString());
                    break;
                case "dataType":
                    dataCatalogModel.setDataType(values.get(i).toString());
                    break;
                case "dataFormat":
                    dataCatalogModel.setDataFormat(values.get(i).toString());
                    break;
                case "knowledgeGraph":
                    dataCatalogModel.setKnowledgeGraph(values.get(i).toString());
                    break;
                case "status":
                    dataCatalogModel.setStatus(Boolean.parseBoolean(values.get(i).toString()));
                    break;
//                case "categories":
//                    openSearchModel.setCategories(values.get(i).toString());
//                    break;
//                case "tags":
//                    openSearchModel.setTags(values.get(i).toString());
//                    break;
                case "connectorType":
                    dataCatalogModel.setConnectorType(values.get(i).toString());
                    break;
                case "connectorName":
                    dataCatalogModel.setConnectorName(values.get(i).toString());
                    break;
                case "creatorId":
                    dataCatalogModel.setCreatorId(values.get(i).toString());
                    break;
                case "creatorName":
                    dataCatalogModel.setCreatorName(values.get(i).toString());
                    break;
//                case "createdAt":
//                    openSearchModel.setCreatedAt(values.get(i).toString());
//                    break;
                default:
                    break;
            }
        }
    }

    public List<Query> mainQueryBuilder(String input) {
        var shouldQuery = new LinkedList<Query>();
        for (var field : DataCatalogModel.class.getDeclaredFields()) {
            if (field.getType() == String.class || field.getType() == String[].class)
                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
        }

        for (var field : DataCatalogModel.Meta.class.getDeclaredFields())
            shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());

        return shouldQuery;
    }

    public List<Query> mustQueryBuilder(Map<String, String> details) {
        var mustQuery = new ArrayList<Query>();
        for (var key : details.keySet()) {
//           mustQuery.add(new MatchQuery.Builder().field(key));
        }
//        mustQuery.add(new MatchQuery.Builder().field());
//
//        for (var field : detailSearch.getDescriptorForType().getFields()) {
//            if (!detailSearch.getField(field).toString().isEmpty()) {
//                var name = field.getName();
//                var fieldValue = detailSearch.getField(field);
//
//                switch (fieldValue.getClass().getSimpleName()) { // TODO meta 관련 검색이 되는지에 대한 확인 필요
//                    case "EmptyList":
//                        break;
//                    case "Time": // TODO
////                        var time = new SimpleDateFormat(((Time) fieldValue).getFormat()).format(((Time) fieldValue).getTime());
////                        var timeFilter = ((Time) fieldValue).getTimeOperator();
////                        switch (timeFilter) {
////                            case ">" -> sb.append(" AND ").append(name).append(":{").append(time).append(" TO *}");
////                            case "<" -> sb.append(" AND ").append(name).append(":{* TO").append(time).append("}");
////                            case ">=" -> sb.append(" AND ").append(name).append(":[").append(time).append(" TO *]");
////                            case "<=" -> sb.append(" AND ").append(name).append(":[* TO").append(time).append("]");
////                            default -> // == or =
////                                    sb.append(" AND ").append(name).append(time);
////                        }
//                        break;
//                    case "UnmodifiableRandomAccessList":
//                    case "LazyStringArrayList":
//                        for (var value : (List) fieldValue)
//                            mustQuery.add(new MatchQuery.Builder().field(name)
//                                    .query(FieldValue.of(value.toString())).build()._toQuery());
//                        break;
//                    default:
//                        mustQuery.add(new MatchQuery.Builder().field(name)
//                                .query(FieldValue.of(fieldValue.toString())).build()._toQuery());
//                        break;
//                }
//            }
//        }
        return mustQuery;
    }

//    public List<Query> shouldQueryBuilder(Filter filterSearch) {
//        List<Query> shouldQuery = new ArrayList<>();
//        for (var field : filterSearch.getDescriptorForType().getFields()) {
//            if (!filterSearch.getField(field).toString().isEmpty()) {
//                var name = field.getName(); // todo name으로 해도 되는건가?
//                var fieldValue = filterSearch.getField(field);
//
//                for (var value : (List<String>) fieldValue) { // TODO meta 관련 Test진행
//                    shouldQuery.add(new MatchQuery.Builder().field(name)
//                            .query(FieldValue.of(value)).build()._toQuery());
//                }
//            }
//        }
//        return shouldQuery;
//    }
}
