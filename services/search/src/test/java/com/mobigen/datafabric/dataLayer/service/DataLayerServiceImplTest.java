package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
        } catch (ClassNotFoundException | SQLException e) {
            fail(e.getMessage());
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

//        try {
//            openSearchRepository.deleteSearchesDocument("testUser");
//        } catch (Exception e) {
//            // pass
//        }
    }

    String insertSql() {
        return "insert into %s " +
                "(name, description, dataType, dataFormat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('insert_name', 'insert_description', 'insert_type', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', true, 'fail', '%s', '%s')";
    }

    //
//    String insertSql(String name, String type, String created_by) {
//        return "insert into %s " +
//                "(name, description, type, format, size, sample_data_id ,status, " +
//                "cache_enable, cache_status, created_by, updated_by)" +
//                " values ('" + name + "', 'insert_description', '" + type + "', 'insert_format', 1000, " +
//                "'insert_sampledata_id','success', false, 'fail', '" + created_by + "', '%s')";
//    }
//
//    String insertSql2(String name, String type, String format) {
//        return "insert into %s " +
//                "(name, description, type, format, size, sample_data_id ,status, " +
//                "cache_enable, cache_status, created_by, updated_by)" +
//                " values ('" + name + "', 'insert_description', '" + type + "', '" + format + "', 1000, " +
//                "'insert_sampledata_id','success', false, 'fail', 'created_by', '%s')";
//    }
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

            assertEquals("200",dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());

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

            assertEquals("404",dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
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
//
//    /**
//     * name type    createdBy
//     * in   1       a
//     * in   1       b
//     * in   2       a
//     * in   2       b
//     * out  1       a
//     * out  1       b
//     * out  2       a
//     * out  2       b
//     */
//    @Test
//    void searchWithInput() {
//        var in = "in";
//        var out = "out";
//        var one = "1";
//        var two = "2";
//        var A = "a";
//        var B = "b";
//
//        insertDocuemt(insertSql(in, one, A));
//        insertDocuemt(insertSql(in, one, B));
//        insertDocuemt(insertSql(in, two, A));
//        insertDocuemt(insertSql(in, two, B));
//        insertDocuemt(insertSql(out, one, A));
//        insertDocuemt(insertSql(out, one, B));
//        insertDocuemt(insertSql(out, two, A));
//        insertDocuemt(insertSql(out, two, B));
//        sleep();
//
//        var filter = dataLayerService.search("in", null, null, "testUser").getFilters();
//
//        assertEquals(2, filter.getTypeFilter(0).getCount());
//        assertEquals(4, filter.getNameFilter(0).getCount());
//    }
//
//    @Test
//    void searchWithInputADetail() {
//        var in = "in";
//        var out = "out";
//        var one = "1";
//        var two = "2";
//        var A = "a";
//        var B = "b";
//
//        insertDocuemt(insertSql(in, one, A));
//        insertDocuemt(insertSql(in, one, B));
//        insertDocuemt(insertSql(in, two, A));
//        insertDocuemt(insertSql(in, two, B));
//        insertDocuemt(insertSql(out, one, A));
//        insertDocuemt(insertSql(out, one, B));
//        insertDocuemt(insertSql(out, two, A));
//        insertDocuemt(insertSql(out, two, B));
//
//        var detail = DataSet.newBuilder().setType("1").build();
//
//        var filter = dataLayerService.search("in", detail, null, "testUser").getFilters();
//
//        assertEquals(2, filter.getTypeFilter(0).getCount());
//        assertEquals(2, filter.getNameFilter(0).getCount());
//    }
//
//    @Test
//    void searchWithInputADetailAFilter() {
//        var in = "in";
//        var out = "out";
//        var one = "1";
//        var two = "2";
//        var A = "a";
//        var B = "b";
//        var C = "zxcv";
//
//        insertDocuemt(insertSql2(in, one, A));
//        insertDocuemt(insertSql2(in, one, B));
//        insertDocuemt(insertSql2(in, one, C));
//        insertDocuemt(insertSql2(in, two, A));
//        insertDocuemt(insertSql2(in, two, B));
//        insertDocuemt(insertSql2(out, one, A));
//        insertDocuemt(insertSql2(out, one, B));
//        insertDocuemt(insertSql2(out, two, A));
//        insertDocuemt(insertSql2(out, two, B));
//        sleep();
//
//        var detail = DataSet.newBuilder().setType("1").build();
//        var filter = Filter.newBuilder().addFormat("a").addFormat("zxcv").build();
//
//        var filters = dataLayerService.search("in", detail, filter, "testUser").getFilters();
//
//        assertEquals(2, filters.getNameFilter(0).getCount());
//        assertEquals(2, filters.getTypeFilter(0).getCount());
//        assertEquals(1, filters.getFormatFilter(0).getCount());
//        assertEquals(1, filters.getFormatFilter(1).getCount());
//    }
//
//    @DisplayName("recent Search success test")
//    @Test
//    void recentSearch() {
//        dataLayerService.search("a", null, null, "testUser");
//        sleep();
//        dataLayerService.search("b", null, null, "testUser");
//        sleep();
//        dataLayerService.search("c", null, null, "testUser");
//        sleep();
//        dataLayerService.search("d", null, null, "testUser");
//        sleep();
//        dataLayerService.search("e", null, null, "testUser");
//        sleep();
//        dataLayerService.search("f", null, null, "testUser");
//        sleep();
//        dataLayerService.search("g", null, null, "testUser");
//        sleep();
//        dataLayerService.search("h", null, null, "testUser");
//        sleep();
//        dataLayerService.search("i", null, null, "testUser");
//        sleep();
//        dataLayerService.search("j", null, null, "testUser");
//        sleep();
//        dataLayerService.search("k", null, null, "testUser");
//        sleep();
//        var recent = dataLayerService.recentSearch("testUser");
//        assertEquals(11, recent.getSearchedCount());
//    }
//
//    @Test
//    void healthCheck() {
//        assertTrue(dataLayerService.healthCheck().getStatus());
//    }
//
    void insertDocument() {
        preInserted = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql(), dbConfig.getDataCatalog(), preInserted, preInserted, preInserted))));
    }

    //
//    void insertDocuemt(String insertSql) {
//        var id = UUID.randomUUID().toString();
//        assertDoesNotThrow(() ->
//                dataLayerService.execute(String.format(insertSql, dbConfig.getDataCatalog(), id, id, id)));
//    }
//
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
//        var data = Arrays.stream(dataLayerRepository.executeBatchUpdate(sqls)).boxed().collect(Collectors.toList());
//        List<String> newSqls = Arrays.stream(sqls)
        return DataLayer.ReqBatchExecute.newBuilder().addAllSql(Arrays.asList(sqls)).build();
    }
}