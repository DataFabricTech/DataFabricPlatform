package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.libs.configuration.Config;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.OpenSearchException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;

import static org.junit.jupiter.api.Assertions.*;

class OpenSearchServiceTempTest {
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
    void gptTest() {
        try {
            // 기존 INSERT 쿼리 문자열을 파싱합니다.
            String sqlQuery = "INSERT INTO test_time_table2 (id) VALUES (1235);";
            Insert insertStatement = (Insert) CCJSqlParserUtil.parse(sqlQuery);

            // 추가할 컬럼과 값을 생성합니다.
            Column newColumn = new Column("new_column");
            Expression newValue = new StringValue("new_value");

            // 새로운 컬럼과 값을 추가합니다.
            insertStatement.getColumns().add(newColumn);
            insertStatement.setWithItemsList(new ExpressionList(List.of(newValue)));

            // 수정된 SQL 문장을 다시 문자열로 변환합니다.
            String newSqlQuery = insertStatement.toString();
            System.out.println(newSqlQuery);

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testExpression() {
        var s = "name = 'asdf'";
        try {
            System.out.println("++++++");
            var expression = CCJSqlParserUtil.parseExpression(s);
            System.out.println("++++++");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void postgreSQLInsertTest() {
        var sqls = new String[]{"insert into test_time_table2 (id,da) values (1236,'c');", "insert into test (id, b) values (1237, 'c')"};
        try (
                var conn = getConnection();
                var stmt = conn.createStatement()
        ) {
            for (var sql : sqls) {
                stmt.addBatch(sql);
            }
            stmt.executeBatch();
            conn.commit();

        } catch (Exception e) {
            System.out.println(e.getClass());
            System.out.println("======");
            System.out.println(e);
            System.out.println("===");
            System.out.println(e.getCause());
            System.out.println("===");
            System.out.println(e.getMessage());
        }
    }

    @Test
    void parserTest() {
        var insert = "insert into test_table (last_name, email, yee_id) values ('ye', \"ye@ye\", 2);";
        try {
            var statement = (Insert) CCJSqlParserUtil.parse(insert);
            System.out.println(statement);
        } catch (Exception e) {

        }
    }

    @Test
    void insertParser() {
        var insert = "insert into test_table (last_name, email, yee_id) values ('ye', \"ye@ye\", 2);";
        try {
            var statement = (Insert) CCJSqlParserUtil.parse(insert);
            ExpressionList<Column> columns = statement.getColumns();
            ExpressionList<?> values = statement.getValues().getExpressions();
            for (int i = 0; i < values.size(); i++) {
                System.out.println(columns.get(i).getColumnName());
                System.out.printf(String.format("column : %s, value: %s\n", columns.get(i), values.get(i)));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void deleteParser() {
        var delete = "DELETE from del where yee_id =123 AND yee_id=1234 AND yee_id=234;";
        try {
            var statement = (Delete) CCJSqlParserUtil.parse(delete);
            statement.getWhere(); // -> yee_id = 123
            System.out.println(statement.getTable().toString());
            var st = new StringTokenizer("yee_id =123", "=");
            System.out.println(st.nextToken());
            System.out.println(st.nextToken());
            st = new StringTokenizer("yee_id=123", "=");
            System.out.println(st.nextToken());
            System.out.println(st.nextToken());
            st = new StringTokenizer("yee_id = 123", "=");
            System.out.println(st.nextToken());
            System.out.println(st.nextToken());

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void occured() {
        var s = "asdf";
        try {
            var statement = (Insert) CCJSqlParserUtil.parse(s);
        } catch (JSQLParserException e) {
            System.out.println(e);
            System.out.println("=");
            System.out.println(e.getMessage());
            System.out.println("==");
            System.out.println(e.getCause());
            System.out.println("==");
            System.out.println(e.getCause().toString());
        }
    }

    @Test
    void updateParser() {
        var update = "update upd set email = 'yee@yee', asdf = 'zxcv' where yee_id = 123;";
        try {
            var statement = (Update) CCJSqlParserUtil.parse(update);
            for (var set : statement.getUpdateSets()) {
                var columns = set.getColumns();
                var values = set.getValues();
                for (int i = 0; i < values.size(); i++) {
                    System.out.printf("%s %s\n", columns.get(i), values.get(i));

                }
            }
            System.out.println("=======");

            update = "update upd set (email, asdf) = ('yee@yee', 'zxcv') where yee_id = 123;";
            statement = (Update) CCJSqlParserUtil.parse(update);
            for (var set : statement.getUpdateSets()) {
                var columns = set.getColumns();
                var values = set.getValues();
                for (int i = 0; i < values.size(); i++) {
                    System.out.printf("%s %s\n", columns.get(i), values.get(i));

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void testException() {
        try {
            throwss();
        } catch (OpenSearchException e) {
            System.out.println("come here2");
        }
    }

    void throwss() throws OpenSearchException {
        try {
            throw new OpenSearchException(
                    new ErrorResponse.Builder().error(
                                    e -> e.type("Insert").reason("OpenSearch Insert Fail"))
                            .status(500).build());
        } catch (OpenSearchException e) {
            System.out.println("come here 1");
            throw e;
        }
    }

    @Test
    void parseGetTableName() {
        try {
            var insert = "insert into in (last_name, email, yee_id) values ('ye', 'ye@ye', 2);";
            var select = "select * from sele where last_name = 'yee';";
            var update = "update upd set email = 'yee@yee' where yee_id = 123;";
            var delete = "DELETE from del where yee_id =123;";

            var statement = CCJSqlParserUtil.parse(insert);
            System.out.println(statement.toString());
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                var tableName = insertStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement = CCJSqlParserUtil.parse(select);
            System.out.println(statement.toString());
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                var tableName = ((Table) selectStatement.getPlainSelect().getFromItem()).getName();
                System.out.println(tableName);
            }

            statement = CCJSqlParserUtil.parse(delete);
            System.out.println(statement.toString());
            if (statement instanceof Delete) {
                Delete deleteStatement = (Delete) statement;
                var tableName = deleteStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement = CCJSqlParserUtil.parse(update);
            System.out.println(statement.toString());
            if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                var tableName = updateStatement.getTable().getName();
                System.out.println(tableName);
            }

            statement = CCJSqlParserUtil.parse(update);
            System.out.println(statement.toString());
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

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = "jdbc:postgresql://192.168.106.104:5432/testdb";
        String user = "testuser";
        String pw = "testUser";

        try {
            Class.forName("org.postgresql.Driver");
            var conn = DriverManager.getConnection(url, user, pw);
            conn.setAutoCommit(false);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            throw e;
        }
    }
}