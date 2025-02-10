package com.mobigen.monitoring.service.storage;

import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.zaxxer.hikari.HikariDataSource;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DatabaseConnectionService {

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

        return dataSource;
    }

    private String buildJdbcUrl(DatabaseConnectionRequest request) {
        String baseJdbcUrl;
        switch (request.getDbType().toLowerCase()) {
            case "postgresql":
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

    private String getDriverClassName(String dbType) {
        switch (dbType.toLowerCase()) {
            case "postgresql":
                return "org.postgresql.Driver";
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "mariadb":
                return "org.mariadb.jdbc.Driver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
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
