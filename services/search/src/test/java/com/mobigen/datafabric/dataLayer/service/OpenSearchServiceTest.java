package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.config.DBConfig;
import com.mobigen.libs.configuration.Config;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenSearchServiceTest {
    AppConfig appConfig = new AppConfig();

    @Test
    void configTest2() {
        var dbConfig = appConfig.dbConfig();
        System.out.println("======");
        System.out.println(dbConfig.getUsername());
        System.out.println("======");
        assertEquals(dbConfig.getUsername(), "testuser");
    }
    @Test
    void configTest() {
        var config = new Config().getConfig();
        System.out.println(config.getInt("test"));
        assertEquals(123, config.getInt("test"));
    }

    @Test
    void insertParser() {
        var insert = "insert into in (last_name, email, yee_id) values ('ye', \"ye@ye\", 2);";
        try {
            var statement = (Insert) CCJSqlParserUtil.parse(insert);
            var columns = statement.getColumns();
            var values = statement.getValues().getExpressions();
            for (int i = 0; i < values.size(); i++) {
                System.out.printf(String.format("column : %s, value: %s\n",columns.get(i), values.get(i)));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void parseGetTableName() {
        try {
            var insert = "insert into in (last_name, email, yee_id) values ('ye', 'ye@ye', 2);";
            var select = "select * from sele where last_name = 'yee';";
            var update = "update upd set email = 'yee@yee' where yee_id = 123;";
            var delete = "DELETE from del where yee_id =123;";

            var statement =  CCJSqlParserUtil.parse(insert);
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                var tableName = insertStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement =  CCJSqlParserUtil.parse(select);
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                var tableName = ((Table)selectStatement.getPlainSelect().getFromItem()).getName();
                System.out.println(tableName);
            }

            statement =  CCJSqlParserUtil.parse(delete);
            if (statement instanceof Delete) {
                Delete deleteStatement = (Delete) statement;
                var tableName = deleteStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement =  CCJSqlParserUtil.parse(update);
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                var tableName = updateStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement =  CCJSqlParserUtil.parse(update);
            switch (statement.getClass().getSimpleName()) {
                case "PlainSelect":
                    System.out.println("select");
                    break;
                case "Insert":
                    System.out.println("insert");
                    break;
                case "Delete":
                    System.out.println("delete");
                    break;
                case "Update":
                    System.out.println("update");
                    break;
                default:
                    System.out.println("Unknown");
                    break;
            }


        } catch (Exception e) {
            System.out.println(e);

        }
    }

    @Test
    void search() {
    }

    @Test
    void mainQueryBuilder() {
    }

    @Test
    void mustQueryBuilder() {
    }

    @Test
    void shouldQueryBuilder() {
    }

    @Test
    void createIndex() {
    }

    @Test
    void searchDocument() {
    }

    @Test
    void insertDocument() {
    }

    @Test
    void updateDocument() {
    }

    @Test
    void deleteDocument() {
    }

    @Test
    void getMustQuery() {
    }

    @Test
    void getShouldQuery() {
    }

    @Test
    void testSearch() {
    }

    @Test
    void testMainQueryBuilder() {
    }

    @Test
    void testMustQueryBuilder() {
    }

    @Test
    void testShouldQueryBuilder() {
    }

    @Test
    void getRecentSearch() {
    }

    @Test
    void testCreateIndex() {
    }

    @Test
    void testSearchDocument() {
    }

    @Test
    void testInsertDocument() {
    }

    @Test
    void insertDocuemnt() {
    }

    @Test
    void testUpdateDocument() {
    }

    @Test
    void testDeleteDocument() {
    }

    @Test
    void syncDocument() {
    }

    @Test
    void testGetMustQuery() {
    }

    @Test
    void testGetShouldQuery() {
    }

    @Test
    void getOpenSearchRepository() {
    }
}