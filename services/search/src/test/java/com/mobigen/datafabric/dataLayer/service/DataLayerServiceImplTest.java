package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.libs.grpc.DataSet;
import com.mobigen.libs.grpc.Filter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DataLayerServiceImplTest {
    AppConfig appConfig;
    DBConfig dbConfig;
    DataLayerServiceImpl dataLayerService;
    OpenSearchService openSearchService;
    OpenSearchRepository openSearchRepository;
    String preInserted;


    @BeforeEach
    void init() {
        appConfig = new AppConfig();
        dbConfig = appConfig.dbConfig();
        try {
            appConfig.openSearchService().createIndex();
            dataLayerService = appConfig.dataLayerServiceImpl();
            openSearchService = appConfig.openSearchService();
            openSearchRepository = appConfig.openSearchRepository();
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
            var ids = openSearchService.search();
            var q = "delete from %s where id = '%s'";
            for (var id : ids) {
                var deleteSql = String.format(q, dbConfig.getDataSet(), id);
                dataLayerService.execute(deleteSql);
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            openSearchRepository.deleteSearchesDocument("testUser");
        } catch (Exception e) {
            // pass
        }
    }

    String insertSql() {
        return "insert into %s " +
                "(name, description, type, format, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('insert_name', 'insert_description', 'insert_type', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', true, 'fail', '%s', '%s')";
    }

    String insertSql(String name, String type, String created_by) {
        return "insert into %s " +
                "(name, description, type, format, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', 'insert_format', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', '" + created_by + "', '%s')";
    }

    String insertSql2(String name, String type, String format) {
        return "insert into %s " +
                "(name, description, type, format, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', '" + format + "', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', 'created_by', '%s')";
    }

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
        var insertSql = String.format(insertSql(), dbConfig.getDataSet(), id, id, id);
        assertTrue(dataLayerService.execute(insertSql).getStatus());
    }

    @DisplayName("Execute Insert fail")
    @Test
    void insertFailTest() {
        var wrongSql = "insert into failtable values (1,2,3);";
        assertFalse(dataLayerService.execute(wrongSql).getStatus());
    }

    @DisplayName("Execute Select")
    @Test
    void selectTest() {
        // given
        insertDocument();
        sleep();
        var select = String.format(selectSql(), dbConfig.getDataSet(), preInserted);
        assertTrue(dataLayerService.execute(select).getStatus());
    }

    @DisplayName("Execute Select Fail")
    @Test
    void selectFailTest() {
        var wrongSql = "select * from asdf";
        assertFalse(dataLayerService.execute(wrongSql).getStatus());
    }

    @DisplayName("Execute Update")
    @Test
    void updateTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var update = String.format(updateSql(), dbConfig.getDataSet(), ids.get(0));
            assertTrue(dataLayerService.execute(update).getStatus());
        });

        var select = String.format(selectSql(), dbConfig.getDataSet(), preInserted);

        assertEquals("updated_name", dataLayerService.execute(select).getTable().getRows(0).getRow(1).getStringValue());
        assertEquals("updated_description", dataLayerService.execute(select).getTable().getRows(0).getRow(2).getStringValue());
    }

    @DisplayName("Execute update fail")
    @Test
    void updateFailTest() {
        var update = String.format(updateSql(), dbConfig.getDataSet(), "wrongid");
        assertFalse(dataLayerService.execute(update).getStatus());
    }

    @DisplayName("Execute delete")
    @Test
    void deleteTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var delete = String.format(deleteSql(), dbConfig.getDataSet(), ids.get(0));
            assertTrue(dataLayerService.execute(delete).getStatus());
        });
    }

    @DisplayName("Execute delete fail")
    @Test
    void deleteFailTest() {
        var delete = String.format(deleteSql(), dbConfig.getDataSet(), "worngId");
        assertFalse(dataLayerService.execute(delete).getStatus());
    }

    @DisplayName("Execute batch Insert")
    @Test
    void insertBatchTest() {
        var id = UUID.randomUUID().toString();
        var id2 = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataSet(), id, id, id),
                String.format(insertSql(), dbConfig.getDataSet(), id2, id2, id2)};

        assertTrue(dataLayerService.executeBatch(sqls).getStatus());
        var select = "select count(*) from data_set_test;";

        assertEquals(2, dataLayerService.execute(select).getTable().getRows(0).getRow(0).getInt64Value());
    }

    @DisplayName("Execute Batch Insert fail")
    @Test
    void insertBatchFailTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataSet(), id, id, id),
                "insert into failTable value (1,2,3);"};

        assertFalse(dataLayerService.executeBatch(sqls).getStatus());
    }

    @DisplayName("Execute Batch Insert with select fail")
    @Test
    void insertBatchFailWithSelectTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataSet(), id, id, id),
                String.format(selectSql(), dbConfig.getDataSet(), preInserted)};

        assertFalse(dataLayerService.executeBatch(sqls).getStatus());
    }

    @DisplayName("Execute batch Update")
    @Test
    void updateBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        List<String> IDs = new LinkedList<>();
        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                IDs.add(id);
                sqls.add(String.format(updateSql(), dbConfig.getDataSet(), id));
            }

            assertTrue(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });

        for (var id : IDs) {
            var select = String.format(selectIDSql(), dbConfig.getDataSet(), id);

            assertEquals("updated_name", dataLayerService.execute(select).getTable().getRows(0).getRow(1).getStringValue());
            assertEquals("updated_description", dataLayerService.execute(select).getTable().getRows(0).getRow(2).getStringValue());
        }

        var select = "select count(*) from data_set_test;";
        assertEquals(2, dataLayerService.execute(select).getTable().getRows(0).getRow(0).getInt64Value());
    }

    @DisplayName("Execute update Batch fail")
    @Test
    void updateBatchFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataSet(), ids.get(0)));
            sqls.add(String.format(updateSql(), dbConfig.getDataSet(), "asdf"));

            assertFalse(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });

    }

    @DisplayName("Execute update Batch With Select fail")
    @Test
    void updateBatchFailWithSelectFailTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataSet(), ids.get(0)));
            sqls.add("select * from for_fail;");

            assertFalse(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });
    }

    @DisplayName("Execute batch delete")
    @Test
    void deleteBatchTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), id));
            }

            assertTrue(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });

        var select = "select count(*) from data_set_test;";
        sleep();
        assertEquals(0, dataLayerService.execute(select).getTable().getRows(0).getRow(0).getInt64Value());
    }

    @DisplayName("Execute batch delete fail")
    @Test
    void deleteBatchFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), "asdf"));

            assertFalse(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });
    }

    @DisplayName("Execute batch delete with select fail")
    @Test
    void deleteBatchWithSelectFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = openSearchService.search();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), ids.get(0)));
            sqls.add("select * from data_set_test;");
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), ids.get(1)));

            assertFalse(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
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
            sqls.add(String.format(insertSql(), dbConfig.getDataSet(), id, id, id));
            var ids = openSearchService.search();
            sleep();
            sqls.add(String.format(updateSql(), dbConfig.getDataSet(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), ids.get(0)));

            assertTrue(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });
    }

    /**
     * Update 도중에 똑같은 Document를 Update하는 경우 문제가 발생한다.
     */
    @DisplayName("Execute combine fail query")
    @Test
    void combineQueryFailTest() {
        insertDocument();
        sleep();
        assertDoesNotThrow(() -> {
            var id = UUID.randomUUID().toString();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(insertSql(), dbConfig.getDataSet(), id, id, id));
            var ids = openSearchService.search();
            sqls.add(String.format(updateSql(), dbConfig.getDataSet(), ids.get(0)));
            var newUpdate = String.format(updateSql(), dbConfig.getDataSet(), "asdf");
            sqls.add(String.format(newUpdate, dbConfig.getDataSet(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataSet(), ids.get(0)));

            assertFalse(dataLayerService.executeBatch(sqls.toArray(new String[0])).getStatus());
        });
    }

    /**
     * db select
     * openSearchSelect
     */

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

        insertDocuemt(insertSql(in, one, A));
        insertDocuemt(insertSql(in, one, B));
        insertDocuemt(insertSql(in, two, A));
        insertDocuemt(insertSql(in, two, B));
        insertDocuemt(insertSql(out, one, A));
        insertDocuemt(insertSql(out, one, B));
        insertDocuemt(insertSql(out, two, A));
        insertDocuemt(insertSql(out, two, B));
        sleep();

        var filter = dataLayerService.search("in", null, null, "testUser").getFilters();

        assertEquals(2, filter.getTypeFilter(0).getCount());
        assertEquals(4, filter.getNameFilter(0).getCount());
    }

    @Test
    void searchWithInputADetail() {
        var in = "in";
        var out = "out";
        var one = "1";
        var two = "2";
        var A = "a";
        var B = "b";

        insertDocuemt(insertSql(in, one, A));
        insertDocuemt(insertSql(in, one, B));
        insertDocuemt(insertSql(in, two, A));
        insertDocuemt(insertSql(in, two, B));
        insertDocuemt(insertSql(out, one, A));
        insertDocuemt(insertSql(out, one, B));
        insertDocuemt(insertSql(out, two, A));
        insertDocuemt(insertSql(out, two, B));

        var detail = DataSet.newBuilder().setType("1").build();

        var filter = dataLayerService.search("in", detail, null, "testUser").getFilters();

        assertEquals(2, filter.getTypeFilter(0).getCount());
        assertEquals(2, filter.getNameFilter(0).getCount());
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

        insertDocuemt(insertSql2(in, one, A));
        insertDocuemt(insertSql2(in, one, B));
        insertDocuemt(insertSql2(in, one, C));
        insertDocuemt(insertSql2(in, two, A));
        insertDocuemt(insertSql2(in, two, B));
        insertDocuemt(insertSql2(out, one, A));
        insertDocuemt(insertSql2(out, one, B));
        insertDocuemt(insertSql2(out, two, A));
        insertDocuemt(insertSql2(out, two, B));
        sleep();

        var detail = DataSet.newBuilder().setType("1").build();
        var filter = Filter.newBuilder().addFormat("a").addFormat("zxcv").build();

        var filters = dataLayerService.search("in", detail, filter, "testUser").getFilters();

        assertEquals(2, filters.getNameFilter(0).getCount());
        assertEquals(2, filters.getTypeFilter(0).getCount());
        assertEquals(1, filters.getFormatFilter(0).getCount());
        assertEquals(1, filters.getFormatFilter(1).getCount());
    }

    @DisplayName("recent Search success test")
    @Test
    void recentSearch() {
        dataLayerService.search("a", null, null, "testUser");
        sleep();
        dataLayerService.search("b", null, null, "testUser");
        sleep();
        dataLayerService.search("c", null, null, "testUser");
        sleep();
        dataLayerService.search("d", null, null, "testUser");
        sleep();
        dataLayerService.search("e", null, null, "testUser");
        sleep();
        dataLayerService.search("f", null, null, "testUser");
        sleep();
        dataLayerService.search("g", null, null, "testUser");
        sleep();
        dataLayerService.search("h", null, null, "testUser");
        sleep();
        dataLayerService.search("i", null, null, "testUser");
        sleep();
        dataLayerService.search("j", null, null, "testUser");
        sleep();
        dataLayerService.search("k", null, null, "testUser");
        sleep();
        var recent = dataLayerService.recentSearch("testUser");
        assertEquals(11, recent.getSearchedCount());
    }

    @Test
    void healthCheck() {
        assertTrue(dataLayerService.healthCheck().getStatus());
    }

    void insertDocument() {
        preInserted = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(String.format(insertSql(), dbConfig.getDataSet(), preInserted, preInserted, preInserted)));
    }

    void insertDocuemt(String insertSql) {
        var id = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(String.format(insertSql, dbConfig.getDataSet(), id, id, id)));
    }

    void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}