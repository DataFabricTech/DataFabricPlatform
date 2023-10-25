package com.mobigen.datafabric.core.util;

import com.mobigen.libs.grpc.DataLayer;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DataLayerConnection {

    private static Connection getConnection() {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String id = "postgres";
        String password = "test";
        try {
            Class.forName("org.postgresql.Driver");
            log.info("Success to load driver");
            var conn = DriverManager.getConnection(url, id, password);
            conn.setAutoCommit(false);
            log.info("Success to connect db");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public static ResultSet getDataDB(String sql) {
        log.info("sql: " + sql);
        try {
            var conn = getConnection();
            var stmt = conn.createStatement();
            var res = stmt.executeQuery(sql);
            log.info("result: " + res);
            return res;
        } catch (SQLException e) {
            log.error("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<Integer> insertUpdateDataDB(String... sqls) throws SQLException {
        var conn = getConnection();
        List<Integer> res = new ArrayList<>();
        try (var stmt = conn.createStatement()) {
            for (var sql : sqls) {
                log.info("sql: " + sql);
                var result = stmt.executeUpdate(sql);
                res.add(result);
                log.info("result: " + res);
            }
        } catch (SQLException e) {
            log.error("Error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            conn.commit();
        }
        return res;
    }

    public static DataLayer.QueryGRPCResponseMessage getData(String sql) {
        var req = DataLayer.QueryGRPCRequestMessage.newBuilder()
                .setQuery(sql)
                .build();
        return DataLayer.QueryGRPCResponseMessage.newBuilder()
                .addAllColumn(List.of(
                        DataLayer.Column.newBuilder().setColumnName("id").setType("string").build(),
                        DataLayer.Column.newBuilder().setColumnName("name").setType("string").build()
                ))
                .addAllRows(List.of(
                        DataLayer.Rows.newBuilder().addAllRow(
                                List.of(
                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
                                        DataLayer.Cell.newBuilder().setStringValue("mysql").build()
                                )
                        ).build(),
                        DataLayer.Rows.newBuilder().addAllRow(
                                List.of(
                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
                                        DataLayer.Cell.newBuilder().setStringValue("postgresql").build()
                                )
                        ).build()
                ))
                .build();

    }
}
