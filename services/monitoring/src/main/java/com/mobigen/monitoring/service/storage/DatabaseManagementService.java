package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.service.query.QueryLoader;
import com.mobigen.monitoring.vo.Queries;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.vo.TableInfo;
import com.mobigen.monitoring.vo.TableRowInfo;
import com.zaxxer.hikari.HikariDataSource;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.swing.text.TableView;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mobigen.monitoring.vo.Queries.*;
import static java.lang.String.format;

@Service
@Slf4j
public class DatabaseManagementService {

    public boolean testDatabaseConnection(DatabaseConnectionRequest request) {
        if ("minio".equalsIgnoreCase(request.getDbType())) {
            return testMinioConnection(request);
        }

        DataSource dataSource = createDataSource(request);

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);  // 2초 내에 응답 확인
        } catch (SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
            return false;  // 연결 실패
        }
    }

    private DataSource createDataSource(DatabaseConnectionRequest request) {
        HikariDataSource dataSource = new HikariDataSource();

        String jdbcUrl = buildJdbcUrl(request);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(request.getUsername());
        dataSource.setPassword(request.getPassword());
        dataSource.setDriverClassName(getDriverClassName(request.getDbType()));

        dataSource.setMaximumPoolSize(2);  // 연결 풀 최소화
        dataSource.setConnectionTimeout(2000);  // 2초 안에 연결 시도
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(60000);

        return dataSource;
    }

    private String buildJdbcUrl(DatabaseConnectionRequest request) {
        String baseJdbcUrl;
        switch (request.getDbType().toLowerCase()) {
            case "postgres":
                baseJdbcUrl = "jdbc:postgresql://" + request.getHost() + ":" + request.getPort();
                return request.getDatabaseName() == null || request.getDatabaseName().isEmpty()
                        ? baseJdbcUrl + "/" + request.getUsername()
                        : baseJdbcUrl + "/" + request.getDatabaseName();

            case "mysql":
                baseJdbcUrl = "jdbc:mysql://" + request.getHost() + ":" + request.getPort();
                return request.getDatabaseName() == null || request.getDatabaseName().isEmpty()
                        ? baseJdbcUrl + "/information_schema"
                        : baseJdbcUrl + "/" + request.getDatabaseName();

            case "mariadb":
                baseJdbcUrl = "jdbc:mariadb://" + request.getHost() + ":" + request.getPort();
                return request.getDatabaseName() == null || request.getDatabaseName().isEmpty()
                        ? baseJdbcUrl + "/information_schema"
                        : baseJdbcUrl + "/" + request.getDatabaseName();

            case "oracle":
                baseJdbcUrl = "jdbc:oracle:thin:@" + request.getHost() + ":" + request.getPort();
                return request.getDatabaseName() == null || request.getDatabaseName().isEmpty()
                        ? baseJdbcUrl + "/XEPDB1"  // 기본 PDB 사용
                        : baseJdbcUrl + "/" + request.getDatabaseName();

            default:
                throw new IllegalArgumentException("Unsupported database type: " + request.getDbType());
        }
    }

    public List<String> getDatabases(DatabaseConnectionRequest request) {
        String query = null;
        List<String> databases = new ArrayList<>();

        if (request.getDbType().equalsIgnoreCase("postgres")) {
            query = POSTGRES_GET_DATABASES.getQueryString();

            for (Map<String, Object> response : executeQuery(request, query)) {
                databases.add((String) response.get("datname"));
            }
        } else if (request.getDbType().equalsIgnoreCase("mysql") ||
                request.getDbType().equalsIgnoreCase("mariadb") ||
                request.getDbType().equalsIgnoreCase("oracle")) {
            query = RDB_GET_DATABASES.getQueryString();

            for (Map<String, Object> response : executeQuery(request, query)) {
                databases.add((String) response.get("Database"));
            }
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + request.getDbType());
        }

        return databases;
    }

    public Map<String, List<String>> getTables(DatabaseConnectionRequest request) {
        String query = GET_TABLES.getQueryString();
        final List<Map<String, Object>> data = executeQuery(request, query);
        Map<String, List<String>> schemaMap = new HashMap<>();

        for (Map<String, Object> entry : data) {
            String schema = (String) entry.get("table_schema");
            String tableName = (String) entry.get("table_name");

            schemaMap.computeIfAbsent(schema, k -> new ArrayList<>()).add(tableName);
        }

        return schemaMap;
    }

    @Transactional
    public Object getRows(DatabaseConnectionRequest request) {
        String query = GET_ALL_TABLE_ROWS.getQueryString();
        List<String> queries = new ArrayList<>();

        final List<Map<String, Object>> maps = executeQuery(request, query);

        for (Map<String, Object> entry : maps) {
            queries.add((String) entry.get("query"));
        }

        DataSource dataSource = createDataSource(request);
        Connection conn = DataSourceUtils.getConnection(dataSource);

        Map<String, Object> result = new HashMap<>();
        List<Object> results = new ArrayList<>();

        try {
            conn.setAutoCommit(false); // 수동 커밋 모드 (트랜잭션 유지)

            for (String queryString : queries) {
                try (PreparedStatement stmt = conn.prepareStatement(queryString);
                     ResultSet rs = stmt.executeQuery()) { // SELECT 실행
                    List<Map<String, Object>> resultSetData = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }

                        if (resultSetData.size() <= 1) {
                            results.add(row);
                        }
                    }
                }
            }

            conn.commit(); // 필요하면 유지
        } catch (SQLException e) {
            try {
                conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource); // Connection 반환
        }

        return results;
    }

    public List<Map<String, Object>> executeQuery(DatabaseConnectionRequest request, String query) {
        DataSource dataSource = createDataSource(request);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        return jdbcTemplate.queryForList(query);
    }

    private String getDriverClassName(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "postgres" -> "org.postgresql.Driver";
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            case "mariadb" -> "org.mariadb.jdbc.Driver";
            case "oracle" -> "oracle.jdbc.OracleDriver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private boolean testMinioConnection(DatabaseConnectionRequest request) {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://" + request.getHost() + ":" + request.getPort())
                    .credentials(request.getUsername(), request.getPassword())
                    .build();

            minioClient.listBuckets();  // Minio 연결 테스트
            return true;
        } catch (MinioException | IllegalArgumentException e) {
            System.err.println("Minio 연결 실패: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
