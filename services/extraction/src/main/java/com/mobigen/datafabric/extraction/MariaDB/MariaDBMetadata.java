package com.mobigen.datafabric.extraction.MariaDB;

import com.mobigen.datafabric.extraction.dataSourceMetadata.Extract;
import com.mobigen.datafabric.extraction.dataSourceMetadata.ExtractAdditional;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import org.apache.tika.exception.UnsupportedFormatException;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class MariaDBMetadata implements Extract, ExtractAdditional {

    MariaDBJDBCInfo mariaDBJDBCInfo = new MariaDBJDBCInfo();
    String NAME = "MariaDB";
    TargetConfig target;
    Metadata metadata = new Metadata();


    public MariaDBMetadata(TargetConfig target) {
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
        var defaultMeta = new HashMap<String, String>();


        try {
            // JDBC 드라이버 로딩
            Class.forName(mariaDBJDBCInfo.getJDBC_DRIVER());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 데이터 베이스 연결
        try (var conn = DriverManager.getConnection(
                mariaDBJDBCInfo.getUrl(), mariaDBJDBCInfo.getUsername(), mariaDBJDBCInfo.getPassword())) {
            var metadata = conn.getMetaData();
            var stmt = conn.createStatement();

            // 테이블 정보 추출
            System.out.print("Tables:");
            var tables = metadata.getTables(mariaDBJDBCInfo.schema, null, null, new String[]{"TABLE"});

            while (tables.next()) {
                String tableSchema = tables.getString("table_schem");
                String tableName = tables.getString("TABLE_NAME");      //테이블 이름
                String tableType = tables.getString("TABLE_TYPE");      //jdbc 데이터 형식 코드 (TABLE/VIEW/INDEX..)
                String remarks = tables.getString("REMARKS");           //열

                System.out.println("Schema: " + tableSchema);
                System.out.println("\ttableName: " + tableName);
                System.out.println("\tcategory: " + "정형");
                System.out.println("\ttype: " + tableType);
                System.out.println("\tdescription: " + remarks);

                // 각 테이블의 컬럼 수 추출
                var columns = metadata.getColumns(mariaDBJDBCInfo.schema, null, tableName, null);

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

        for (var i : defaultMeta.keySet()) {
            this.metadata.metadata.put(i, defaultMeta.get(i));
        }
    }

    @Override
    public void extractAdditional() throws UnsupportedFormatException {
        var table = new MariaDBTableExtraction();
        var additionalMeta = table.extract(this.target);
//
//        for (var i: additionalMeta.keySet()) {
//            this.metadata.metadata.put(i, additionalMeta.get(i));
//        }
    }
}