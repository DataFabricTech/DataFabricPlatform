package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.model.ResponseCode;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.Portal;
import com.mobigen.datafabric.share.protobuf.Utilities;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataLayerServiceImplTest {
    AppConfig appConfig;
    PortalServiceImpl portalService;
    DBConfig dbConfig;
    DataLayerServiceImpl dataLayerService;
    DataLayerRepository dataLayerRepository;
    PortalRepository portalRepository;
    String preInserted;

    @BeforeEach
    void init() {
        appConfig = new AppConfig();
        dbConfig = appConfig.dbConfig();
        try {
            appConfig.portalRepository().createIndex();
            portalService = appConfig.portalService();
            dataLayerRepository = appConfig.dataLayerRepository();
            dataLayerService = appConfig.dataLayerServiceImpl();
            portalRepository = appConfig.portalRepository();
//        } catch (ClassNotFoundException | SQLException e) {
//            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void reset() {
        // stoarge 제거
        try {
            sleep();

            var storage = portalService.searchAllStorage();
            var id = storage.isEmpty() ? "" : storage.get(0);

            var storageSqls = new String[]{
                    String.format("delete from dataStorageTag where datastorage_id = '%s'", id),
                    String.format("delete from dataStorage where id = '%s'", id),
                    String.format("delete from datastorageadaptor where id = '%s'", id),
                    "delete from dataStorageType where name ='testStorageTypeName' ",
            };
            dataLayerService.executeBatch(makeBatchReq(storageSqls));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // data catalog 제거
        try {
            var ids = portalService.searchAllDataCatalog();

            for (var dataCatalogId : ids) {
                dataLayerService.execute(makeReq(String.format(deleteSql(), dbConfig.getDataCatalog(), dataCatalogId)));
            }

        } catch (IOException e) {
            fail(e.getMessage());
        }

        // 검색어 제거
        try {
            portalService.deleteSearchesDocument("testUser");
        } catch (Exception e) {
//            fail(e.getMessage());
            // pass
        }
    }

    String insertSql() {
        return "insert into %s " +
                "(id, name, description, dataType, dataFormat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('%s', 'insert_name', 'insert_description', 'insert_type', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', true, 'fail', '%s', '%s')";
    }

    String insertSql(String name, String type, String format) {
        var id = UUID.randomUUID().toString();
        var insertSql = "insert into %s " +
                "(id, name, description, datatype, dataformat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('%s', '" + name + "', 'insert_description', '" + type + "', '" + format + "', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', 'created_by', '%s')";
        return String.format(insertSql, dbConfig.getDataCatalog(), id, id);
    }


    String insertSql2(String name, String type, String format) {
        var id = UUID.randomUUID().toString();
        var insertSql = "insert into %s " +
                "(name, description, datatype, dataformat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', '" + format + "', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', 'created_by', '%s')";
        return String.format(insertSql, dbConfig.getDataCatalog(), id);
    }

    //
    String deleteSql() {
        return "delete from %s where id = '%s';";
    }

    String selectSql() {
        return "select * from %s where created_by = '%s';";
    }

    String selectIDSql() {
        return "select * from %s where id = '%s';";
    }

    String updateSql() {
        return "update %s set name='updated_name', description = 'updated_description' where id = '%s'";
    }

    // todo storage는 다음으로 미루자?
    @DisplayName("Execute Insert")
    @Test
    void insertTest() {
        var id = UUID.randomUUID().toString();
        var insertSql = String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id);
        var reqExecute = DataLayer.ReqExecute.newBuilder().setSql(insertSql).build();
        assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(reqExecute).getCode());
    }

    @DisplayName("Execute Insert fail")
    @Test
    void insertFailTest() {
        var wrongSql = "insert into failtable values (1,2,3);";
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(wrongSql)).getCode());
    }

    @DisplayName("Execute Select")
    @Test
    void selectTest() {
        insertDocument();
        sleep();
        var select = String.format(selectSql(), dbConfig.getDataCatalog(), preInserted);
        assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(select)).getCode());
    }

    @DisplayName("Execute Select Fail")
    @Test
    void selectFailTest() {
        var wrongSql = "select * from asdf";
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(wrongSql)).getCode());
    }

    @DisplayName("Execute Update")
    @Test
    void updateTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var update = String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0));
            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(update)).getCode());
        });

        var select = String.format(selectSql(), dbConfig.getDataCatalog(), preInserted);

        assertEquals("updated_name", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(1).getStringValue());
        assertEquals("updated_description", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(2).getStringValue());
    }

    @DisplayName("Execute update fail")
    @Test
    void updateFailTest() {
        var update = String.format(updateSql(), dbConfig.getDataCatalog(), "wrongid");
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(update)).getCode());
    }

    @DisplayName("Execute delete")
    @Test
    void deleteTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var delete = String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0));
            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(delete)).getCode());
        });
    }

    @DisplayName("Execute delete fail")
    @Test
    void deleteFailTest() {
        var delete = String.format(deleteSql(), dbConfig.getDataCatalog(), "worngId");
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(delete)).getCode());
    }

    @DisplayName("Execute batch Insert")
    @Test
    void insertBatchTest() {
        var id = UUID.randomUUID().toString(); // storage_id
        var id2 = UUID.randomUUID().toString(); // adaptor_id

        var sqls = new String[]{
                "insert into DataStorageType (name) values ('testStorageTypeName')",
                String.format("insert into DataStorageAdaptor (id, storage_type_name) values ('%s', 'testStorageTypeName')", id),
                String.format("insert into DataStorage (id,adaptor_id, name, user_desc, created_by) values ('%s', '%s', 'testName','testDesc','%s')", id, id, id),
                String.format("insert into DataStorageTag (datastorage_id, tag) values ('%s','tag1')", id),
                String.format("insert into DataStorageTag (datastorage_id, tag) values ('%s','tag2')", id),
                String.format("insert into DataStorageTag (datastorage_id, tag) values ('%s','tag3')", id),
                String.format("insert into DataStorageTag (datastorage_id, tag) values ('%s','tag4')", id)};

        assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
        sleep();

        assertEquals(1, (portalService.search(makeReq("testName", null, null, null)).getData().getSearchResponse()).getPageable().getPage().getTotalSize());
    }

    @DisplayName("Execute Batch Insert fail")
    @Test
    void insertBatchFailTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id),
                "insert into failTable value (1,2,3);"};

        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
    }

    @DisplayName("Execute Batch Insert with select fail")
    @Test
    void insertBatchFailWithSelectTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id),
                String.format(selectSql(), dbConfig.getDataCatalog(), preInserted)};

        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
    }

    @DisplayName("Execute batch Update")
    @Test
    void updateBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        List<String> IDs = new LinkedList<>();
        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                IDs.add(id);
                sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), id));
            }

            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });

        for (var id : IDs) {
            var select = String.format(selectIDSql(), dbConfig.getDataCatalog(), id);

            assertEquals("updated_name", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(1).getStringValue());
            assertEquals("updated_description", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(2).getStringValue());
        }

        var select = "select count(*) from data_catalog_test;";
        assertEquals(2, dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(0).getInt64Value());
    }

    @DisplayName("Execute update Batch fail")
    @Test
    void updateBatchFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("asdf");

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute update Batch With Select fail")
    @Test
    void updateBatchFailWithSelectFailTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("select * from for_fail;");

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute batch delete")
    @Test
    void deleteBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), id));
            }

            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());

        });

        var select = "select count(*) from data_catalog_test;";
        sleep();
        assertEquals(0, dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(0).getInt64Value());
    }

    @DisplayName("Execute batch delete fail")
    @Test
    void deleteBatchFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("asdf");

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute batch delete with select fail")
    @Test
    void deleteBatchWithSelectFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataCatalog();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("select * from data_catalog_test;");
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(1)));

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @Deprecated
    @DisplayName("Execute combine query")
    @Test
    void combineQueryTest() {
        insertDocument();
        sleep();
        assertDoesNotThrow(() -> {
            var id = UUID.randomUUID().toString();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id));
            var ids = portalService.searchAllDataCatalog();
            sleep();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));

            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @Deprecated
    @DisplayName("Execute combine fail query")
    @Test
    void combineQueryFailTest() {
        // combine은 사용 안할 예정입니다.
        insertDocument();
        sleep();
        assertDoesNotThrow(() -> {
            var id = UUID.randomUUID().toString();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id));
            var ids = portalService.searchAllDataCatalog();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            var newUpdate = String.format(updateSql(), dbConfig.getDataCatalog(), "asdf");
            sqls.add(String.format(newUpdate, dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @Test
    void searchWithInput() {
        var in = "in";
        var out = "out";
        var one = "1";
        var two = "2";
        var A = "a";
        var B = "b";

        insertDocument(insertSql(in, one, A));
        insertDocument(insertSql(in, one, B));
        insertDocument(insertSql(in, two, A));
        insertDocument(insertSql(in, two, B));
        insertDocument(insertSql(out, one, A));
        insertDocument(insertSql(out, one, B));
        insertDocument(insertSql(out, two, A));
        insertDocument(insertSql(out, two, B));
        sleep();
        var filter = portalService.search(makeReq("in", null, null, null))
                .getData().getSearchResponse().getFiltersMap();

        assertEquals(2, filter.get("dataType").getValueCount());
        assertEquals("1", filter.get("dataType").getValue(0).getKey());
        assertEquals(2, filter.get("dataType").getValue(0).getValue());
        assertEquals("2", filter.get("dataType").getValue(1).getKey());
        assertEquals(2, filter.get("dataType").getValue(1).getValue());
    }


    @Test
    void searchWithInputADetail() {
        var in = "in";
        var out = "out";
        var one = "1";
        var two = "2";
        var A = "a";
        var B = "b";
        insertDocument(insertSql(in, one, A));
        insertDocument(insertSql(in, one, B));
        insertDocument(insertSql(in, two, A));
        insertDocument(insertSql(in, two, B));
        insertDocument(insertSql(out, one, A));
        insertDocument(insertSql(out, one, B));
        insertDocument(insertSql(out, two, A));
        insertDocument(insertSql(out, two, B));

        var details = new HashMap<String, String>();
        details.put("DATA_TYPE", "1");
        var filter = portalService.search(makeReq("in", null, details, null))
                .getData().getSearchResponse().getFiltersMap();

        assertEquals(1, filter.get("dataType").getValueCount());
        assertEquals("1", filter.get("dataType").getValue(0).getKey());
        assertEquals(2, filter.get("dataType").getValue(0).getValue());
    }


    @Test
    void searchWithInputADetailAFilter() {
        var in = "in";
        var out = "out";
        var one = "1";
        var two = "2";
        var A = "a";
        var B = "b";
        var C = "zxcv";

        insertDocument(insertSql(in, one, A));
        insertDocument(insertSql(in, one, B));
        insertDocument(insertSql(in, one, C));
        insertDocument(insertSql(in, two, A));
        insertDocument(insertSql(in, two, B));
        insertDocument(insertSql(out, one, A));
        insertDocument(insertSql(out, one, B));
        insertDocument(insertSql(out, two, A));
        insertDocument(insertSql(out, two, B));

        var details = new HashMap<String, String>();
        details.put("DATA_TYPE", "1");

        var filter = new HashMap<String, Portal.ListString>();
        filter.put("DATA_FORMAT", Portal.ListString.newBuilder().addValue("a").addValue("zxcv").build());

        var filters = portalService.search(makeReq("in", null, details, filter))
                .getData().getSearchResponse().getFiltersMap();

        assertEquals(1, filters.get("name").getValueCount());
        assertEquals(2, filters.get("dataFormat").getValueCount());
        assertEquals(1, filters.get("dataFormat").getValue(0).getValue());
        assertEquals(1, filters.get("dataFormat").getValue(1).getValue());
    }

    @DisplayName("recent Search success test")
    @Test
    void recentSearch() {
        portalService.search(makeReq("a", null, null, null));
        sleep();
        portalService.search(makeReq("b", null, null, null));
        sleep();
        portalService.search(makeReq("c", null, null, null));
        sleep();
        portalService.search(makeReq("d", null, null, null));
        sleep();
        portalService.search(makeReq("e", null, null, null));
        sleep();
        portalService.search(makeReq("f", null, null, null));
        sleep();
        portalService.search(makeReq("g", null, null, null));
        sleep();
        portalService.search(makeReq("h", null, null, null));
        sleep();
        portalService.search(makeReq("i", null, null, null));
        sleep();
        portalService.search(makeReq("j", null, null, null));
        sleep();
        portalService.search(makeReq("k", null, null, null));
        sleep();
        var recent = portalService.recentSearches(Empty.newBuilder().build());
        sleep();
        assertEquals(11, recent.getData().getRecentSearchesCount());
    }

    void insertDocument() {
        preInserted = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql(), dbConfig.getDataCatalog(), preInserted, preInserted, preInserted))));
    }


    void insertDocument(String insertSql) {
        var id = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql, dbConfig.getDataCatalog(), id, id, id))));
    }

    void insertDocument(String[] sqls) {
        assertDoesNotThrow(() ->
                dataLayerService.executeBatch(makeBatchReq(sqls)));
    }

    void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    DataLayer.ReqExecute makeReq(String sql) {
        return DataLayer.ReqExecute.newBuilder().setSql(sql).build();
    }

    DataLayer.ReqBatchExecute makeBatchReq(String[] sqls) {
        return DataLayer.ReqBatchExecute.newBuilder().addAllSql(Arrays.asList(sqls)).build();
    }

    Portal.ReqSearch makeReq(String input, Utilities.Pageable pageable, Map<String, String> details, Map<String, Portal.ListString> filters) {
        var reqSearch = Portal.ReqSearch.newBuilder();
        if (pageable != null) reqSearch.setPageable(pageable);
        if (details != null) reqSearch.putAllDetailSearch(details);
        if (filters != null) reqSearch.putAllFilterSearch(filters);

        return reqSearch
                .setKeyword(input)
                .build();
    }
}