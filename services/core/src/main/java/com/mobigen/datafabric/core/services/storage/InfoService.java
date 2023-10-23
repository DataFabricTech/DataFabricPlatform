package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.libs.grpc.Storage;
import com.mobigen.sqlgen.model.JoinHow;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.conditions.Equal;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;

import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

public class InfoService {
    SqlTable dataStorageTable = SqlTable.of("DataStorage");
    SqlColumn idCol = SqlColumn.of("id", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn storageTypeCol = SqlColumn.of("type", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn adaptorCol = SqlColumn.of("adaptor", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn userDescCol = SqlColumn.of("user_desc", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn totalDataCol = SqlColumn.of("total_data", dataStorageTable, JDBCType.INTEGER);
    SqlColumn regiDataCol = SqlColumn.of("regi_data", dataStorageTable, JDBCType.INTEGER);
    SqlColumn createdByCol = SqlColumn.of("created_by", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", dataStorageTable, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", dataStorageTable, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", dataStorageTable, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", dataStorageTable, JDBCType.TIMESTAMP);
    SqlColumn statusCol = SqlColumn.of("status", dataStorageTable, JDBCType.INTEGER);
    SqlColumn lastConnectionCheckedAtCol = SqlColumn.of("last_connection_checked_at", dataStorageTable, JDBCType.TIMESTAMP);
    SqlColumn lastSyncAtCol = SqlColumn.of("last_sync_at", dataStorageTable, JDBCType.TIMESTAMP);
    SqlTable dataStorageConnectionInfoTable = SqlTable.of("DataStorageConnectionInfo");
    SqlColumn dataStorageIdCol = SqlColumn.of("datastorage_id", dataStorageConnectionInfoTable, JDBCType.VARCHAR);
    SqlColumn connectionInfoKeyCol = SqlColumn.of("key", dataStorageConnectionInfoTable, JDBCType.VARCHAR);
    SqlColumn connectionInfoValueCol = SqlColumn.of("value", dataStorageConnectionInfoTable, JDBCType.VARCHAR);
    SqlTable adaptorSchemaTable = SqlTable.of("DataStorageConnectionSchema");
    SqlColumn storageAdaptorIdCol = SqlColumn.of("datastorage_adaptor_id", adaptorSchemaTable, JDBCType.VARCHAR);
    SqlColumn schemaKeyCol = SqlColumn.of("key", adaptorSchemaTable, JDBCType.VARCHAR);
    SqlColumn schemaTypeCol = SqlColumn.of("type", adaptorSchemaTable, JDBCType.VARCHAR);
    SqlColumn schemaRequiredCol = SqlColumn.of("required", adaptorSchemaTable, JDBCType.VARCHAR);

    public List<Storage.InfoModel> getInfos() {
        var sql = select(idCol, storageTypeCol, adaptorCol, nameCol, schemaKeyCol, schemaTypeCol, schemaRequiredCol, connectionInfoKeyCol, connectionInfoValueCol)
                .from(dataStorageTable)
                .join(dataStorageConnectionInfoTable, JoinHow.LEFT, Equal.of(idCol, dataStorageIdCol))
                .join(adaptorSchemaTable, JoinHow.LEFT, Equal.of(adaptorCol, storageAdaptorIdCol), Equal.of(schemaKeyCol, connectionInfoKeyCol))
                .generate().getStatement();
        var result = DataLayerConnection.getDataDB(sql);
        List<Storage.InfoModel> models = new ArrayList<>();
        try {
            Map<String, Storage.InfoModel.Builder> infoModelData = new HashMap<>();
            Map<String, List<Storage.ConnectionInfo>> connInfoData = new HashMap<>();
            while (result.next()) {
                if (!infoModelData.containsKey(result.getString(1))) {
                    infoModelData.put(result.getString(1),
                            Storage.InfoModel.newBuilder()
                                    .setId(result.getString(1))
                                    .setStorageType(result.getString(2))
                                    .setAdaptorId(result.getString(3))
                                    .setName(result.getString(4))
                    );
                }

                var schemaKey = result.getString(5);
                var schemaType = result.getString(6);
                if (schemaType == null) {
                    continue;
                }
                var connInfoKey = result.getString(8);
                if (connInfoKey == null) {
                    connInfoKey = "";
                }
                if (schemaType.equals("string")) {
                    var connInfoValue = result.getString(9);
                    if (connInfoValue == null) {
                        connInfoValue = "";
                    }
                    if (!connInfoData.containsKey(result.getString(1))) {
                        List<Storage.ConnectionInfo> data = new ArrayList<>();
                        data.add(Storage.ConnectionInfo.newBuilder()
                                .setKey(connInfoKey)
                                .setStringValue(connInfoValue)
                                .build());
                        connInfoData.put(result.getString(1), data);
                    } else {
                        connInfoData.get(result.getString(1)).add(Storage.ConnectionInfo.newBuilder()
                                .setKey(connInfoKey)
                                .setStringValue(connInfoValue)
                                .build());
                    }
                } else {
                    var connInfoValue = result.getInt(9);
                    if (!connInfoData.containsKey(result.getString(1))) {
                        List<Storage.ConnectionInfo> data = new ArrayList<>();
                        data.add(Storage.ConnectionInfo.newBuilder()
                                .setKey(connInfoKey)
                                .setInt32Value(connInfoValue)
                                .build());
                        connInfoData.put(result.getString(1), data);
                    } else {
                        connInfoData.get(result.getString(1)).add(Storage.ConnectionInfo.newBuilder()
                                .setKey(connInfoKey)
                                .setInt32Value(connInfoValue)
                                .build());
                    }
                }

            }
            for (var modelId : infoModelData.keySet()) {
                if (connInfoData.isEmpty()) {
                    models.add(
                            infoModelData.get(modelId)
                                    .build()
                    );
                } else {
                    models.add(
                            infoModelData.get(modelId)
                                    .addAllConnInfo(connInfoData.get(modelId))
                                    .build()
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return models;
    }

    public Storage.InfoModel createInfo(Storage.InfoModel model) throws SQLException {
        var sql = insert(dataStorageTable)
                .columns(idCol, storageTypeCol, adaptorCol, nameCol)
                .values(UUID.randomUUID().toString(), model.getStorageType(), model.getAdaptorId(), model.getName())
                .generate().getStatement();
        var result = DataLayerConnection.insertUpdateDataDB(sql);
        System.out.println(result);
        if (result.stream().anyMatch(x -> x.equals(-1))) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }
}
