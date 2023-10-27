package com.mobigen.datafabric.core.services.storage.direct;

import com.mobigen.datafabric.core.model.ConnInfoTable;
import com.mobigen.datafabric.core.model.ConnectionSchemaTable;
import com.mobigen.datafabric.core.model.DataStorageTable;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.libs.grpc.Storage;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.where.conditions.Equal;

import java.sql.SQLException;
import java.util.*;

import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

/**
 * DataStorage 서비스
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 * @deprecated
 */
@Deprecated
public class InfoService {
    DataStorageTable dataStorageTable = new DataStorageTable();
    ConnInfoTable connInfoTable = new ConnInfoTable();
    ConnectionSchemaTable connectionSchemaTable = new ConnectionSchemaTable();


    public List<Storage.InfoModel> getInfos() {
        var sql = select(
                dataStorageTable.getIdCol(),
                dataStorageTable.getAdaptorCol(),
                dataStorageTable.getNameCol(),
                connInfoTable.getKeyCol(),
                connInfoTable.getTypeCol(),
                connInfoTable.getValueCol())
                .from(dataStorageTable.getTable())
                .join(connInfoTable.getTable(), JoinMethod.LEFT, Equal.of(dataStorageTable.getIdCol(), connInfoTable.getDatastorageIdCol()))
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
                                    .setAdaptorId(result.getString(2))
                                    .setName(result.getString(3))
                    );
                }

                var connInfoKey = result.getString(4);
                var schemaType = result.getString(5);
                if (schemaType == null) {
                    continue;
                }
                if (schemaType.equals("string")) {
                    var connInfoValue = result.getString(6);
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
                    var connInfoValue = result.getInt(6);
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
                if (!connInfoData.containsKey(modelId)) {
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
        var connId = UUID.randomUUID().toString();
        var sql = insert(dataStorageTable.getTable())
                .columns(dataStorageTable.getIdCol(), dataStorageTable.getAdaptorCol(), dataStorageTable.getNameCol())
                .values(connId, model.getAdaptorId(), model.getName())
                .generate().getStatement();

        List<Integer> result;
        if (!model.getConnInfoList().isEmpty()) {
            var stmt = insert(connInfoTable.getTable())
                    .columns(connInfoTable.getDatastorageIdCol(), connInfoTable.getKeyCol(),
                            connInfoTable.getTypeCol(), connInfoTable.getValueCol());
            for (var pair : model.getConnInfoList()) {
                var valType = pair.getType();
                var value = pair.getStringValue();
//                byte[] value;
//                if (valType.equals("string")) {
//                    value = pair.getStringValue().getBytes();
//                } else if (valType.equals("int32")) {
//                    value = new byte[]{Integer.valueOf(pair.getInt32Value()).byteValue()};
//                } else if (valType.equals("int64")) {
//                    value = new byte[]{Long.valueOf(pair.getInt64Value()).byteValue()};
//                } else {
//                    value = pair.getStringValue().getBytes();
//                }
                stmt = stmt.values(connId, pair.getKey(), valType, value);
            }
            var connInfoSql = stmt.generate().getStatement();
            result = DataLayerConnection.insertUpdateDataDB(sql, connInfoSql);
        } else {
            result = DataLayerConnection.insertUpdateDataDB(sql);
        }

        System.out.println(result);
        if (result.stream().anyMatch(x -> x.equals(-1))) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }
}
