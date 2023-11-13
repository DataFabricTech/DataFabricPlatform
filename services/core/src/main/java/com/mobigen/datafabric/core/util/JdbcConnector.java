package com.mobigen.datafabric.core.util;

import com.mobigen.libs.configuration.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final String url;
    private final Properties advancedOptions;
    private final String driver;
    private final String driverFilePath;
    private Connection connection;
    private Cursor cursor;
    private Config config = new Config();

    private JdbcConnector(Builder builder) {
        var urlFormat = Objects.requireNonNull(builder.urlFormat);
        url = StringSubstitutor.replace(urlFormat, builder.urlOptions, "{", "}");
        advancedOptions = Objects.requireNonNull(builder.advancedOptions);
        driver = Objects.requireNonNull(builder.driver);
        driverFilePath = builder.driverFilePath;
    }

    public JdbcConnector connect() throws SQLException, ClassNotFoundException {
        // TODO: driver 받아서 연결해야함. 예외처리 필수
        if (driverFilePath != null) {
            var loader = new DynamicJarLoader(config.getString("core.driver.path"));
            loader.load(driverFilePath);
            var instance = loader.newInstance(driver);
            if (instance == null) {
                throw new RuntimeException("Fail to load driver");
            }
            log.info("Success to load driver. " + driver);
            connection = ((Driver) instance).connect(url, advancedOptions);
        } else {
            // TODO: driver path 없는 경우 에러처리 로 변경 필요
            Class.forName(driver);
            log.info("Success to load driver. " + driver);
            connection = DriverManager.getConnection(url, advancedOptions);
        }
        log.info("Success to connection to " + url);
        connection.setAutoCommit(false);
        return this;
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

    public ResultSet getMetadata(String schemaPattern, String tableNamePattern) {
        if (tableNamePattern == null) {
            tableNamePattern = "%";
        }
        try {
            return connection.getMetaData().getTables(null, schemaPattern, tableNamePattern, null);
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

    public static class Builder {
        private String urlFormat;
        private final Map<String, Object> urlOptions = new HashMap<>();
        private final Properties advancedOptions = new Properties();
        private String driver;
        private String driverFilePath;

        public Builder withUrlFormat(String urlFormat) {
            this.urlFormat = urlFormat;
            return this;
        }

        public Builder withUrlOptions(Map<String, Object> options) {
            this.urlOptions.putAll(options);
            return this;
        }

        public Builder withAdvancedOptions(Map<String, Object> options) {
            this.advancedOptions.putAll(options);
            return this;
        }

        public Builder withAdvancedOptions(Properties options) {
            this.advancedOptions.putAll(options);
            return this;
        }

        public Builder withDriver(String driver) {
            this.driver = driver;
            return this;
        }

        public Builder withDriverFilePath(String path) {
            this.driverFilePath = path;
            return this;
        }

        public JdbcConnector build() {
            return new JdbcConnector(this);
        }
    }
}
