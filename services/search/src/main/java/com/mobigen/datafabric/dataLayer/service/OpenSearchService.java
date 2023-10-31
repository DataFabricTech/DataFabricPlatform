package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.config.OpenSearchConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import lombok.Getter;
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
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.*;

@Deprecated
@Slf4j
@Getter
public class OpenSearchService {
//    private final OpenSearchRepository openSearchRepository;
//    private final DBConfig dbConfig;
//    private final OpenSearchConfig openSearchConfig;
//
//    public OpenSearchService(OpenSearchRepository openSearchRepository, DBConfig dbConfig, OpenSearchConfig openSearchConfig) {
//        this.openSearchRepository = openSearchRepository;
//        this.dbConfig = dbConfig;
//        this.openSearchConfig = openSearchConfig;
//    }
//
//    /**
//     * @param input        full text search value
//     * @param detailSearch detail search values
//     * @param filterSearch filter search values
//     * @param userId       for save recent searches
//     * @return data model ids
//     * @throws OpenSearchException
//     * @throws IOException
//     */
//    public List<String> search(String input, DataSet detailSearch, Filter filterSearch, String userId)
//            throws OpenSearchException, IOException {
//        insertDocuemnt(input, userId); // for recentSearch
//        return openSearchRepository.search(
//                dataSetMainQueryBuilder(input),
//                detailSearch == null ? null : mustQueryBuilder(detailSearch),
//                filterSearch == null ? null : shouldQueryBuilder(filterSearch)
//        );
//    }
//
//    /**
//     * @param input Storage name
//     * @return Storage's ids
//     */
//    public List<String> search(String input) throws IOException, OpenSearchException {
//        var query = storageMainQueryBuilder(input);
//        return openSearchRepository.search(query);
//    }
//
//    public List<String> search() throws IOException, OpenSearchException {
//        return openSearchRepository.search();
//    }
//
//    public Map<String, Aggregate> getFacet(String input, DataSet detailSearch, Filter filterSearch)
//            throws IOException, OpenSearchException {
//        return openSearchRepository.getFacet(
//                dataSetMainQueryBuilder(input),
//                detailSearch == null ? null : mustQueryBuilder(detailSearch),
//                filterSearch == null ? null : shouldQueryBuilder(filterSearch));
//    }
//
//    public void insertDocument(Insert insertStatement) throws OpenSearchException, IOException {
//        openSearchRepository.insertDocument(queryToModel(insertStatement));
//    }
//
//    /**
//     * @param input  Recent search
//     * @param userId Search user's id
//     *               <p>
//     *               for recent searches document
//     */
//    public void insertDocuemnt(String input, String userId) throws OpenSearchException, IOException {
//        var recentSearch = openSearchRepository.searchRecent(userId);
//
//        if (recentSearch == null) {
//            var arr = new LinkedList<String>();
//            arr.add(input);
//            var recentSearchModel = new RecentSearchesModel();
//            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));
//
//            recentSearchModel.setUserId(userId);
//
//            openSearchRepository.insertDocument(recentSearchModel);
//        } else {
//            var docId = recentSearch.id();
//
//            var arr = recentSearch.source().getRecentSearches() == null ?
//                    new LinkedList<String>() :
//                    new LinkedList<String>(List.of(recentSearch.source().getRecentSearches()));
//
//            if (arr.size() < openSearchConfig.getNumberOfSaveRecentSearches()) {
//                arr.addLast(input);
//            } else {
//                arr.pollFirst();
//                arr.addFirst(input);
//            }
//
//            var recentSearchModel = recentSearch.source();
//            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));
//
//            openSearchRepository.updateDocument(recentSearchModel, docId);
//        }
//    }
//
//    /**
//     * @param input
//     * @return
//     */
//    public List<Query> dataSetMainQueryBuilder(String input) {
//        var shouldQuery = new LinkedList<Query>();
//        for (var field : DataCatalogModel.class.getDeclaredFields()) {
//            if (field.getType() == String.class || field.getType() == String[].class)
//                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
//        }
//
//        for (var field : DataCatalogModel.Meta.class.getDeclaredFields())
//            shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
//
//        return shouldQuery;
//    }
//
//    public List<Query> storageMainQueryBuilder(String input) {
//        var shouldQuery = new LinkedList<Query>();
//        for (var field : StorageModel.class.getDeclaredFields()) {
//            if (field.getType() == String.class || field.getType() == String[].class)
//                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
//        }
//
//        return shouldQuery;
//    }
//
//    public List<Query> mustQueryBuilder(DataSet detailSearch) {
//        var mustQuery = new ArrayList<Query>();
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
//        return mustQuery;
//    }
//
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
//
//    public String[] getRecentSearch(String userId) {
//        try {
//            return openSearchRepository.getResentSearches(userId);
//        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
//            return null;
//        }
//    }
//
//    public void createIndex() throws OpenSearchException, IOException {
//        openSearchRepository.createIndex();
//    }
//
//    public void searchDocument(String[] dataSetIds) {
//        // TODO getDocument to RDBMS
//    }
//
//
//    // todo datamodel
//    public void updateDocument(Update updateStatement)
//            throws OpenSearchException, IOException, IndexOutOfBoundsException {
//        log.info("[updateDocument] start");
//        var id = ((EqualsTo) updateStatement.getWhere()).getRightExpression().toString();
//        openSearchRepository.updateDocument(queryToModel(updateStatement), id);
//    }
//
//    public void deleteDocument(Delete deleteStatement) throws OpenSearchException, IOException {
//        log.info("[deleteDocument] start");
//        var id = ((EqualsTo) deleteStatement.getWhere()).getRightExpression().toString();
//        openSearchRepository.deleteDocument(id);
//    }
//
//    public void SyncDocument() {
//    }
//
//    private DataCatalogModel queryToModel(Insert insertStatement) {
//        var columns = insertStatement.getColumns();
//        var values = insertStatement.getValues().getExpressions();
//        var dataSetModel = new DataCatalogModel();
//
//        setDataSetModel(dataSetModel, columns, values);
//
//        return dataSetModel;
//    }
//
//    /**
//     * @param updateStatement
//     * @return dataSetModel -> updateDocument
//     * String -> target Document's id
//     */
//    private DataCatalogModel queryToModel(Update updateStatement) throws OpenSearchException, IOException, IndexOutOfBoundsException {
//        log.info("[queryToModel] start");
//
//        var dataSetModel = openSearchRepository.searchId(updateStatement.getWhere().toString()).source();
//
//        for (var set : updateStatement.getUpdateSets()) {
//            var columns = set.getColumns();
//            var values = set.getValues();
//
//            setDataSetModel(dataSetModel, columns, values);
//        }
//
//        return dataSetModel;
//    }
//
//
//    private void setDataSetModel(DataCatalogModel dataCatalogModel,
//                                 ExpressionList<Column> columns, ExpressionList<?> values) {
//        // todo 여기 나중에 setting 필요
//        for (int i = 0; i < values.size(); i++) {
//            // todo meta, tags, createdAt
//            switch (columns.get(i).toString()) {
//                case "id":
//                    dataCatalogModel.setId(values.get(i).toString());
//                    break;
//                case "name":
//                    dataCatalogModel.setName(values.get(i).toString());
//                    break;
//                case "description":
//                    dataCatalogModel.setDescription(values.get(i).toString());
//                    break;
//                case "type":
//                    dataCatalogModel.setDataType(values.get(i).toString());
//                    break;
//                case "format":
//                    dataCatalogModel.setDataFormat(values.get(i).toString());
//                    break;
//                case "knowledgeGraph":
//                    dataCatalogModel.setKnowledgeGraph(values.get(i).toString());
//                    break;
//                case "status":
//                    dataCatalogModel.setStatus(Boolean.parseBoolean(values.get(i).toString()));
//                    break;
////                case "categories":
////                    openSearchModel.setCategories(values.get(i).toString());
////                    break;
////                case "tags":
////                    openSearchModel.setTags(values.get(i).toString());
////                    break;
//                case "connectorType":
//                    dataCatalogModel.setConnectorType(values.get(i).toString());
//                    break;
//                case "connectorName":
//                    dataCatalogModel.setConnectorName(values.get(i).toString());
//                    break;
//                case "creatorId":
//                    dataCatalogModel.setCreatorId(values.get(i).toString());
//                    break;
//                case "creatorName":
//                    dataCatalogModel.setCreatorName(values.get(i).toString());
//                    break;
////                case "createdAt":
////                    openSearchModel.setCreatedAt(values.get(i).toString());
////                    break;
//                default:
//                    break;
//            }
//        }
//    }
//
//    public void executeBatchUpdate(String[] sqls)
//            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
//        for (var sql : sqls) {
//            executeUpdate(sql);
//        }
//    }
//
//    public void executeUpdate(String sql)
//            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
//        log.info("[executeUpdate] start");
//        var dataSetTableName = dbConfig.getDataCatalog();
//        var storageTableName = dbConfig.getStorage();
//        Statement statement = null;
//        try {
//            statement = CCJSqlParserUtil.parse(sql);
//        } catch (JSQLParserException e) {
//            log.error(e.getMessage());
//        }
//
//        if (statement instanceof Insert insertStatement) {
//            if (insertStatement.getTable().getName().equals(dataSetTableName) ||
//                    insertStatement.getTable().getName().equals(storageTableName)) {
//                insertDocument(insertStatement);
//            }
//        } else if (statement instanceof Delete deleteStatement) {
//            if (deleteStatement.getTable().getName().equals(dataSetTableName) ||
//                    deleteStatement.getTable().getName().equals(storageTableName)) {
//                deleteDocument(deleteStatement);
//            }
//        } else if (statement instanceof Update updateStatement) {
//            if (updateStatement.getTable().getName().equals(dataSetTableName) ||
//                    updateStatement.getTable().getName().equals(storageTableName)) {
//                updateDocument(updateStatement);
//            }
//        }
//    }
}
