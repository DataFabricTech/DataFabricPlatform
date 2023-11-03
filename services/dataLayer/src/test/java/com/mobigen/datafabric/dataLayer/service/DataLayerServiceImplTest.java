package com.mobigen.datafabric.dataLayer.service;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.Portal;
import com.mobigen.datafabric.share.protobuf.Utilities;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataLayerServiceImplTest {
    AppConfig appConfig;
    PortalServiceImpl portalService;
    DBConfig dbConfig;
    DataLayerServiceImpl dataLayerService;
    DataLayerRepository dataLayerRepository;
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
//        } catch (ClassNotFoundException | SQLException e) {
//            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void reset() {
        try {
            sleep();

            var ids = portalService.searchAll();
            var q = "delete from %s where id = '%s'";
            for (var id : ids) {
                var deleteSql = String.format(q, dbConfig.getDataCatalog(), id);
                dataLayerService.execute(makeReq(deleteSql));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            portalService.deleteSearchesDocument("testUser");
        } catch (Exception e) {
            // pass
        }
    }

    String insertSql() {
        return "insert into %s " +
                "(name, description, dataType, dataFormat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('insert_name', 'insert_description', 'insert_type', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', true, 'fail', '%s', '%s')";
    }

    String insertSql(String name, String type, String created_by) {
        var id = UUID.randomUUID().toString();
        var insertSql = "insert into %s " +
                "(name, description, datatype, dataformat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', '" + created_by + "', '%s')";
        return String.format(insertSql, dbConfig.getDataCatalog(), id);
    }


    String insertSql2(String name, String type, String format) {
        var id = UUID.randomUUID().toString();
        var insertSql = "insert into %s " +
                "(name, description, datatype, dataformat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', '"+format+"', 1000, " +
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
        assertEquals("200", dataLayerService.execute(reqExecute).getCode());
    }

    @DisplayName("Execute Insert fail")
    @Test
    void insertFailTest() {
        var wrongSql = "insert into failtable values (1,2,3);";
        assertEquals("404", dataLayerService.execute(makeReq(wrongSql)).getCode());
    }

    @DisplayName("Execute Select")
    @Test
    void selectTest() {
        insertDocument();
        sleep();
        var select = String.format(selectSql(), dbConfig.getDataCatalog(), preInserted);
        assertEquals("200", dataLayerService.execute(makeReq(select)).getCode());
    }

    @DisplayName("Execute Select Fail")
    @Test
    void selectFailTest() {
        var wrongSql = "select * from asdf";
        assertEquals("404", dataLayerService.execute(makeReq(wrongSql)).getCode());
    }

    @DisplayName("Execute Update")
    @Test
    void updateTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var update = String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0));
            assertEquals("200", dataLayerService.execute(makeReq(update)).getCode());
        });

        var select = String.format(selectSql(), dbConfig.getDataCatalog(), preInserted);

        assertEquals("updated_name", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(1).getStringValue());
        assertEquals("updated_description", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(2).getStringValue());
    }

    @DisplayName("Execute update fail")
    @Test
    void updateFailTest() {
        var update = String.format(updateSql(), dbConfig.getDataCatalog(), "wrongid");
        assertEquals("404", dataLayerService.execute(makeReq(update)).getCode());
    }

    @DisplayName("Execute delete")
    @Test
    void deleteTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var delete = String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0));
            assertEquals("200", dataLayerService.execute(makeReq(delete)).getCode());
        });
    }

    @DisplayName("Execute delete fail")
    @Test
    void deleteFailTest() {
        var delete = String.format(deleteSql(), dbConfig.getDataCatalog(), "worngId");
        assertEquals("404", dataLayerService.execute(makeReq(delete)).getCode());
    }

    @DisplayName("Execute batch Insert")
    @Test
    void insertBatchTest() {
        var id = UUID.randomUUID().toString();
        var id2 = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id),
                String.format(insertSql(), dbConfig.getDataCatalog(), id2, id2, id2)};

        assertEquals("200", dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
        var select = "select count(*) from data_catalog_test;";

        assertEquals(2, dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(0).getInt64Value());
    }

    @DisplayName("Execute Batch Insert fail")
    @Test
    void insertBatchFailTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id),
                "insert into failTable value (1,2,3);"};

        assertEquals("404", dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
    }

    @DisplayName("Execute Batch Insert with select fail")
    @Test
    void insertBatchFailWithSelectTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id),
                String.format(selectSql(), dbConfig.getDataCatalog(), preInserted)};

        assertEquals("404", dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
    }

    @DisplayName("Execute batch Update")
    @Test
    void updateBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        List<String> IDs = new LinkedList<>();
        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                IDs.add(id);
                sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), id));
            }

            assertEquals("200", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
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
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), "asdf"));

            assertEquals("404", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute update Batch With Select fail")
    @Test
    void updateBatchFailWithSelectFailTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("select * from for_fail;");

            assertEquals("400", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute batch delete")
    @Test
    void deleteBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), id));
            }

            assertEquals("200", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());

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
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), "asdf"));

            assertEquals("404", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute batch delete with select fail")
    @Test
    void deleteBatchWithSelectFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAll();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add("select * from data_catalog_test;");
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(1)));

            assertEquals("400", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute combine query")
    @Test
    void combineQueryTest() {
        insertDocument();
        sleep();
        assertDoesNotThrow(() -> {
            var id = UUID.randomUUID().toString();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id));
            var ids = portalService.searchAll();
            sleep();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));

            assertEquals("200", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Execute combine fail query")
    @Test
    void combineQueryFailTest() {
        insertDocument();
        sleep();
        assertDoesNotThrow(() -> {
            var id = UUID.randomUUID().toString();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(insertSql(), dbConfig.getDataCatalog(), id, id, id));
            var ids = portalService.searchAll();
            sqls.add(String.format(updateSql(), dbConfig.getDataCatalog(), ids.get(0)));
            var newUpdate = String.format(updateSql(), dbConfig.getDataCatalog(), "asdf");
            sqls.add(String.format(newUpdate, dbConfig.getDataCatalog(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataCatalog(), ids.get(0)));

            assertEquals("404", dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    /**
     * name type    createdBy
     * in   1       a
     * in   1       b
     * in   2       a
     * in   2       b
     * out  1       a
     * out  1       b
     * out  2       a
     * out  2       b
     */
    @Test
    void searchWithInput() {
        var in = "in";
        var out = "out";
        var one = "1";
        var two = "2";
        var A = "a";
        var B = "b";

        var sqls = new String[]{
                insertSql(in, one, A),
                insertSql(in, one, B),
                insertSql(in, two, A),
                insertSql(in, two, B),
                insertSql(out, one, A),
                insertSql(out, one, B),
                insertSql(out, two, A),
                insertSql(out, two, B),
        };
        insertDocuemt(sqls);

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

        var sqls = new String[]{
                insertSql(in, one, A),
                insertSql(in, one, B),
                insertSql(in, two, A),
                insertSql(in, two, B),
                insertSql(out, one, A),
                insertSql(out, one, B),
                insertSql(out, two, A),
                insertSql(out, two, B),
        };
        insertDocuemt(sqls);

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

        var sqls = new String[]{
                insertSql2(in, one, A),
                insertSql2(in, one, B),
                insertSql2(in, one, C),
                insertSql2(in, two, A),
                insertSql2(in, two, B),
                insertSql2(out, one, A),
                insertSql2(out, one, B),
                insertSql2(out, two, A),
                insertSql2(out, two, B),
        };
        sleep();

        insertDocuemt(sqls);

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


    void insertDocuemt(String insertSql) {
        var id = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql, dbConfig.getDataCatalog(), id, id, id))));
    }

    void insertDocuemt(String[] sqls) {
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