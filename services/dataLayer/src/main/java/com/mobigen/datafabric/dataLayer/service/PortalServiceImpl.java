package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.config.PortalConfig;
import com.mobigen.datafabric.dataLayer.model.DataModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.ResponseCode;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
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
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class PortalServiceImpl implements PortalCallBack {
    private final PortalRepository portalRepository;
    private final DBConfig dbConfig;
    private final PortalConfig portalConfig;

    public PortalServiceImpl(PortalRepository portalRepository, DBConfig dbConfig, PortalConfig portalConfig) {
        this.portalRepository = portalRepository;
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

            totalSize += dataModelBuilder(searchContentBuilder, input, details, filters, pageable);
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
        } catch (OpenSearchException | IOException | SQLException | ClassNotFoundException | ParseException e) {
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

    private int dataModelBuilder(SearchContent.Builder builder, String input, Map<String, String> details
            , Map<String, ListString> filters, Utilities.Pageable pageable) throws OpenSearchException, IOException, SQLException, ClassNotFoundException, ParseException {
        var searchModel = portalRepository.search(
                mainQueryBuilder(input),
                details.isEmpty() ? null : mustQueryBuilder(details),
                filters.isEmpty() ? null : shouldQueryBuilder(filters),
                pageable
        );

        var totalSize = searchModel.getTotalSize();

        for (var dataModel : searchModel.getDataModelList()) {
            builder.addDataModels(convertToModel(dataModel));
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
            , Map<String, String> details, Map<String, ListString> filters) throws OpenSearchException, IOException, ParseException {
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
        var dataModelTableNames = dbConfig.getDataModel();
        var storageTableNames = dbConfig.getStorage();
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.error(e.getMessage());
        }

        if (statement instanceof Insert insertStatement) {
            if (dataModelTableNames.contains(insertStatement.getTable().getName()))
                portalRepository.insertDocument(sqlToDataModelModel(insertStatement));
            else if (storageTableNames.contains(insertStatement.getTable().getName()))
                portalRepository.insertDocument(sqlToStorageModel(insertStatement));

        } else if (statement instanceof Delete deleteStatement) {
            if (dataModelTableNames.contains(deleteStatement.getTable().getName()))
                portalRepository.deleteDataModelDocument(getId(deleteStatement));
            else if (storageTableNames.contains(deleteStatement.getTable().getName()))
                portalRepository.deleteStorageDocument(getId(deleteStatement));

        } else if (statement instanceof Update updateStatement) {
            if (dataModelTableNames.contains(updateStatement.getTable().getName()))
                portalRepository.updateDocument(sqlToDataModelModel(updateStatement), getId(updateStatement));
            else if (storageTableNames.contains(updateStatement.getTable().getName()))
                portalRepository.updateDocument(sqlToStorageModel(updateStatement), getId(updateStatement));

        }
    }

    public void executeBatchUpdate(String[] sqls)
            throws JSQLParserException, IOException, OpenSearchException, IndexOutOfBoundsException {
        var dataModelTableNames = dbConfig.getDataModel();
        var storageTableNames = dbConfig.getStorage();
        var state = CCJSqlParserUtil.parse(sqls[0]);
        try {
            if (state instanceof Insert insertState) {
                var tableName = insertState.getTable().getName().toLowerCase();
                if (dataModelTableNames.contains(tableName)) {
                    var dataModel = new DataModel();
                    for (var sql : sqls) {
                        insertState = (Insert) CCJSqlParserUtil.parse(sql);
                        sqlToBulkDataModel(dataModel, insertState);
                    }

                    portalRepository.insertDocument(dataModel);
                } else if (storageTableNames.contains(tableName)) {
                    var storageModel = new StorageModel();
                    for (var sql : sqls) {
                        insertState = (Insert) CCJSqlParserUtil.parse(sql);
                        sqlToBulkStorageModel(storageModel, insertState);
                    }
                    portalRepository.insertDocument(storageModel);
                }
            } else if (state instanceof Update updateState) {
                var tableName = updateState.getTable().getName().toLowerCase();
                if (dataModelTableNames.contains(tableName)) {
                    var id = getId(updateState);
                    var dataModel = portalRepository.getDataModelDocument(id);

                    for (var sql : sqls) {
                        updateState = (Update) CCJSqlParserUtil.parse(sql);
                        sqlToBulkDataModel(dataModel, updateState);
                    }

                    portalRepository.updateDocument(dataModel, id);
                } else if (storageTableNames.contains(tableName)) {
                    var id = getId(updateState);
                    var storageModel = portalRepository.getStorageDocuemnt(id);

                    for (var sql : sqls) {
                        updateState = (Update) CCJSqlParserUtil.parse(sql);
                        sqlToBulkStorageModel(storageModel, updateState);
                    }

                    portalRepository.updateDocument(storageModel, id);
                }
            } else if (state instanceof Delete deleteState) {
                var tableName = deleteState.getTable().getName().toLowerCase();
                if (dataModelTableNames.contains(tableName)) {
                    portalRepository.deleteDataModelDocument(getId(deleteState));
                } else if (storageTableNames.contains(tableName)) {
                    portalRepository.deleteStorageDocument(getId(deleteState));
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

    public List<String> searchAllDataModel() throws IOException, OpenSearchException {
        return portalRepository.searchDataModelAll();
    }

    private DataModel sqlToDataModelModel(Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();
        var tableName = insert.getTable().getName().toLowerCase();

        var dataModel = new DataModel();
        setDataModel(dataModel, tableName, columns, values);

        return dataModel;
    }

    private void sqlToBulkDataModel(DataModel dataModel, Insert insert) {
        var columns = insert.getColumns();
        var values = insert.getValues().getExpressions();
        var tableName = insert.getTable().getName().toLowerCase();

        setDataModel(dataModel, tableName, columns, values);
    }

    private void sqlToBulkDataModel(DataModel dataModel, Update update) {
        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();
            var tableName = update.getTable().getName().toLowerCase();

            setDataModel(dataModel, tableName, columns, values);
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


    private DataModel sqlToDataModelModel(Update update) throws
            OpenSearchException, IOException, IndexOutOfBoundsException {
        var dataModel = portalRepository.getDataModelDocument(getId(update));

        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();
            var tableName = update.getTable().getName();

            setDataModel(dataModel, tableName, columns, values);
        }

        return dataModel;
    }

    private StorageModel sqlToStorageModel(Update update) throws
            OpenSearchException, IOException, IndexOutOfBoundsException {
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

    private void setDataModel(DataModel dataModel, String tableName
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        List<DataModel.Meta> metas = dataModel.getMetas() != null ?
                new ArrayList<>(List.of(dataModel.getMetas())) :
                new ArrayList<>();
        var meta = new DataModel.Meta();
        for (int i = 0; i < values.size(); i++) {
            switch (columns.get(i).toString().toLowerCase()) {
                case "id" -> {
                    if (tableName.equalsIgnoreCase("data_model"))
                        dataModel.setId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "name" -> {
                    if (tableName.equalsIgnoreCase("data_model"))
                        dataModel.setName(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "description" -> {
                    if (tableName.equalsIgnoreCase("data_model"))
                        dataModel.setDescription(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                }
                case "type" ->
                        dataModel.setType(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "format" ->
                        dataModel.setFormat(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "status" ->
                        dataModel.setStatus(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "is_system" ->
                        meta.setSystem(Boolean.parseBoolean(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)));
                case "key", "\"key\"" ->
                        meta.setKey(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "value", "\"value\"" ->
                        meta.setValue(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                case "tag" -> {
                    List<String> tags = dataModel.getTags() != null ?
                            new ArrayList<>(List.of(dataModel.getTags())) :
                            new ArrayList<>();

                    tags.add(values.get(i).toString().substring(1, values.get(i).toString().length() - 1));
                    dataModel.setTags(tags.toArray(new String[0]));
                }
                case "created_by" -> dataModel.setCreator(
                        setUserWithId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)));
                case "created_at" -> {
                    var timeStr = values.get(i).toString().substring(1, values.get(i).toString().length() - 1);
                    dataModel.setCreatedAt(Timestamp.valueOf(timeStr).toInstant().toEpochMilli());
                }
                case "last_modified_by" -> dataModel.setLastModifier(
                        setUserWithId(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)));
                case "last_modified_at" -> {
                    var timeStr = values.get(i).toString().substring(1, values.get(i).toString().length() - 1);
                    dataModel.setLastModifiedAt(Timestamp.valueOf(timeStr).toInstant().toEpochMilli());
                }
            }
        }
        metas.add(meta);
        dataModel.setMetas(metas.toArray(new DataModel.Meta[0]));
    }

    private void setStorage(StorageModel storageModel, String tableName
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            switch (columns.get(i).toString().toLowerCase()) {
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
                        storageModel.setUpdatedAt(Timestamp.valueOf(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)).toInstant().toEpochMilli());
                case "status" ->
                        storageModel.setStatus(Utilities.Status.valueOf(values.get(i).toString().substring(1, values.get(i).toString().length() - 1)));
            }
        }
    }

    public List<Query> mainQueryBuilder(String input) {
        var shouldQuery = new LinkedList<Query>();
        for (var field : DataModel.class.getDeclaredFields()) {
            if (field.getType() == String.class || field.getType() == String[].class)
                shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());
        }

        for (var field : DataModel.Meta.class.getDeclaredFields())
            shouldQuery.add(new MatchQuery.Builder().field(field.getName()).query(FieldValue.of(input)).build()._toQuery());

        return shouldQuery;
    }

    public List<Query> mustQueryBuilder(Map<String, String> details) throws ParseException {
        var mustQuery = new ArrayList<Query>();
        var rangeQueryBuilder = new RangeQuery.Builder();

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
                case "START_DATE" -> rangeQueryBuilder.from(
                        JsonData.of(
                                details.get(key).length() <= 10 ?
                                        new SimpleDateFormat("yyyy-MM-dd").parse(details.get(key)).getTime() :
                                        Timestamp.valueOf(details.get(key)).toInstant().toEpochMilli()
                        )
                );
                case "END_DATE" -> rangeQueryBuilder.to(
                        JsonData.of(
                                details.get(key).length() <= 10 ?
                                        new SimpleDateFormat("yyyy-MM-dd").parse(details.get(key)).getTime() :
                                        Timestamp.valueOf(details.get(key)).toInstant().toEpochMilli()
                        )
                );
                case "CATEGORY", "TAG" -> System.out.println("TODO"); // todo
                case "STORAGE_TYPE" -> mustQuery.add(new MatchQuery.Builder()
                        .field("storage_type")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "CONNECTOR_NAME" -> mustQuery.add(new MatchQuery.Builder()
                        .field("connector_name")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                case "CREATOR" -> mustQuery.add(new MatchQuery.Builder()
                        .field("creator")
                        .query(FieldValue.of(details.get(key))).build()._toQuery());
                default -> {// CATEGORY, TAG
                    continue;
                }
            }
        }

        if (details.get("DATE_TYPE") != null) {
            rangeQueryBuilder.field(details.get("DATE_TYPE"));
            mustQuery.add(rangeQueryBuilder.build()._toQuery());
        }

        return mustQuery;
    }

    private DataModelOuterClass.DataModel convertToModel(DataModel dataModel) {

        var dataModelBuilder = DataModelOuterClass.DataModel.newBuilder();
        // todo meta, tag check, permission, update,
        for (var field : dataModel.getClass().getDeclaredFields()) {
            switch (field.getName().toLowerCase()) {
                case "id" -> dataModelBuilder.setId(dataModel.getId());
                case "name" -> dataModelBuilder.setName(dataModel.getName());
                case "description" -> dataModelBuilder.setDescription(dataModel.getDescription());
                case "type" -> dataModelBuilder.setDataType(dataModel.getType());
                case "format" -> dataModelBuilder.setDataFormat(dataModel.getFormat());
                case "status" -> dataModelBuilder.setStatus(dataModel.getStatus());
                case "meta" -> {
                    for (var dataModelMeta : dataModel.getMetas()) {
                        var meta = Utilities.Meta.newBuilder()
                                .setKey(dataModelMeta.getKey())
                                .setValue(dataModelMeta.getValue())
                                .build();

                        if (dataModelMeta.isSystem())
                            dataModelBuilder.addSystemMeta(meta);
                        else
                            dataModelBuilder.addUserMeta(meta);
                    }
                }
                case "tags" -> dataModelBuilder.addAllTag(Arrays.asList(dataModel.getTags()));
                case "creator" -> {
                    var user = UserOuterClass.User.newBuilder()
                            .setId(dataModel.getCreator().getId()) // todo id가 짤려서 들어간다.           id: "en_random_uuid("
                            .setName(dataModel.getCreator().getName() != null ?
                                    dataModel.getCreator().getName() :
                                    ""
                            )
                            .build();
                    dataModelBuilder.setCreator(user);
                }
                case "createdat" -> {
                    var time = new Timestamp(dataModel.getCreatedAt()); // todo do not insert created_at, updatedAt(last_modified_at)
                    var dateTime = Utilities.DateTime.newBuilder()
                            .setStrDateTime(time.toString())
                            .setUtcTime(dataModel.getCreatedAt())
                            .build();
                    dataModelBuilder.setCreatedAt(dateTime);
                }

                case "lastmodifier" -> { // todo 이거랑 at 확인해 볼 것
                    if (dataModel.getLastModifier() != null && dataModel.getLastModifier().getId() != null) {
                        var user = UserOuterClass.User.newBuilder()
                                .setId(dataModel.getLastModifier().getId())
                                .setName(dataModel.getLastModifier().getName() != null ?
                                        dataModel.getLastModifier().getName() :
                                        "")
                                .build();
                        dataModelBuilder.setCreator(user);
                    }
                }
                case "lastmodifiedat" -> {
                    if (dataModel.getCreatedAt() != null) {
                        var time = new Timestamp(dataModel.getCreatedAt());
                        var dateTime = Utilities.DateTime.newBuilder()
                                .setStrDateTime(time.toString())
                                .setUtcTime(dataModel.getCreatedAt())
                                .build();
                        dataModelBuilder.setCreatedAt(dateTime);
                    }
                }
                case "permission" -> {
                    var permission = DataModelOuterClass.Permission.newBuilder()
                            .setRead(true)
                            .setWrite(true)
                            .build(); // todo it's hard coding
                    dataModelBuilder.setPermission(permission);
                }
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
                                .setStrDateTime(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                                .format(Instant.ofEpochMilli(storageModel.getUpdatedAt()))
                                ).setUtcTime(storageModel.getUpdatedAt())
                                .build());
                case "status" -> storageBuilder.setStatus(storageModel.getStatus());
            }
        }
        return storageBuilder.build();
    }

    public void deleteSearchesDocument(String id) throws OpenSearchException, IOException {
        try {
            portalRepository.deleteSearchesDocument(id);
        } catch (OpenSearchException | IOException e) {
            throw e;
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
        // todo
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
                    System.out.println("TODO"); // todo
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

    private DataModel.User setUserWithId(String id) {
        var user = new DataModel.User();
        user.setId(id);
        /**
         * todo
         * 1. setName -> db에서 직접 가져와야함
         * user.setName();
         */
        return user;
    }
}