package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.config.PortalConfig;
import com.mobigen.datafabric.dataLayer.model.DataCatalogModel;
import com.mobigen.datafabric.dataLayer.model.RecentSearchesModel;
import com.mobigen.datafabric.dataLayer.model.StorageModel;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.share.protobuf.DataCatalogOuterClass;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.Portal.*;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
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

            var currentSize = searchContentBuilder.getDataCatalogsCount() + searchContentBuilder.getStoragesCount();
            var resPageable = Utilities.Pageable.newBuilder()
                    .setPage(
                            Utilities.Page.newBuilder()
                                    .setSize(currentSize)
                                    .setSelectPage(pageable.getPage().getSelectPage())
                                    .setTotalPage(totalSize)
                                    .build()
                    ).build();

            searchResponseBuilder
                    .setPageable(resPageable)
                    .setContents(searchContentBuilder.build());

            resSearchBuilder.setCode("200").setData(
                    ResSearch.Data.newBuilder().setSearchResponse(searchResponseBuilder.build())
            );

            // recentSearches
            saveRecent(input, userId);

            return resSearchBuilder.build();
        } catch (OpenSearchException | IOException | SQLException | ClassNotFoundException e) {
            return ResSearch.newBuilder()
                    .setCode("404")
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
                    .setCode("404")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    private int dataCatalogBuilder(SearchContent.Builder builder, String input, Map<String, String> details
            , Map<String, ListString> filters, Utilities.Pageable pageable) throws OpenSearchException, IOException, SQLException, ClassNotFoundException {
        var sql = "select * from %s where id = '%s'";
        var searchModel = portalRepository.search(
                mainQueryBuilder(input),
                details.isEmpty() ? null : mustQueryBuilder(details),
                filters.isEmpty() ? null : shouldQueryBuilder(filters),
                pageable
        );
        var totalSize = searchModel.getTotalSize();
        var dataCatalogIds = searchModel.getIds();

        if (dataCatalogIds != null && !dataCatalogIds.isEmpty()) {
            for (var dataCatalogId : dataCatalogIds) {
                var table = dataLayerRepository.executeQuery(
                        String.format(sql, dbConfig.getDataCatalog(), dataCatalogId));
                builder.addDataCatalogs(convertToCatalog(table));
            }
        }

        return totalSize;
    }

    private int storageBuilder(SearchContent.Builder builder, String input, Utilities.Pageable pageable)
            throws OpenSearchException, IOException, SQLException, ClassNotFoundException {
        var sql = "select * from %s where id = '%s'";
        var searchModel = portalRepository.search(storageMainQueryBuilder(input), pageable);
        var totalSize = searchModel.getTotalSize();
        var storageIds = searchModel.getIds();

        if (storageIds != null && !storageIds.isEmpty()) {
            for (var storageId : storageIds) {
                var table = dataLayerRepository.executeQuery(String.format(sql, dbConfig.getStorage(), storageId));
                builder.addStorages(convertToStorage(table));
            }
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
        setDataCatalog(dataCatalogModel, columns, values);

        return dataCatalogModel;
    }

    private DataCatalogModel sqlToModel(Update update) throws OpenSearchException, IOException, IndexOutOfBoundsException {
        // todo getId(update)가 진짜로 id를 가져오는지 확인
        var dataCatalogModel = portalRepository.getDocuemnt(getId(update));

        for (var set : update.getUpdateSets()) {
            var columns = set.getColumns();
            var values = set.getValues();

            setDataCatalog(dataCatalogModel, columns, values);
        }

        return dataCatalogModel;
    }

    private String getId(Delete delete) {
        return ((EqualsTo) delete.getWhere()).getRightExpression().toString();
    }

    private String getId(Update update) {
        return ((EqualsTo) update.getWhere()).getRightExpression().toString();
    }

    private void setDataCatalog(DataCatalogModel dataCatalogModel
            , ExpressionList<Column> columns, ExpressionList<?> values) {
        for (int i = 0; i < values.size(); i++) {
            // todo meta, tags, createdAt
            switch (columns.get(i).toString()) {
                case "id" -> dataCatalogModel.setId(values.get(i).toString());
                case "name" -> dataCatalogModel.setName(values.get(i).toString());
                case "description" -> dataCatalogModel.setDescription(values.get(i).toString());
                case "dataType", "datatype" -> dataCatalogModel.setDataType(values.get(i).toString());
                case "dataFormat", "dataformat" -> dataCatalogModel.setDataFormat(values.get(i).toString());
                case "knowledgeGraph" -> dataCatalogModel.setKnowledgeGraph(values.get(i).toString());
                case "status" -> dataCatalogModel.setStatus(Boolean.parseBoolean(values.get(i).toString()));
                case "connectorType" -> dataCatalogModel.setConnectorType(values.get(i).toString());
                case "connectorName" -> dataCatalogModel.setConnectorName(values.get(i).toString());
                case "creatorId" -> dataCatalogModel.setCreatorId(values.get(i).toString());
                case "creatorName" -> dataCatalogModel.setCreatorName(values.get(i).toString());
                case "categories", "tags", "createdAt" -> System.out.println("TODO");
            }
        }
    }

    private DataCatalogOuterClass.DataCatalog setDataCatalog(List<DataLayer.Column> columns, List<DataLayer.Cell> values) {
        var dataCatalogBuilder = DataCatalogOuterClass.DataCatalog.newBuilder();
        for (int i = 0; i < values.size(); i++) {
            // todo mapping
            switch (columns.get(i).getColumnName()) {
                case "id" -> dataCatalogBuilder.setId(values.get(i).getStringValue());
                case "name" -> dataCatalogBuilder.setName(values.get(i).getStringValue());
                case "description" -> dataCatalogBuilder.setDescription(values.get(i).getStringValue());
                case "status" -> dataCatalogBuilder.setStatus(values.get(i).getStringValue());
                case "datatype" -> dataCatalogBuilder.setDataType(values.get(i).getStringValue());
                case "dataformat" -> dataCatalogBuilder.setDataFormat(values.get(i).getStringValue());
                case "row" -> dataCatalogBuilder.setRow(values.get(i).getInt32Value());
                case "size" -> dataCatalogBuilder.setSize(values.get(i).getInt32Value());
                case "dataLocation", "dataStructur", "dataRefine", "category"
                        , "systemMeta", "userMeta", "tag", "permission", "downloadInfo"
                        , "ratingAndComment", "statistics", "creator", "createdAt", "lastModifier", "lastModifiedAt" -> {
                    // todo knowlegdeGraph?
                    /**
                     * User
                     * var user = UserOuterClass.User.newBuilder()
                     * .setId(values.get(i).toString());
                     * or .setName(values.get(i).toString());
                     * dataCatalogBuilder.setCreator(user);
                     */

                    /**
                     * Connector 관련
                     * var location = DataCatalogOuterClass.DataLocation.newBuilder()
                     *         .setDatabaseName(values.get(i).toString()).build();
                     * dataCatalogBuilder.addDataLocation(location);
                     */
                    System.out.println("TODO");
                }
            }
        }
        return dataCatalogBuilder.build();
    }

    private StorageOuterClass.Storage setStorage(List<DataLayer.Column> columns, List<DataLayer.Cell> values) {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        for (int i = 0; i < values.size(); i++) {
            // todo meta, tags, createdAt
            switch (columns.get(i).getColumnName()) {
                case "id" -> storageBuilder.setId(values.get(i).toString());
                case "name" -> storageBuilder.setName(values.get(i).toString());
                case "description" -> storageBuilder.setDescription(values.get(i).toString());
                case "storageType" -> storageBuilder.setStorageType(values.get(i).toString());
                case "adaptorId" -> storageBuilder.setAdaptorId(values.get(i).toString());
                case "systemMeta", "userMeta", "tag", "basicOptions", "additonalOptions"
                        , "settings", "statue", "statistics", "dataStatistics", "history"
                        , "event", "createdBy", "createdAt", "lastModifiedBy", "lastModifiedAt" ->
                        System.out.println("TODO");
            }
        }

        return storageBuilder.build();
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

    private DataCatalogOuterClass.DataCatalog convertToCatalog(DataLayer.Table table) {
        // todo 추후에 확정된 schema로 mapping 필요
        return setDataCatalog(table.getColumnsList(), table.getRows(0).getCellList());
    }

    private StorageOuterClass.Storage convertToStorage(DataLayer.Table table) {
        // todo 추후에 확정된 schema로 mapping 필요
        return setStorage(table.getColumnsList(), table.getRows(0).getCellList());
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
