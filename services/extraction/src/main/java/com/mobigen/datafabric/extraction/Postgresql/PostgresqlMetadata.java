package com.mobigen.datafabric.extraction.Postgresql;

import com.mobigen.datafabric.extraction.RdbDefault.RdbExtractMetadata;
import com.mobigen.datafabric.extraction.UserDefineException.TableWhileStoppedException;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresqlMetadata extends RdbExtractMetadata {
    TargetConfig target = new TargetConfig();

    public PostgresqlMetadata(TargetConfig target) throws ClassNotFoundException {
        super(target);
    }

    @Override
    public void extractDefault() throws UnsupportedFormatException {
        try {
            Class.forName(target.getConnectInfo().getRdbmsConnectInfo().getJDBC_DRIVER());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 데이터 베이스 연결
        try (var conn = DriverManager.getConnection(target.getConnectInfo().getRdbmsConnectInfo().getUrl(), target.getConnectInfo().getRdbmsConnectInfo().getUsername(), target.getConnectInfo().getRdbmsConnectInfo().getPassword())) {
            var metadata = conn.getMetaData();
            var stmt = conn.createStatement();

            // 테이블 정보 추출
            System.out.print("Tables: ");
            var tables = metadata.getTables(null, null, null, new String[]{"TABLE"});
            System.out.println(tables);

            var tableNameList = target.getConnectInfo().getRdbmsConnectInfo().getTableNameList();

            while (tables.next()) {
                if (tableNameList.contains(tables.getString("TABLE_NAME"))) {
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
                    var columns = metadata.getColumns(null, null, tableName, null);

                    int columnCount = 0;
                    while (columns.next()) {
                        columnCount++;
                    }
                    System.out.println("\tColumn Count : " + columnCount);
                    columns.close();

                    // 테이블의 행 수 추출
                    var rowCountResult = stmt.executeQuery("SELECT COUNT(*) FROM " + schema + "." + tableName);
                    int rowCount = 0;
                    if (rowCountResult.next()) {
                        rowCount = rowCountResult.getInt(1);
                    }
                    System.out.println("\t\tRow Count : " + rowCount);
                }
            }
            tables.close();
        } catch (TableWhileStoppedException e) {
            System.out.println("Table stopped error!!!!!!!!!!!!!!!");
            e.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("SQL error!!!!!!!!!!!!!!!");
            ex.printStackTrace();
        }
    }
}
