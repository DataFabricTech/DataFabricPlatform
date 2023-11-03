package com.mobigen.datafabric.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

/**
 * 저장소로 등록된 외부 저장소에 JDBC 를 이용하여 연결하기 위한 Connector 클래스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class JdbcConnector implements AutoCloseable {
    private String url;
    private Connection connection;
    private Cursor cursor;

    public JdbcConnector(String urlFormat, Map<String, Object> basicOptions) {
        url = StringSubstitutor.replace(urlFormat, basicOptions, "{", "}");
    }

    public JdbcConnector connect(Properties options) throws SQLException {
        // TODO: driver 받아서 연결해야함. 예외처리 필수
        connection = DriverManager.getConnection(url, options);
        return this;
    }


    public JdbcConnector connect() throws SQLException {
        return connect(null);
    }

    public ResultSet getTables() {
        try {
            var metadata = connection.getMetaData();
            var types = new String[]{"TABLE"};
            return metadata.getTables(null, null, null, types);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Cursor cursor() {
        try {
            cursor = new Cursor(connection.createStatement());
            return cursor;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (cursor != null) {
                cursor.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Cursor {
        private final Statement statement;

        private Cursor(Statement statement) {
            this.statement = statement;
        }

        public void execute(String sql) {
            try {
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public ResultSet getResultSet() {
            try {
                return statement.getResultSet();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
