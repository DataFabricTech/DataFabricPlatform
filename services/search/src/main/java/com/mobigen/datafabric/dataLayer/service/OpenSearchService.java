package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.model.DataSetModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.libs.configuration.Config;
import com.mobigen.libs.grpc.DataModel;
import com.mobigen.libs.grpc.Filter;
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
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.*;

@Slf4j
@Getter
public class OpenSearchService {
    private final OpenSearchRepository openSearchRepository;
    private final DBConfig dbConfig;

    public OpenSearchService(OpenSearchRepository openSearchRepository, DBConfig dbConfig) {
        this.openSearchRepository = openSearchRepository;
        this.dbConfig = dbConfig;
    }

    /**
     * @param input        full text search value
     * @param detailSearch detail search values
     * @param filterSearch filter search values
     * @param userId       for save recent searches
     * @return data model ids
     * @throws OpenSearchException
     * @throws IOException
     */
    public List<String> search(String input, DataModel detailSearch, Filter filterSearch, String userId)
            throws OpenSearchException, IOException {
        List<Query> mustQuery = new ArrayList<>();
        List<Query> shouldQuery = new ArrayList<>();
        shouldQuery = mainQueryBuilder(input);

        // TODO detailSearch가 null일 때 이게 체크 되는지에 대한 Test필요
        if (detailSearch != null)
            mustQuery = mustQueryBuilder(detailSearch);

        if (filterSearch != null)
            shouldQuery = shouldQueryBuilder(shouldQuery, filterSearch);

        var searchResposne = new LinkedList<String>();

        insertDocuemnt(input, userId); // for recentSearch

        searchResposne = openSearchRepository.search(mustQuery, shouldQuery);

        return searchResposne;
    }

    /**
     * @param input Storage name
     * @return Storage's ids
     */
    public List<String> search(String input) throws IOException, OpenSearchException {
        var query = mainQueryBuilder(input);
        return openSearchRepository.search(query);
    }

    public List<String> search() throws IOException, OpenSearchException {
        return openSearchRepository.search();
    }

    public void insertDocument(Insert insertStatement) throws OpenSearchException, IOException {
        openSearchRepository.insertDocument(queryToModel(insertStatement));
    }

    /**
     * @param input  Recent search
     * @param userId Search user's id
     *               <p>
     *               for recent searches document
     */
    public void insertDocuemnt(String input, String userId) throws OpenSearchException, IOException {
        var recentSearch = openSearchRepository.searchRecent(userId);
        // todo -> if null -> error가 뜨나 안뜨나 확인할 것

        if (recentSearch == null) {
            var arr = new LinkedList<String>();
            arr.add(input);
            var recentSearchModel = new RecentSearchesModel();
            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));

            recentSearchModel.setUserId(userId);

