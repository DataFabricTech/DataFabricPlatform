package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.UserDefineException.DefaultWhileStoppedException;
import com.mobigen.datafabric.extraction.UserDefineException.UserDefineExceptionMessage;
import dto.ModelMetadata;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class RdbDefaultExraction {

    public static List<ModelMetadata> DefaultExract(Map<String, String> conList, Map<String, UUID> list, Statement stmt, DatabaseMetaData dbmetadata) throws DefaultWhileStoppedException, SQLException {

        List<ModelMetadata> modelMetadataList = new ArrayList<>();
        UUID modelId = UUID.randomUUID();
        String tableName = conList.get("table_name");
        String schema = conList.get("schema");

        try (ResultSet columns = dbmetadata.getColumns("public", null, "address", null)) {
            int columnCount = 0;
            while (columns.next()) {
                columnCount++;
            }
            columns.close();

            // 테이블의 행 수 추출
            var rowCountResult = stmt.executeQuery("SELECT COUNT(*) FROM " + schema + "." + tableName);
            int rowCount = 0;
            if (rowCountResult.next()) {
                rowCount = rowCountResult.getInt(1);
            }

            for (String name : list.keySet()) {
                UUID metaId = list.get(name);
                switch (name) {
                    case "COLUMN_COUNT":
                        ModelMetadata columnCountModel = new ModelMetadata(modelId, metaId, String.valueOf(columnCount));
                        modelMetadataList.add(columnCountModel);
                        break;
                    case "ROW_COUNT":
                        ModelMetadata rowCountSchema = new ModelMetadata(modelId, metaId, String.valueOf(rowCount));
                        modelMetadataList.add(rowCountSchema);
                        break;
                    default:
                        ModelMetadata term = new ModelMetadata(modelId, metaId, name);
                        modelMetadataList.add(term);
                        break;
                }

            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (Exception a) {
            throw new DefaultWhileStoppedException(UserDefineExceptionMessage.COLUMN_ERROR_MESSAGE.getErrorMessage());
        }

        return modelMetadataList;
    }
}
