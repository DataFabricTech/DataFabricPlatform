package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.UserDefineException.TableWhileStoppedException;
import com.mobigen.datafabric.extraction.model.TargetConfig;

import java.sql.*;

public class RdbDefaultExraction {

    public static void DefaultExract(ResultSet tables, Statement stmt, TargetConfig target, DatabaseMetaData metadata) throws TableWhileStoppedException, SQLException {

        try {
            String tableName = tables.getString("TABLE_NAME");      //테이블 이름
            String tableType = tables.getString("TABLE_TYPE");      //jdbc 데이터 형식 코드 (TABLE/VIEW/INDEX..)
            String remarks = tables.getString("REMARKS");           //열
            String schema = tables.getString("TABLE_SCHEM");


            System.out.println("\tschema: " + schema); //schema name
            System.out.println("\ttableName: " + tableName);
            System.out.println("\tcategory: " + "정형");
            System.out.println("\ttype: " + tableType);
            System.out.println("\tdescription: " + remarks);

            // 테이블의 컬럼 수 추출
            var columns = metadata.getColumns(target.getConnectInfo().getRdbmsConnectInfo().getSchema(), null, tableName, null);
            int columnCount = 0;
            while (columns.next()) {
                columnCount++;
            }
            System.out.println("\tColumn Count : " + columnCount);
            columns.close();

            // 테이블의 행 수 추출
            var rowCountResult = stmt.executeQuery("SELECT COUNT(*) FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + "." + tableName);
            int rowCount = 0;
            if (rowCountResult.next()) {
                rowCount = rowCountResult.getInt(1);
            }
            System.out.println("\t\tRow Count : " + rowCount);
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (Exception a){
            System.out.println("속 catch 호출 -------------------------");
            throw new TableWhileStoppedException("while문 순회에 에러");
        }
    }
}
