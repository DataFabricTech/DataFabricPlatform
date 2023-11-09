package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.config.PortalConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.ResponseCode;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.share.protobuf.*;
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
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class PortalServiceImpl implements PortalCallBack {
    private final PortalRepository portalRepository;
    private final DataLayerRepository dataLayerRepository;
    private final DBConfig dbConfig;
    private final PortalConfig portalConfig;

    public PortalServiceImpl(PortalRepository portalRepository, DataLayerRepository dataLayerRepository,
                             DBConfig dbConfig, PortalConfig portalConfig) {
        this.portalRepository = portalRepository;
        this.dataLayerRepository = dataLayerRepository;
        this.dbConfig = dbConfig;
        this.portalConfig = portalConfig;
    }

    @Override
    public ResSearch search(ReqSearch request) {
        try {
            var input = request.getKeyword();
            var details = request.getDetailSearchMap();
            var filters = request.getFilterSearchMap();
            var pageable = request.getPageable();
            var userId = "testUser"; // todo request.getUserId

            var resSearchBuilder = ResSearch.newBuilder();
            var searchResponseBuilder = SearchResponse.newBuilder();
            var searchContentBuilder = SearchContent.newBuilder();

            var totalSize = 0;

            totalSize += dataCatalogBuilder(searchContentBuilder, input, details, filters, pageable);
            totalSize += storageBuilder(searchContentBuilder, input, pageable);
            facetBuilder(searchResponseBuilder, input, details, filters);

            var currentSize = searchContentBuilder.getDataModelsCount() + searchContentBuilder.getStoragesCount();
            var resPageable = Utilities.Pageable.newBuilder()
                    .setPage(
                            Utilities.Page.newBuilder()
                                    .setSize(currentSize)
                                    .setSelectPage(pageable.getPage().getSelectPage())
                                    .setTotalPage(pageable.getPage().getSize() == 0 ?
                                            totalSize / portalConfig.getDefaultSearchSize() + totalSize % portalConfig.getDefaultSearchSize() != 0 ? 1 : 0 :
                                            totalSize / pageable.getPage().getSize() + totalSize % pageable.getPage().getSize() != 0 ? 1 : 0)
                                    .setTotalSize(totalSize)
                                    .build()
                    ).build();

            searchResponseBuilder
                    .setPageable(resPageable)
                    .putAllFilters(searchResponseBuilder.getFiltersMap())
                    .setContents(searchContentBuilder.build());

            resSearchBuilder.setCode(ResponseCode.SUCCESS.getValue()).setData(
                    ResSearch.Data.newBuilder().setSearchResponse(searchResponseBuilder.build())
            );

            // recentSearches
            saveRecent(input, userId);

            return resSearchBuilder.build();
        } catch (OpenSearchException | IOException | SQLException | ClassNotFoundException e) {
            return ResSearch.newBuilder()
                    .setCode(ResponseCode.UNKNOWN.getValue())
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public ResRecentSearches recentSearches(Empty request) {
        var userId = "testUser"; // todo request.getUserId
        try {
            var recentSearches = portalRepository.recentSearches(userId);
            return ResRecentSearches.newBuilder()
                    .setData(
                            ResRecentSearches.Data.newBuilder()
                                    .addAllRecentSearches(recentSearches == null || recentSearches.source() == null ?
                                            new ArrayList<>() :
                                            List.of(recentSearches.source().getRecentSearches()))
                                    .build()
                    ).build();
        } catch (OpenSearchException | IOException e) {
            return ResRecentSearches.newBuilder()
                    .setCode(ResponseCode.UNKNOWN.getValue())
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    private int dataCatalogBuilder(SearchContent.Builder builder, String input, Map<String, String> details
            , Map<String, ListString> filters, Utilities.Pageable pageable) throws OpenSearchException, IOException, SQLException, ClassNotFoundException {
        var searchModel = portalRepository.search(
                mainQueryBuilder(input),
                details.isEmpty() ? null : mustQueryBuilder(details),
                filters.isEmpty() ? null : shouldQueryBuilder(filters),
                pageable
        );

        var totalSize = searchModel.getTotalSize();

        for (var dataCatalogModel : searchModel.getDataCatalogModelList()) {
            builder.addDataModels(convertToCatalog(dataCatalogModel));
        }

        return totalSize;
    }

    private int storageBuilder(SearchContent.Builder builder, String input, Utilities.Pageable pageable)
            throws OpenSearchException, IOException, SQLException, ClassNotFoundException {
        var searchModel = portalRepository.search(storageMainQueryBuilder(input), pageable);

        var totalSize = searchModel.getTotalSize();

        for (var storageModel : searchModel.getStorageModelList()) {
            builder.addStorages(convertToStorage(storageModel));
        }

        return totalSize;
    }

    private void facetBuilder(SearchResponse.Builder builder, String input
            , Map<String, String> details, Map<String, ListString> filters) throws OpenSearchException, IOException {
        var facet = portalRepository.getFacet(
                mainQueryBuilder(input),
                details.isEmpty() ? null : mustQueryBuilder(details),
                filters.isEmpty() ? null : shouldQueryBuilder(filters));

        builder.putAllFilters(filterBuilder(facet));
    }

    private void saveRecent(String input, String userId) throws OpenSearchException, IOException {
        var recentSearches = portalRepository.recentSearches(userId);

        if (recentSearches == null) {
            var arr = new LinkedList<String>();
            arr.add(input);
            var recentSearchModel = new RecentSearchesModel();
            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));

            recentSearchModel.setUserId(userId);

            portalRepository.insertDocument(recentSearchModel);
        } else {
            var docId = recentSearches.id();

            var arr = recentSearches.source() != null && recentSearches.source().getRecentSearches() == null ?
                    new LinkedList<String>() :
                    new LinkedList<String>(List.of(recentSearches.source().getRecentSearches()));

            if (arr.size() < portalConfig.getNumberOfSaveRecentSearches()) {
                arr.addLast(input);
            } else {
                arr.pollFirst();
                arr.addFirst(input);
            }

            var recentSearchModel = recentSearches.source();
            recentSearchModel.setRecentSearches(arr.toArray(new String[0]));

            portalRepository.updateDocument(recentSearchModel, docId);
        }

    }


    public void executeUpdate(String sql) throws OpenSearchException, IOException {
        log.info("[executeUpdate] start");
        var dataSetTableName = dbConfig.getDataCatalog(); // todo dataCatalog schema 정의
        var storageTableNames = dbConfig.getStorage();
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
        }

        if (statement instanceof Insert insertStatement) {
            if (insertStatement.getTable().getName().equals(dataSetTableName))
                portalRepository.insertDocument(sqlToDataCatalogModel(insertStatement));
            else if (storageTableNames.contains(insertStatement.getTable().getName()))
                portalRepository.insertDocument(sqlToStorageModel(insertStatement));
        } else if (statement instanceof Delete deleteStatement) {
            if (deleteStatement.getTable().getName().equals(dataSetTableName))
                portalRepository.deleteDataCatalogDocument(getId(deleteStatement));
            else if (storageTableNames.contains(deleteStatement.getTable().getName()))
                portalRepository.deleteStorageDocument(getId(deleteStatement));
        } else if (statement instanceof Update updateStatement) {
            if (updateStatement.getTable().getName().equals(dataSetTableName))
                portalRepository.updateDocument(sqlToDataCatalogModel(updateStatement), getId(updateStatement));
            else if (storageTableNames.contains(updateStatement.getTable().getName()))
                portalRepository.updateDocument(sqlToStorageModel(updateStatement), getId(updateStatement));

        }
    }

    public void executeBatchUpdate(String[] sqls)
            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
        var dataCatalogTableNames = dbConfig.getDataCatalog();
        var storageTableNames = dbConfig.getStorage();
        var state = CCJSqlParserUtil.parse(sqls[0]);
        try {
            if (state instanceof Insert insertState) {
                var tableName = insertState.getTable().getName().toLowerCase();
                if (storageTableNames.contains(tableName)) {
                    var storageModel = new StorageModel();
                    for (var sql : sqls) {
                        insertState = (Insert) CCJSqlParserUtil.parse(sql);
                        sqlToBulkStorageModel(storageModel, insertState);
                    }

                    portalRepository.insertDocument(storageModel);
                } else if (dataCatalogTableNames.contains(tableName)) {
                    var catalogModel = new DataCatalogModel();
                    for (var sql : sqls) {
                        insertState = (Insert) CCJSqlParserUtil.parse(sql);
                        sqlToBulkCatalogModel(catalogModel, insertState);
                    }
                    portalRepository.insertDocument(catalogModel);
                }
            } else if (state instanceof Update updateState) {
                var tableName = updateState.getTable().getName().toLowerCase();
                if (storageTableNames.contains(tableName)) {
                    var id = getId(updateState);
                    var storageModel = portalRepository.getStorageDocuemnt(id);

                    for (var sql : sqls) {
                        updateState = (Update) CCJSqlParserUtil.parse(sql);
                        sqlToBulkStorageModel(storageModel, updateState);
                    }

                    portalRepository.updateDocument(storageModel, id);
                } else if (dataCatalogTableNames.contains(tableName)) {
                    var id = getId(updateState);
                    var catalogModel = portalRepository.getDataCatalogDocuemnt(id);

                    for (var sql : sqls) {
                        updateState = (Update) CCJSqlParserUtil.parse(sql);
                        sqlToBulkCatalogModel(catalogModel, updateState);
                    }

                    portalRepository.updateDocument(catalogModel, id);
                }
            } else if (state instanceof Delete deleteState) {
                var tableName = deleteState.getTable().getName().toLowerCase();
                if (storageTableNames.contains(tableName)) {
                    portalRepository.deleteStorageDocument(getId(deleteState));
                } else if (dataCatalogTableNames.contains(tableName)) {
                    portalRepository.deleteDataCatalogDocument(getId(deleteState));
                }
            }
        } catch (JSQLParserException | IOException | OpenSearchException | IndexOutOfBoundsException |
                 NullPointerException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public List<String> searchAllStorage() throws IOException, OpenSearchException {
        return portalRepository.searchStorageAll();
    }

    public List<String> searchAllDataCatalog() throws IOException, OpenSearchException {
        return portalRepository.searchDataCatalogAll();
    }

    private DataCatalogModel sqlToDataCatalogModel(Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();

        var dataCatalogModel = new DataCatalogModel();
        setDataCatalog(dataCatalogModel, columns, values);

        return dataCatalogModel;
    }

    private void sqlToBulkCatalogModel(DataCatalogModel dataCatalogModel, Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();

        setDataCatalog(dataCatalogModel, columns, values);
    }

    private void sqlToBulkCatalogModel(DataCatalogModel dataCatalogModel, Update update) {
        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();

            setDataCatalog(dataCatalogModel, columns, values);
        }
    }


    private StorageModel sqlToStorageModel(Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();
        var tableName = insert.getTable().getName().toLowerCase();

        var storageModel = new StorageModel();
        setStorage(storageModel, tableName, columns, values);

        return storageModel;
    }

    private void sqlToBulkStorageModel(StorageModel storageModel, Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();
        var tableName = insert.getTable().getName().toLowerCase();

        setStorage(storageModel, tableName, columns, values);
    }

    private void sqlToBulkStorageModel(StorageModel storageModel, Update update) {
        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();
            var tableName = update.getTable().getName();

            setStorage(storageModel, tableName, columns, values);
        }
    }


    private DataCatalogModel sqlToDataCatalogModel(Update update) throws OpenSearchException, IOException, IndexOutOfBoundsException {
        var dataCatalogModel = portalRepository.getDataCatalogDocuemnt(getId(update));

        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();

            setDataCatalog(dataCatalogModel, columns, values);
        }

        return dataCatalogModel;
    }

    private StorageModel sqlToStorageModel(Update update) throws OpenSearchException, IOException, IndexOutOfBoundsException {
        var storageModel = portalRepository.getStorageDocuemnt(getId(update));

        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();
            var tableName = update.getTable().getName();

            setStorage(storageModel, tableName, columns, values);
        }

        return storageModel;
    }

    private String getId(Delete delete) {
        return ((EqualsTo) delete.getWhere()).getRightExpression().toString();
    }

    private String getId(Insert insert) {
        var columns = insert.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equals("id"))
                return insert.getValues().getExpressions().get(i).toString();
        }
        return "";
    }

    private String getId(Update update) {
        return ((EqualsTo) update.getWhere()).getRightExpression().toString();
    }

    private void setDataCatalog(DataCatalogModel dataCatalogModel
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            // todo meta, tags, createdAt
            switch (columns.get(i).toString().toLowerCase()) {
                case "id" ->
                        dataCatalogModel.setId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "name" ->
                        dataCatalogModel.setName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "description" ->
                        dataCatalogModel.setDescription(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "datatype" ->
                        dataCatalogModel.setDataType(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "dataformat" ->
                        dataCatalogModel.setDataFormat(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "knowledgegraph" ->
                        dataCatalogModel.setKnowledgeGraph(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "status" ->
                        dataCatalogModel.setStatus(Boolean.parseBoolean(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)));
                case "connectortype" ->
                        dataCatalogModel.setConnectorType(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "connectorname" ->
                        dataCatalogModel.setConnectorName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "creatorid" ->
                        dataCatalogModel.setCreatorId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "creatorname" ->
                        dataCatalogModel.setCreatorName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "categories", "tags", "createdat" -> System.out.println("TODO");
            }
        }
    }

    private void setStorage(StorageModel storageModel, String tableName
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            switch (columns.get(i).toString()) {
                case "id" -> {
                    if (tableName.equalsIgnoreCase("datastorage"))
                        storageModel.setId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "name" -> {
                    if (tableName.equalsIgnoreCase("datastorage"))
                        storageModel.setName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "user_desc" -> {
                    if (tableName.equalsIgnoreCase("datastorage"))
                        storageModel.setDesc(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "tag" -> {
                    List<String> tags = storageModel.getTag() != null ?
                            new ArrayList<>(List.of(storageModel.getTag())) :
                            new ArrayList<>();
                    tags.add(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                    storageModel.setTag(tags.toArray(new String[0]));
                }
                case "created_by" ->
                        storageModel.setCreatedBy(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "storage_type_name" ->
                        storageModel.setStorageTypeName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "updated_at" ->
                        storageModel.setUpdatedAt(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "status" ->
                        storageModel.setStatus(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
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

    // todo 나중에 maaping 다시 해야할 것이다.
    public List<Query> mustQueryBuilder(Map<String, String> details) {
        var mustQuery = new ArrayList<Query>();

        for (var key : details.keySet()) {
            switch (key) {
                case "DATA_NAME" -> mustQuery.add(new MatchQuery.Builder()
                        .field("name")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "DATA_TYPE" -> mustQuery.add(new MatchQuery.Builder()
                        .field("dataType")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "DATA_FORMAT" -> mustQuery.add(new MatchQuery.Builder()
                        .field("dataFormat")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "CATEGORY", "TAG", "START_DATE", "END_DATE" -> {
                    /**
                     * START_DATE
                     * var from = LocalDate.parse(details.get(key)).atStartOfDay(ZoneOffset.UTC).toInstant();
                     * mustQuery.add(new RangeQuery.Builder()
                     * .field("cretedAt").from(JsonData.of(from.toEpochMilli())).build()._toQuery());
                     *
                     * END_DATE
                     * var to = LocalDate.parse(details.get(key)).atStartOfDay(ZoneOffset.UTC).toInstant();
                     * mustQuery.add(new RangeQuery.Builder()
                     * .field("cretedAt").to(JsonData.of(to.toEpochMilli())).build()._toQuery());
                     */
                    System.out.println("TODO");
                }
                case "STORAGE_TYPE" -> mustQuery.add(new MatchQuery.Builder()
                        .field("storage_type")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "CONNECTOR_NAME" -> mustQuery.add(new MatchQuery.Builder()
                        .field("connector_name")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "CREATOR" -> mustQuery.add(new MatchQuery.Builder()
                        .field("creator")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
            }
        }

        return mustQuery;
    }

    private DataModelOuterClass.DataModel convertToCatalog(DataCatalogModel dataCatalogModel) {
        var dataModelBuilder = DataModelOuterClass.DataModel.newBuilder();
        for (var field : dataCatalogModel.getClass().getDeclaredFields()) {
            switch (field.getName().toLowerCase()) {
                case "id" -> dataModelBuilder.setId(dataCatalogModel.getId());
                case "name" -> dataModelBuilder.setName(dataCatalogModel.getName());
                case "description" -> dataModelBuilder.setDescription(dataCatalogModel.getDescription());
                case "datatype" -> dataModelBuilder.setDataType(dataCatalogModel.getDataType());
                case "dataformat" -> dataModelBuilder.setDataFormat(dataCatalogModel.getDataFormat());
                // todo 추후에 확정된 schema로 mapping 필요
            }
        }
        return dataModelBuilder.build();
    }

    private StorageOuterClass.Storage convertToStorage(StorageModel storageModel) {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        for (var field : storageModel.getClass().getDeclaredFields()) {
            switch (field.getName().toLowerCase()) {
                case "id" -> storageBuilder.setId(storageModel.getId());
                case "name" -> storageBuilder.setName(storageModel.getName());
                case "user_desc" -> storageBuilder.setDescription(storageModel.getDesc());
                case "tag" -> {
                    storageBuilder.addAllTags(Arrays.asList(storageModel.getTag()));
                }
                case "created_by" -> storageBuilder.setCreatedBy(
                        UserOuterClass.User.newBuilder()
                                .setName(storageModel.getCreatedBy())
                                .build());
                case "storage_type_name" -> storageBuilder.setStorageType(storageModel.getStorageTypeName());
                case "updated_at" -> storageBuilder.setLastModifiedAt(
                        Utilities.DateTime.newBuilder()
                                .setStrDateTime(storageModel.getUpdatedAt())
                                .build());
//                case "status" -> storageBuilder.setStatus(storageModel.getStatus()); // todo
            }
        }
        return storageBuilder.build();
    }

    public void deleteSearchesDocument(String id) {
        try {
            portalRepository.deleteSearchesDocument(id);
        } catch (OpenSearchException | IOException e) {
            // todo
        }
    }

    private Map<String, ListMapStrNumber> filterBuilder(Map<String, Aggregate> aggregations) {
        var fields = portalConfig.getFilters();
        var filters = new HashMap<String, ListMapStrNumber>();

        for (var field : fields) {
            if (aggregations.get(field) != null) {
                var listMapStrNumberBuilder = ListMapStrNumber.newBuilder();
                var bucket = aggregations.get(field)._get()._toAggregate().sterms().buckets().array();
                bucket.forEach(b -> {
                    var mapStrNumber = ListMapStrNumber.MapStrNumber.newBuilder();
                    mapStrNumber.setValue(b.docCount());
                    mapStrNumber.setKey(b.key());
                    listMapStrNumberBuilder.addValue(mapStrNumber);
                });
                if (listMapStrNumberBuilder.getValueCount() != 0)
                    filters.put(field, listMapStrNumberBuilder.build());
            }
        }

        return filters;
    }

    public List<Query> shouldQueryBuilder(Map<String, ListString> filters) {
        List<Query> shouldQuery = new ArrayList<>();
        for (var key : filters.keySet()) {
            var newKey = "";
            switch (key) {
                case "DATA_NAME" -> newKey = "name";
                case "DATA_TYPE" -> newKey = "dataType";
                case "DATA_FORMAT" -> newKey = "dataFormat";
                case "CATEGORY", "TAG", "START_DATE", "END_DATE" -> {
                    /**
                     * START_DATE
                     * var from = LocalDate.parse(details.get(key)).atStartOfDay(ZoneOffset.UTC).toInstant();
                     * mustQuery.add(new RangeQuery.Builder()
                     * .field("cretedAt").from(JsonData.of(from.toEpochMilli())).build()._toQuery());
                     *
                     * END_DATE
                     * var to = LocalDate.parse(details.get(key)).atStartOfDay(ZoneOffset.UTC).toInstant();
                     * mustQuery.add(new RangeQuery.Builder()
                     * .field("cretedAt").to(JsonData.of(to.toEpochMilli())).build()._toQuery());
                     */
                    System.out.println("TODO");
                }
                case "STORAGE_TYPE" -> newKey = "storage_type";
                case "CONNECTOR_NAME" -> newKey = "connector_name";
                case "CREATOR" -> newKey = "creator";
            }
            var fieldValue = filters.get(key).getValueList();

            for (var value : fieldValue) {
                shouldQuery.add(new MatchQuery.Builder().field(newKey)
                        .query(FieldValue.of(value)).build()._toQuery());
            }
        }

        return shouldQuery;
    }

    public List<Query> storageMainQueryBuilder(String input) {
        var shouldQuery = new LinkedList<Query>();
        for (var field : StorageModel.class.getDeclaredFields()) {
            if (field.getType() == String.class || field.getType() == String[].class)
                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
        }

        return shouldQuery;
    }
}
