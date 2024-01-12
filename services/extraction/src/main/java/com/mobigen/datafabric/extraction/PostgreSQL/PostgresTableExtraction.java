package com.mobigen.datafabric.extraction.PostgreSQL;

import com.mobigen.datafabric.extraction.extraction.Extract;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class PostgresTableExtraction implements Extract {
    PostgreSQLJDBCInfo postgreSQLJDBCInfo = new PostgreSQLJDBCInfo();

    @Override
    public HashMap<String, String> extract(Object input) {

//        HashMap<String, String> tableExtract = new HashMap<>();

        try {
            // JDBC 드라이버 로딩
            Class.forName(postgreSQLJDBCInfo.JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 데이터 베이스 연결
        try (var conn = DriverManager.getConnection(postgreSQLJDBCInfo.url, postgreSQLJDBCInfo.username, postgreSQLJDBCInfo.password)) {
            var metadata = conn.getMetaData();

            // 테이블 정보 추출
            System.out.print("Tables: ");
            var tables = metadata.getTables(null, null, null, new String[]{"TABLE"});
            System.out.println(tables);
            var tableNameList = postgreSQLJDBCInfo.tableNameList;

            while (tables.next()) {
                if (tableNameList.contains(tables.getString("TABLE_NAME"))) {
                    String tableName = tables.getString("TABLE_NAME");      //테이블 이름

                    // 각 테이블의 컬럼 정보 추출
                    var columns = metadata.getColumns(null, null, tableName, null);


                    int columnCount = 0;
                    while (columns.next()) {
                        columnCount++;

                        String columnName = columns.getString("COLUMN_NAME");       //열 이름
                        String columnSize = columns.getString("COLUMN_SIZE");   //열의 크기 문자열 형식의 경우 문자의 수  -- 데이터 형 integer
                        String nullable = columns.getString("NULLABLE");        //열의 NULL 허용 여부 0/1로 표시됨
                        String col_remarks = columns.getString("REMARKS");      //열에 대한 설명이나 주석
                        String columnDef = columns.getString("COLUMN_DEF");     //열의 기본값
                        String sqlDataType = columns.getString("TYPE_NAME");        //SQL데이터 형식

                        //nullable 출력값 변경
                        if (nullable.equals("1")) {
                            nullable = "true";
                        } else {
                            nullable = "false";
                        }

                        //DataFormat
                        String col_typeName = PostgreSQLdataTypetoFormat.PostgresDataFormatDistinct(sqlDataType);


                        //제약조건 추출
                        String const_sql = "SELECT constraint_type " + "FROM information_schema.key_column_usage AS kc " + "JOIN information_schema.table_constraints AS tc " + "ON kc.constraint_name = tc.constraint_name " + "WHERE kc.table_name = ? AND kc.column_name = ?;";
                        PreparedStatement const_preparedStatement = conn.prepareStatement(const_sql);
                        const_preparedStatement.setString(1, tableName);
                        const_preparedStatement.setString(2, columnName);

                        ResultSet const_resultSet = const_preparedStatement.executeQuery();
                        StringBuilder constraints = new StringBuilder();
                        String isPK = "false";
                        String isFK = "false";
                        while (const_resultSet.next()) {
                            if (Objects.equals(const_resultSet.getString("constraint_type"), "PRIMARY KEY")) {
                                isPK = "true";
                            } else if (Objects.equals(const_resultSet.getString("constraint_type"), "FOREIGN KEY")) {
                                isFK = "true";
                            } else if (Objects.equals(const_resultSet.getString("constraint_type"), "NOT NULL")) {
                                continue;
                            } else {
                                constraints.append(const_resultSet.getString("constraint_type"));
                            }
                            constraints = new StringBuilder(constraints.toString());
                        }


                        System.out.println("\ttableName : " + tableName);
                        System.out.println("\tcolumnName : " + columnName);
                        System.out.println("\tcolumnDescription : " + col_remarks);
                        System.out.println("\tdataLength : " + columnSize);
                        System.out.println("\tdataType : " + sqlDataType);
                        System.out.println("\tdataFormat : " + col_typeName);
                        System.out.println("\tisPK : " + isPK);
                        System.out.println("\tisFK : " + isFK);
                        System.out.println("\tdefaultValue : " + columnDef);
                        System.out.println("\tisNull : " + nullable);
                        System.out.println("\tconstraints : " + constraints);

                    }
                    columns.close();
                }
            }
            tables.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
