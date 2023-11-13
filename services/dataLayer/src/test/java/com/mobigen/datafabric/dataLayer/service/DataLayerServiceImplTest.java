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

            if (!id.isEmpty())
                dataLayerService.executeBatch(makeBatchReq(storageSqls));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            sleep();
            var dataModel = portalService.searchAllDataModel();
            var id = dataModel.isEmpty() ? "" : dataModel.get(0);

            var dataModelSqls = new String[]{
                    String.format("delete from data_user_comment where data_model_id= '%s'", id),
                    String.format("delete from data_tag where id ='%s'", id),
                    String.format("delete from data_model_schema where id = '%s'", id),
                    String.format("delete from data_metadata where id = '%s'", id),
                    String.format("delete from data_location where id ='%s'", id),
                    String.format("delete from data_model where id = '%s'", id)
            };

            if (!id.isEmpty())
                dataLayerService.executeBatch(makeBatchReq(dataModelSqls));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // 검색어 제거
        try {
            portalService.deleteSearchesDocument("testUser");
        } catch (Exception e) {
            fail(e.getMessage());
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
        return String.format(insertSql, dbConfig.getDataModel(), id, id);
    }


    String insertSql2(String name, String type, String format) {
        var id = UUID.randomUUID().toString();
        var insertSql = "insert into %s " +
                "(name, description, datatype, dataformat, size, sample_data_id ,status, " +
                "cache_enable, cache_status, created_by, updated_by)" +
                " values ('" + name + "', 'insert_description', '" + type + "', '" + format + "', 1000, " +
                "'insert_sampledata_id','success', false, 'fail', 'created_by', '%s')";
        return String.format(insertSql, dbConfig.getDataModel(), id);
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

    @DisplayName("Execute Insert")
    @Test
    void insertTest() {
        var id = UUID.randomUUID().toString();
        var insertSql = String.format(insertSql(), dbConfig.getDataModel(), id, id, id);
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
        var select = String.format(selectSql(), dbConfig.getDataModel(), preInserted);
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
            var ids = portalService.searchAllDataModel();
            var update = String.format(updateSql(), dbConfig.getDataModel(), ids.get(0));
            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(update)).getCode());
        });

        var select = String.format(selectSql(), dbConfig.getDataModel(), preInserted);

        assertEquals("updated_name", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(1).getStringValue());
        assertEquals("updated_description", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(2).getStringValue());
    }

    @DisplayName("Execute update fail")
    @Test
    void updateFailTest() {
        var update = String.format(updateSql(), dbConfig.getDataModel(), "wrongid");
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(update)).getCode());
    }

    @DisplayName("Execute delete")
    @Test
    void deleteTest() {
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataModel();
            var delete = String.format(deleteSql(), dbConfig.getDataModel(), ids.get(0));
            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(delete)).getCode());
        });
    }

    @DisplayName("Execute delete fail")
    @Test
    void deleteFailTest() {
        var delete = String.format(deleteSql(), dbConfig.getDataModel(), "worngId");
        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.execute(makeReq(delete)).getCode());
    }

    @DisplayName("Execute batch Insert")
    @Test
    void insertBatchTest() {
        var id = UUID.randomUUID().toString(); // storage_id

        var sqls = new String[]{
                "insert into DataStorageType (name) values ('testStorageTypeName')",
                String.format("insert into DataStorageAdaptor (id, storage_type_name) values ('%s', 'testStorageTypeName')", id),
                String.format("insert into DataStorage (id,adaptor_id, name, user_desc, created_by, status, created_at) values ('%s', '%s', 'testName','testDesc','%s', 'INIT', '2023-11-06 01:31:46.002878')", id, id, id),
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
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataModel(), id, id, id),
                "insert into failTable value (1,2,3);"};

        assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
    }

    @DisplayName("Execute Batch Insert with select fail")
    @Test
    void insertBatchFailWithSelectTest() {
        var id = UUID.randomUUID().toString();
        var sqls = new String[]{String.format(insertSql(), dbConfig.getDataModel(), id, id, id),
                String.format(selectSql(), dbConfig.getDataModel(), preInserted)};

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
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                IDs.add(id);
                sqls.add(String.format(updateSql(), dbConfig.getDataModel(), id));
            }

            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });

        for (var id : IDs) {
            var select = String.format(selectIDSql(), dbConfig.getDataModel(), id);

            assertEquals("updated_name", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(1).getStringValue());
            assertEquals("updated_description", dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(2).getStringValue());
        }

        var select = "select count(*) from data_model_test;";
        assertEquals(2, dataLayerService.execute(makeReq(select)).getData().getTable().getRows(0).getCell(0).getInt64Value());
    }

    @DisplayName("Execute update Batch fail")
    @Test
    void updateBatchFailTest() {
        insertDocument();
        insertDocument();
        sleep();

        assertDoesNotThrow(() -> {
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataModel(), ids.get(0)));
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
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(updateSql(), dbConfig.getDataModel(), ids.get(0)));
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
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            for (var id : ids) {
                sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), id));
            }

            assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());

        });

        var select = "select count(*) from data_model_test;";
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
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), ids.get(0)));
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
            var ids = portalService.searchAllDataModel();
            var sqls = new LinkedList<String>();
            sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), ids.get(0)));
            sqls.add("select * from data_model_test;");
            sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), ids.get(1)));

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
            sqls.add(String.format(insertSql(), dbConfig.getDataModel(), id, id, id));
            var ids = portalService.searchAllDataModel();
            sleep();
            sqls.add(String.format(updateSql(), dbConfig.getDataModel(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), ids.get(0)));

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
            sqls.add(String.format(insertSql(), dbConfig.getDataModel(), id, id, id));
            var ids = portalService.searchAllDataModel();
            sqls.add(String.format(updateSql(), dbConfig.getDataModel(), ids.get(0)));
            var newUpdate = String.format(updateSql(), dbConfig.getDataModel(), "asdf");
            sqls.add(String.format(newUpdate, dbConfig.getDataModel(), ids.get(0)));
            sqls.add(String.format(deleteSql(), dbConfig.getDataModel(), ids.get(0)));

            assertEquals(ResponseCode.UNKNOWN.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls.toArray(new String[0]))).getCode());
        });
    }

    @DisplayName("Search Keyword")
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


    @DisplayName("Search keyword with details")
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


    @DisplayName("Search keyword with details, filters")
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

    @DisplayName("data model insert test")
    @Test
    void insertBatchWithDataModelTest() {
        var id = "3a883d0e-c2bd-47de-b16a-58d1d22c0572";
        var id2 = "b5839838-0b4d-4414-a5c6-f45cf13eafc7";

        var sqls = new String[]{
                String.format("insert into data_model(id, name, description, type, format, status, created_at, created_by) values ('%s', 'testa', 'testa testa', 'STRUCTURED', 'TABLE', 'CONNECTED', '2023-11-09 09:40:59.721', gen_random_uuid ())", id),
                String.format("insert into data_location(id, storage_id, path, name) values ('%s', '1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'testdb', 'testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'NAME', 'testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'OWNER', 'testUser')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'DESC', 'testa testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'FORMAT', 'TABLE')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'COLUMNS', '2')", id),
                String.format("insert into data_model_schema(id, ordinal_position, column_name, data_type, length, \"default\", description) values ('%s', 1, 'id', 'integer', 32, '', '')", id),
                String.format("insert into data_model_schema(id, ordinal_position, column_name, data_type, length, \"default\", description) values ('%s', 2, 'db', 'text', 1073741824, '', '')", id),
                String.format("insert into data_tag (id, tag) values ('%s', 'tag_one')", id),
                String.format("insert into data_tag (id, tag) values ('%s', 'tag_two')", id),
                String.format("insert into data_user_comment (id, data_model_id, user_id, rating, comment, \"time\") values ('%s', '%s', '336c8550-a7f8-4c96-9d17-cd10770ace87', '5', 'comment', NOW())", id2, id)
        };

        assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.executeBatch(makeBatchReq(sqls)).getCode());
        sleep();

        assertEquals(1, (portalService.search(makeReq("testa", null, null, null)).getData().getSearchResponse()).getPageable().getPage().getTotalSize());
    }

    @DisplayName("data model update test")
    @Test
    void updateDataModelTest() {
        var id = "3a883d0e-c2bd-47de-b16a-58d1d22c0572";
        var id2 = "b5839838-0b4d-4414-a5c6-f45cf13eafc7";

        var sqls = new String[]{
                String.format("insert into data_model(id, name, description, type, format, status, created_at, created_by) values ('%s', 'testa', 'testa testa', 'STRUCTURED', 'TABLE', 'CONNECTED', '2023-11-09 09:40:59.721', gen_random_uuid ())", id),
                String.format("insert into data_location(id, storage_id, path, name) values ('%s', '1b6c8550-a7f8-4c96-9d17-cd10770ace87', 'testdb', 'testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'NAME', 'testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'OWNER', 'testUser')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'DESC', 'testa testa')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'FORMAT', 'TABLE')", id),
                String.format("insert into data_metadata(id, is_system, \"key\", \"value\") values ('%s', true, 'COLUMNS', '2')", id),
                String.format("insert into data_model_schema(id, ordinal_position, column_name, data_type, length, \"default\", description) values ('%s', 1, 'id', 'integer', 32, '', '')", id),
                String.format("insert into data_model_schema(id, ordinal_position, column_name, data_type, length, \"default\", description) values ('%s', 2, 'db', 'text', 1073741824, '', '')", id),
                String.format("insert into data_tag (id, tag) values ('%s', 'tag_one')", id),
                String.format("insert into data_tag (id, tag) values ('%s', 'tag_two')", id),
                String.format("insert into data_user_comment (id, data_model_id, user_id, rating, comment, \"time\") values ('%s', '%s', '336c8550-a7f8-4c96-9d17-cd10770ace87', '5', 'comment', NOW())", id2, id)
        };
        dataLayerService.executeBatch(makeBatchReq(sqls));
        sleep();

        var sql = String.format("update data_model set name = 'updated_name', last_modified_at = '2023-11-13 09:40:59.721', last_modified_by = '%s' where id = '%s'", id2, id);
        assertEquals(ResponseCode.SUCCESS.getValue(), dataLayerService.execute(makeReq(sql)).getCode());

        sleep();
        System.out.println(portalService.search(makeReq("updated_name", null, null, null)));
    }

    void insertDocument() {
        preInserted = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql(), dbConfig.getDataModel(), preInserted, preInserted, preInserted))));
    }


    void insertDocument(String insertSql) {
        var id = UUID.randomUUID().toString();
        assertDoesNotThrow(() ->
                dataLayerService.execute(makeReq(String.format(insertSql, dbConfig.getDataModel(), id, id, id))));
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