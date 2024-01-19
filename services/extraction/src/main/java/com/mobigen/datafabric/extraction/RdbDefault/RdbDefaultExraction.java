package com.mobigen.datafabric.extraction.RdbDefault;

import com.mobigen.datafabric.extraction.UserDefineException.TableWhileStoppedException;
import com.mobigen.datafabric.extraction.model.Metadata;
import com.mobigen.datafabric.extraction.model.TargetConfig;
import dto.*;
import dto.enums.StatusType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RdbDefaultExraction {

    public static List<ModelMetadata> DefaultExract(ResultSet tables, Statement stmt, TargetConfig target, DatabaseMetaData metadata) throws TableWhileStoppedException, SQLException {

        List<ModelMetadata> modelMetadataList = new ArrayList<>();
        UUID modelId = UUID.fromString("7a481647-8eb6-4466-8d58-90e2137a6202");
        ResultSet metadata_schema = stmt.executeQuery("SELECT metadata_id FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".model_metadata WHERE model_id=" + modelId);
        String tableName = tables.getString("TABLE_NAME");

        try {

            // 테이블의 컬럼 수 추출
            var columns = metadata.getColumns(target.getConnectInfo().getRdbmsConnectInfo().getSchema(), null, tableName, null);
            int columnCount = 0;
            while (columns.next()) {
                columnCount++;
            }
            columns.close();

            // 테이블의 행 수 추출
            var rowCountResult = stmt.executeQuery("SELECT COUNT(*) FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + "." + tableName);
            int rowCount = 0;
            if (rowCountResult.next()) {
                rowCount = rowCountResult.getInt(1);
            }

            while(metadata_schema.next()){
                ResultSet metadata_set = stmt.executeQuery("SELECT metadata_id, name FROM " + target.getConnectInfo().getRdbmsConnectInfo().getSchema() + ".model_metadata WHERE model_id=" + modelId);
                UUID metaId = (UUID) metadata_set.getObject("metadata_id");
                String name = metadata_set.getString("name");

                switch (name){
                    case "table_name":
                        ModelMetadata tableNameMeta = new ModelMetadata(modelId, metaId, tables.getString("TABLE_NAME"));
                        modelMetadataList.add(tableNameMeta);
                        break;
                    case "category":
                        ModelMetadata category = new ModelMetadata(modelId, metaId, tables.getString("정형"));
                        modelMetadataList.add(category);
                        break;
                    case "type":
                        ModelMetadata type = new ModelMetadata(modelId, metaId, tables.getString("TABLE_TYPE"));
                        modelMetadataList.add(type);
                        break;
                    case "column_count":
                        ModelMetadata columnCountModel = new ModelMetadata(modelId, metaId, String.valueOf(columnCount));
                        modelMetadataList.add(columnCountModel);
                        break;
                    case "row_count":
                        ModelMetadata row_count = new ModelMetadata(modelId, metaId, String.valueOf(rowCount));
                        modelMetadataList.add(row_count);
                        break;
                    case "schema":
                        ModelMetadata schema = new ModelMetadata(modelId, metaId, tables.getString("TABLE_SCHEM"));
                        modelMetadataList.add(schema);
                        break;
                    case "description":
                        ModelMetadata description = new ModelMetadata(modelId, metaId, tables.getString("REMARKS"));
                        modelMetadataList.add(description);
                        break;
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (Exception a){
            throw new TableWhileStoppedException("while문 순회에 에러");
        }

        return modelMetadataList;
    }
}
