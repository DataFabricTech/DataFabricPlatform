package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.core.worker.Job;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class AddStorageTask implements Runnable {
    private final Job job;
    public AddStorageTask(Job job ) {
        this.job = job;
    }

    @Override
    public void run() {
        log.error( "[ New Storage ] : Start" );
        // TODO : Get Storage
        StorageOuterClass.Storage storage = testStorage();

        JdbcConnector connector  = getConnector(storage);
        // TODO : System Meta 없데이트

        // TODO : Setting Check

        // TODO : 실행 결과 저장
//        ds.updateStorage();
        // TODO : 필요한 경우 알림 전송 개발 추가 - 아마도 필요
    }

    public StorageOuterClass.Storage testStorage() {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        storageBuilder
                .setId("test")
                .setName("data-fabric-storage")
                .setDescription("data-fabric-storage")
                .setStorageType("PostgreSQL")
                .setAdaptorId("PostgreSQL")
                .setUrl("jdbc:postgresql://{HOST}:{PORT}/{DATABASE}");

        var optBuilder = StorageCommon.InputField.newBuilder();

        optBuilder.setKey("HOST").setValue("192.168.107.28").build();
        storageBuilder.addBasicOptions(optBuilder.build());

        optBuilder.setKey("PORT").setValue("14632").build();
        storageBuilder.addBasicOptions(1, optBuilder.build());

        optBuilder.setKey("DATABASE").setValue("testdb").build();
        storageBuilder.addBasicOptions(2, optBuilder.build());

        optBuilder.setKey("USER").setValue("testUser").build();
        storageBuilder.addBasicOptions(3, optBuilder.build());

        optBuilder.setKey("PASSWORD").setValue("testUser").build();
        storageBuilder.addBasicOptions(4, optBuilder.build());

        return storageBuilder.build();
    }

    public JdbcConnector getConnector(StorageOuterClass.Storage storage) {
        // JDBC connection
        JdbcConnector conn;
        Properties addition = new Properties();
        Map<String, Object> basic = new HashMap<>();
        for (var op : storage.getBasicOptionsList()) {
            if (op.getKey().equalsIgnoreCase("user")) {
                addition.put(op.getKey().toLowerCase(), op.getValue());
                continue;
            }
            if (op.getKey().equalsIgnoreCase("password")) {
                addition.put(op.getKey().toLowerCase(), op.getValue());
                continue;
            }
            basic.put(op.getKey(), op.getValue());
        }
        var urlFormat = storage.getUrl();
        try (var connector = new JdbcConnector(urlFormat, basic)) {
            conn = connector.connect(addition);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    public Utilities.Meta[] getMeta(String storageType, JdbcConnector conn) {
//        String query = "";
//        switch(storageType) {
//            case "PostgreSQL":
//                query = "SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'";
//            default:
//                throw new RuntimeException("Not Supported Storage Type");
//        }
//        // Get Storage Info
//        var cur = conn.cursor();
//        cur.execute("SELECT * FROM conninfo");
//        StringBuffer sb = new StringBuffer();
//        for( int i = 0; i < cur.getResultSet().getMetaData().getColumnCount(); i++) {
//            sb.append(cur.getResultSet().getMetaData().getColumnName(i + 1) + " ");
//        }
//        log.error(sb.toString());
//
//        var rs = cur.getResultSet();
//        while (rs.next()) {
//            sb = new StringBuffer();
//            for( int i = 0; i < cur.getResultSet().getMetaData().getColumnCount(); i++) {
//                sb.append(rs.getString(i + 1) + " ");
//            }
//            log.error(sb.toString());
//        }
        return null;
    }
}
