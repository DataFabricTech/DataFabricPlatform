package com.mobigen.datafabric.extraction.MySQL;

import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.ExtractAdditional;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class MySQLMetadata implements Extract, ExtractAdditional {
    MySQLJDBCInfo mySQLJDBCInfo = new MySQLJDBCInfo();
    String NAME = "MySQL";
    TargetConfig target;
    Metadata metadata = new Metadata();

    public MySQLMetadata(TargetConfig target) {
        this.metadata.metadata = new HashMap<>();
        this.target = target;
    }

    @Override
    public Metadata extract() {
        try {
            extractDefault();
            extractAdditional();
        } catch (UnsupportedFormatException e) {
            throw new RuntimeException(e);
        }

        return this.metadata;
    }

    @Override
    public void extractDefault() throws UnsupportedFormatException {

        //var defaultMeta = new HashMap<String, String>();

//들여쓰기 에러 - 잡아주는 단축키 있음 command option l - commit 시 문제
        try {
            // JDBC 드라이버 로딩
            Class.forName(mySQLJDBCInfo.getJDBC_DRIVER());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 데이터 베이스 연결
        //**
        try (var conn = DriverManager.getConnection(
                mySQLJDBCInfo.getUrl(), mySQLJDBCInfo.getUsername(), mySQLJDBCInfo.getPassword())) {
            var metadata = conn.getMetaData();
            var stmt = conn.createStatement();

            // 테이블 정보 추출
            System.out.println("Tables:");
            var tables = metadata.getTables(mySQLJDBCInfo.getSchema(), null, null, new String[]{"TABLE"});
            System.out.println(tables);
            while (tables.next()) {
                //exception 처리 - 함수로 빼기
                String tableName = tables.getString("TABLE_NAME");      //테이블 이름
                String tableType = tables.getString("TABLE_TYPE");      //jdbc 데이터 형식 코드 (TABLE/VIEW/INDEX..)
                String remarks = tables.getString("REMARKS");           //열
                String schema = tables.getString("TABLE_SCHEM");



                System.out.println("\tschema: " + schema); //schema name
                System.out.println("\ttableName: " + tableName);
                System.out.println("\tcategory: " + "정형");
                System.out.println("\ttype: " + tableType);
                System.out.println("\tdescription: " + remarks);

                // 각 테이블의 컬럼 수 추출
                var columns = metadata.getColumns(mySQLJDBCInfo.getSchema(), null, tableName, null);

                int columnCount = 0;
                while (columns.next()) {
                    columnCount++;
                }
                System.out.println("\tColumn Count : " + columnCount);
                columns.close();

                // 테이블의 행 수 추출
                var rowCountResult = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                int rowCount = 0;
                if (rowCountResult.next()) {
                    rowCount = rowCountResult.getInt(1);
                }
                System.out.println("\t\tRow Count : " + rowCount);
            }
            tables.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

//        var name = defaultMeta.get("name");
//        var modifiedAt = Long.parseLong(defaultMeta.get("modifiedAt"));
//        var type = StructuredType.STRUCTURED;
//
//        if (!defaultMeta.get("dataformat").equals("table"))
//            throw new UnsupportedFormatException("no table");
//
//        var dataFormat = DataFormat.TABLE;
//
//        var size = Long.parseLong(defaultMeta.get("size"));

//        for (var i: defaultMeta.keySet()) {
//            this.metadata.metadata.put(i, defaultMeta.get(i));
//        }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {

        var table = new MySQLTableExtraction();
        var additionalMeta = table.extract(target);

//        for (var i: additionalMeta.keySet()) {
//            this.metadata.metadata.put(i, additionalMeta.get(i));
//        }
    }
}