            openSearchRepository.insertDocument(recentSearchModel);
        } else {
            var docId = recentSearch.id();

            var arr = recentSearch.source().getRecentSearches() == null ?
                    new LinkedList<String>() :
                    new LinkedList<String>(List.of(recentSearch.source().getRecentSearches()));

            if (arr.size() < new Config().getConfig().getInt("open_search.recent_count")) {
                arr.addLast(input);
            } else {
                arr.pollFirst();
                arr.addFirst(input);
            }

            var recentSearchModel = recentSearch.source();
            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));

            openSearchRepository.updateDocument(recentSearchModel, docId);
        }
    }

    /**
     *
     * @param input
     * @return
     */
    public List<Query> mainQueryBuilder(String input) {
        var shouldQuery = new LinkedList<Query>();
        for (var field : DataSetModel.class.getDeclaredFields()) {
            if (field.getType() == String.class || field.getType() == String[].class)
                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
        }

        for (var field : DataSetModel.Meta.class.getDeclaredFields())
            shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());

        return shouldQuery;
    }

    public List<Query> mustQueryBuilder(DataModel detailSearch) {
        var mustQuery = new ArrayList<Query>();
        for (var field : detailSearch.getDescriptorForType().getFields()) {
            if (!detailSearch.getField(field).toString().isEmpty()) {
                var name = field.getName();
                var fieldValue = detailSearch.getField(field);
                switch (fieldValue.getClass().getSimpleName()) { // TODO meta 관련 검색이 되는지에 대한 확인 필요
                    case "EmptyList":
                        break;
                    case "Time": // TODO
//                        var time = new SimpleDateFormat(((Time) fieldValue).getFormat()).format(((Time) fieldValue).getTime());
//                        var timeFilter = ((Time) fieldValue).getTimeOperator();
//                        switch (timeFilter) {
//                            case ">" -> sb.append(" AND ").append(name).append(":{").append(time).append(" TO *}");
//                            case "<" -> sb.append(" AND ").append(name).append(":{* TO").append(time).append("}");
//                            case ">=" -> sb.append(" AND ").append(name).append(":[").append(time).append(" TO *]");
//                            case "<=" -> sb.append(" AND ").append(name).append(":[* TO").append(time).append("]");
//                            default -> // == or =
//                                    sb.append(" AND ").append(name).append(time);
//                        }
                        break;
                    case "UnmodifiableRandomAccessList":
                        for (var value : (List) fieldValue)
                            mustQuery.add(new MatchQuery.Builder().field(name)
                                    .query(FieldValue.of(value.toString())).build()._toQuery());
                        break;
                    default:
                        mustQuery.add(new MatchQuery.Builder().field(name)
                                .query(FieldValue.of(fieldValue.toString())).build()._toQuery());
                        break;
                }
            }
        }
        return mustQuery;
    }

    public List<Query> shouldQueryBuilder(List<Query> shouldQuery, Filter filterSearch) {
        for (var field : filterSearch.getDescriptorForType().getFields()) {
            if (!filterSearch.getField(field).toString().isEmpty()) {
                var name = field.getName(); // todo name으로 해도 되는건가?
                var fieldValue = filterSearch.getField(field);

                for (var value : (List<String>) fieldValue) { // TODO meta 관련 Test진행
                    shouldQuery.add(new MatchQuery.Builder().field(name)
                            .query(FieldValue.of(value)).build()._toQuery());
                }
            }
        }
        return shouldQuery;
    }

    public String[] getRecentSearch(String userId) {
        try {
            return openSearchRepository.getResentSearches(userId);
        } catch (OpenSearchException | IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void createIndex() throws OpenSearchException, IOException {
        openSearchRepository.createIndex();
    }

    public void searchDocument(String[] dataModelIds) {
        // TODO getDocument to RDBMS
    }


    // todo datamodel
    public void updateDocument(Update updateStatement)
            throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[updateDocument] start");
        var id = ((EqualsTo) updateStatement.getWhere()).getRightExpression().toString();
        openSearchRepository.updateDocument(queryToModel(updateStatement), id);
    }

    public void deleteDocument(Delete deleteStatement) throws OpenSearchException, IOException {
        var id = ((EqualsTo) deleteStatement.getWhere()).getRightExpression().toString();
        openSearchRepository.deleteDocument(id);
    }

    public void SyncDocument() {
    }

    public DataSetModel queryToModel(Insert insertStatement) {
        var columns = insertStatement.getColumns();
        var values = insertStatement.getValues().getExpressions();
        var dataSetModel = new DataSetModel();

        setDataSetModel(dataSetModel, columns, values);

        return dataSetModel;
    }

    /**
     * @param updateStatement
     * @return dataSetModel -> updateDocument
     * String -> target Document's id
     */
    public DataSetModel queryToModel(Update updateStatement) throws OpenSearchException, IOException, IndexOutOfBoundsException {
        log.info("[queryToModel] start");

        var dataSetModel = openSearchRepository.searchId(updateStatement.getWhere().toString()).source();

        for (var set : updateStatement.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();

            setDataSetModel(dataSetModel, columns, values);
        }

        return dataSetModel;
    }


    private void setDataSetModel(DataSetModel dataSetModel,
                                 ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            // todo meta, tags, createdAt
            switch (columns.get(i).toString()) {
                case "id":
                    dataSetModel.setId(values.get(i).toString());
                    break;
                case "name":
                    dataSetModel.setName(values.get(i).toString());
                    break;
                case "description":
                    dataSetModel.setDescription(values.get(i).toString());
                    break;
                case "type":
                    dataSetModel.setType(values.get(i).toString());
                    break;
                case "format":
                    dataSetModel.setFormat(values.get(i).toString());
                    break;
                case "knowledgeGraph":
                    dataSetModel.setKnowledgeGraph(values.get(i).toString());
                    break;
                case "status":
                    dataSetModel.setStatus(Boolean.parseBoolean(values.get(i).toString()));
                    break;
//                case "categories":
//                    openSearchModel.setCategories(values.get(i).toString());
//                    break;
//                case "tags":
//                    openSearchModel.setTags(values.get(i).toString());
//                    break;
                case "connectorType":
                    dataSetModel.setConnectorType(values.get(i).toString());
                    break;
                case "connectorName":
                    dataSetModel.setConnectorName(values.get(i).toString());
                    break;
                case "creatorId":
                    dataSetModel.setCreatorId(values.get(i).toString());
                    break;
                case "creatorName":
                    dataSetModel.setCreatorName(values.get(i).toString());
                    break;
//                case "createdAt":
//                    openSearchModel.setCreatedAt(values.get(i).toString());
//                    break;
                default:
                    break;
            }
        }
    }

    public void executeBatchUpdate(String[] sqls)
            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
        for (var sql : sqls) {
            executeUpdate(sql);
        }
    }

    public void executeUpdate(String sql)
            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
        log.info("[executeUpdate] start");
        var dataSetTableName = dbConfig.getDataSet();
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
                insertDocument(insertStatement);
            }
        } else if (statement instanceof Delete deleteStatement) {
            if (deleteStatement.getTable().getName().equals(dataSetTableName) ||
                    deleteStatement.getTable().getName().equals(storageTableName)) {
                deleteDocument(deleteStatement);
            }
        } else if (statement instanceof Update updateStatement) {
            if (updateStatement.getTable().getName().equals(dataSetTableName) ||
                    updateStatement.getTable().getName().equals(storageTableName)) {
                updateDocument(updateStatement);
            }
        }
    }
}
