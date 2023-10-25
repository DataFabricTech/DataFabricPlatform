package com.mobigen.datafabric.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class JdbcConnector implements AutoCloseable {
    private String url;
    private Connection connection;
    private Cursor cursor;

    public JdbcConnector(String urlFormat, Map<String, String> basicOptions) {
        url = StringSubstitutor.replace(urlFormat, basicOptions, "{", "}");
    }

    public JdbcConnector connect(Properties options) {
        try {
            connection = DriverManager.getConnection(url, options);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return this;
    }


    public JdbcConnector connect() {
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
