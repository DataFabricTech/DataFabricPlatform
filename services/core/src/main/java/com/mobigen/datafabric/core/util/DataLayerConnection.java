package com.mobigen.datafabric.core.util;

import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.DataLayerGRPCServiceGrpc;
import com.mobigen.datafabric.share.protobuf.Utilities;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DataLayer 서비스와 통신 하기 위한 정의부
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class DataLayerConnection {
    private final DataLayerGRPCServiceGrpc.DataLayerGRPCServiceBlockingStub stub;
    private Boolean test;

    public DataLayerConnection() {
        this(false);
    }

    public DataLayerConnection(Boolean test) {
        this.test = test;
        if (this.test) {
            stub = null;
        } else {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.107.28", 123)
                    .usePlaintext()
                    .build();
            stub = DataLayerGRPCServiceGrpc.newBlockingStub(channel);
        }
    }

    public DataLayer.ResExecute getStorage(String sql) {
        log.info("sql: " + sql);
        return DataLayer.ResExecute.newBuilder()
                .setData(DataLayer.ResExecute.Data.newBuilder()
                        .setTable(DataLayer.Table.newBuilder()
                                .addAllColumns(List.of(
                                        DataLayer.Column.newBuilder()
                                                .setColumnName("id")
                                                .setType(Utilities.DataType.STRING)
                                                .build(),
                                        DataLayer.Column.newBuilder()
                                                .setColumnName("adaptor_id")
                                                .setType(Utilities.DataType.STRING)
                                                .build(),
                                        DataLayer.Column.newBuilder()
                                                .setColumnName("name")
                                                .setType(Utilities.DataType.STRING)
                                                .build(),
                                        DataLayer.Column.newBuilder()
                                                .setColumnName("user_desc")
                                                .setType(Utilities.DataType.STRING)
                                                .build(),
                                        DataLayer.Column.newBuilder()
                                                .setColumnName("status")
                                                .setType(Utilities.DataType.STRING)
                                                .build()
                                ))
                                .addAllRows(List.of(
                                        DataLayer.Row.newBuilder()
                                                .addAllCell(List.of(
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(0)
                                                                .setStringValue("11761353-2963-4963-995d-844e24b80d93")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(1)
                                                                .setStringValue("9c761353-2963-4963-995d-844e24b80d93")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(2)
                                                                .setStringValue("postgresql 연결정보 1")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(3)
                                                                .setStringValue("postgresql 연결정보 1 설명")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(4)
                                                                .setStringValue("CONNECTED")
                                                                .build()
                                                ))
                                                .build(),
                                        DataLayer.Row.newBuilder()
                                                .addAllCell(List.of(
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(0)
                                                                .setStringValue("22761353-2963-4963-995d-844e24b80d93")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(1)
                                                                .setStringValue("9c761353-2963-4963-995d-844e24b80d93")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(2)
                                                                .setStringValue("postgresql 연결정보 2")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(3)
                                                                .setStringValue("postgresql 연결정보 2 설명")
                                                                .build(),
                                                        DataLayer.Cell.newBuilder()
                                                                .setColumnIndex(4)
                                                                .setStringValue("DISCONNECTED")
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build())
                        .build())
                .build();
    }

    public DataLayer.ResExecute execute(String sql) {
        log.info("sql: " + sql);
        if (test) {
            return DataLayer.ResExecute.newBuilder().build();
        }
        return stub.execute(DataLayer.ReqExecute.newBuilder()
                .setSql(sql)
                .build());
    }

    public DataLayer.ResBatchExecute executeBatch(String... sqlList) {
        log.info("sql list: " + String.join(";", sqlList));
        if (test) {
            return DataLayer.ResBatchExecute.newBuilder()
                    .addAllData(List.of(1, 2, 3))
                    .build();
        }
        return stub.executeBatch(DataLayer.ReqBatchExecute.newBuilder()
                .addAllSql(List.of(sqlList))
                .build());
    }

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

//    public static DataLayer.QueryGRPCResponseMessage getData(String sql) {
//        var req = DataLayer.QueryGRPCRequestMessage.newBuilder()
//                .setQuery(sql)
//                .build();
//        return DataLayer.QueryGRPCResponseMessage.newBuilder()
//                .addAllColumn(List.of(
//                        DataLayer.Column.newBuilder().setColumnName("id").setType("string").build(),
//                        DataLayer.Column.newBuilder().setColumnName("name").setType("string").build()
//                ))
//                .addAllRows(List.of(
//                        DataLayer.Rows.newBuilder().addAllRow(
//                                List.of(
//                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
//                                        DataLayer.Cell.newBuilder().setStringValue("mysql").build()
//                                )
//                        ).build(),
//                        DataLayer.Rows.newBuilder().addAllRow(
//                                List.of(
//                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
//                                        DataLayer.Cell.newBuilder().setStringValue("postgresql").build()
//                                )
//                        ).build()
//                ))
//                .build();
//
//    }
}
