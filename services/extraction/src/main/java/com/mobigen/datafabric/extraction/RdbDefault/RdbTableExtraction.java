package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.Postgresql.PostgresqlTypetoFormat;
import com.mobigen.datafabric.extraction.extraction.Extract;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

public class RdbTableExtraction implements Extract {

    public TargetConfig target;
    final public Connection conn;

    Metadata metadata = new Metadata();

    public RdbTableExtraction(TargetConfig target, Connection conn) throws ClassNotFoundException {
        this.target = target;
        this.conn = conn;
        this.metadata.metadata = new HashMap<>();
    }

    @Override
    public HashMap<String, String> extract(Object input) {

        try (conn) {
            var metadata = conn.getMetaData();

            // 테이블 정보 추출
            System.out.println("Tables:");
            var tables = metadata.getTables(target.getConnectInfo().getRdbmsConnectInfo().getSchema(), null, null, new String[]{"TABLE"});
            System.out.println(tables);


            var tableNameList = target.getConnectInfo().getRdbmsConnectInfo().getTableNameList();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");      //테이블 이름

                if (tableNameList.contains(tables.getString("TABLE_NAME"))) {
                    // 각 테이블의 컬럼 정보 추출
                    var columns = metadata.getColumns(target.getConnectInfo().getRdbmsConnectInfo().getSchema(), null, tableName, null);

                    System.out.println("\tcolumn : " + columns);

                    while (columns.next()) {

                        String columnTableName = columns.getString("TABLE_NAME");    //테이블 이름
                        String columnName = columns.getString("COLUMN_NAME");       //열 이름
                        String columnIndex = columns.getString("ORDINAL_POSITION");       //열 이름

                        String columnSize = columns.getString("COLUMN_SIZE");   //열의 크기 문자열 형식의 경우 문자의 수  -- 데이터 형 integer
                        String nullable = columns.getString("NULLABLE");        //열의 NULL 허용 여부 0/1로 표시됨
                        String columnRemarks = columns.getString("REMARKS");      //열에 대한 설명이나 주석
                        String columnDef = columns.getString("COLUMN_DEF");     //열의 기본값
                        String sqlDataType = columns.getString("TYPE_NAME");        //SQL데이터 형식

                        //nullable 출력값 변경
                        if (nullable.equals("1")) {
                            nullable = "true";
                        } else {
                            nullable = "false";
                        }

                        //DataFormat
                        String columnTypeName = PostgresqlTypetoFormat.PostgresDataFormatDistinct(sqlDataType);


                        //제약조건 추출
                        String constSql = "SELECT constraint_type " + "FROM information_schema.key_column_usage AS kc " + "JOIN information_schema.table_constraints AS tc " + "ON kc.constraint_name = tc.constraint_name " + "WHERE kc.table_name = ? AND kc.column_name = ?;";
                        PreparedStatement constPreparedStatement = conn.prepareStatement(constSql);
                        constPreparedStatement.setString(1, columnTableName);
                        constPreparedStatement.setString(2, columnName);

                        ResultSet constResultSet = constPreparedStatement.executeQuery();
                        StringBuilder constraints = new StringBuilder();
                        String isPK = "false";
                        String isFK = "false";
                        while (constResultSet.next()) {
                            if (Objects.equals(constResultSet.getString("constraint_type"), "PRIMARY KEY")) {
                                isPK = "true";
                            } else if (Objects.equals(constResultSet.getString("constraint_type"), "FOREIGN KEY")) {
                                isFK = "true";
                            } else if (Objects.equals(constResultSet.getString("constraint_type"), "NOT NULL")) {
                                continue;
                            } else if (constraints.isEmpty()) {
                                constraints.append(constResultSet.getString("constraint_type"));
                            } else {
                                constraints.append(", ");
                                constraints.append(constResultSet.getString("constraint_type"));
                            }
                            constraints = new StringBuilder(constraints.toString());
                        }

                        System.out.println("\ttableName : " + tableName);
                        System.out.println("\tcolumnIndex : " + columnIndex);
                        System.out.println("\tcolumnName : " + columnName);
                        System.out.println("\tcolumnDescription : " + columnRemarks);
                        System.out.println("\tdataType : " + sqlDataType);
                        System.out.println("\tdataFormat : " + columnTypeName);
                        System.out.println("\tdataLength : " + columnSize);
                        System.out.println("\tisPK : " + isPK);
                        System.out.println("\tisFK : " + isFK);
                        System.out.println("\tisNull : " + nullable);
//                    System.out.println("\tdefaultValue : " + columnDef);
                        System.out.println("\tconstraints : " + constraints);
                    }
                    columns.close();
                }
            }
            tables.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
}
