package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.model.AdaptorUsableAuthTable;
import com.mobigen.datafabric.core.model.ConnectionSchemaTable;
import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.libs.grpc.Storage;
import com.mobigen.sqlgen.maker.JoinMaker;
import com.mobigen.sqlgen.maker.MakerInterface;
import com.mobigen.sqlgen.model.JoinHow;
import com.mobigen.sqlgen.where.conditions.Equal;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

public class AdaptorService {
    DataStorageAdaptorTable adaptorTable = new DataStorageAdaptorTable();

    ConnectionSchemaTable connectionSchemaTable = new ConnectionSchemaTable();
    AdaptorUsableAuthTable adaptorUsableAuthTable = new AdaptorUsableAuthTable();

    private List<Storage.AdaptorModel> getAdaptorData(ResultSet result) {
        List<Storage.AdaptorModel> models = new ArrayList<>();
        try {
            Map<String, Storage.AdaptorModel.Builder> adaptorData = new HashMap<>();
            Map<String, List<Storage.AdaptorSchema>> adaptorSchemaData = new HashMap<>();
            Map<String, Set<String>> authTypes = new HashMap<>();
            while (result.next()) {
                if (!adaptorData.containsKey(result.getString(1))) {
                    adaptorData.put(result.getString(1),
                            Storage.AdaptorModel.newBuilder()
                                    .setId(result.getString(1))
                                    .setName(result.getString(2))
                                    .setVersion(result.getString(3))
                                    .setUrl(result.getString(4))
                                    .setDriver(result.getString(5))
                    );
                }
                if (!authTypes.containsKey(result.getString(1))) {
                    Set<String> set = new HashSet<>();
                    set.add(result.getString(9));
                    authTypes.put(result.getString(1), set);
                } else {
                    authTypes.get(result.getString(1)).add(result.getString(9));
                }

                if (!adaptorSchemaData.containsKey(result.getString(1))) {
                    List<Storage.AdaptorSchema> data = new ArrayList<>();
                    data.add(Storage.AdaptorSchema.newBuilder()
                            .setKey(result.getString(6))
                            .setType(result.getString(7))
                            .setRequired(result.getBoolean(8))
                            .build());
                    adaptorSchemaData.put(result.getString(1), data);
                } else {
                    adaptorSchemaData.get(result.getString(1)).add(Storage.AdaptorSchema.newBuilder()
                            .setKey(result.getString(6))
                            .setType(result.getString(7))
                            .setRequired(result.getBoolean(8))
                            .build());
                }
            }
            for (var adaptorModelId : adaptorData.keySet()) {
                models.add(
                        adaptorData.get(adaptorModelId)
                                .addAllSchema(adaptorSchemaData.get(adaptorModelId))
                                .addAllAuthType(authTypes.get(adaptorModelId).stream().toList())
                                .build()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return models;
    }

    private MakerInterface getStatementToGetAdaptor() {
        return select(
                adaptorTable.getIdCol(),
                adaptorTable.getNameCol(),
                adaptorTable.getStorageTypeNameCol(),
                adaptorTable.getVersionCol(),
                adaptorTable.getDriverCol(),

                connectionSchemaTable.getKeyCol(),
                connectionSchemaTable.getTypeCol(),
                connectionSchemaTable.getDefaultCol(),

                adaptorUsableAuthTable.getAuthTypeCol()
        )
                .from(adaptorTable.getTable())
                .join(connectionSchemaTable.getTable(), JoinHow.LEFT, Equal.of(adaptorTable.getIdCol(), connectionSchemaTable.getAdaptorIdCol()))
                .join(adaptorUsableAuthTable.getTable(), JoinHow.LEFT, Equal.of(adaptorTable.getIdCol(), adaptorUsableAuthTable.getAdaptorIdCol()));
    }

    public Storage.AdaptorModel getAdaptor(String adaptorId) {
        var sql = ((JoinMaker) getStatementToGetAdaptor())
                .where(Equal.of(adaptorTable.getIdCol(), adaptorId))
                .generate().getStatement();
        var result = DataLayerConnection.getDataDB(sql);
        var models = getAdaptorData(result);
        if (models.size() != 1) {
            return null;
        }
        return models.get(0);
    }

    public List<Storage.AdaptorModel> getAdaptors(@Nullable String storageType) {
        var sqlSelect = (JoinMaker) getStatementToGetAdaptor();
        MakerInterface sqlStmt;
        if (storageType != null && !storageType.isBlank()) {
            sqlStmt = sqlSelect.where(Equal.of(adaptorTable.getStorageTypeNameCol(), storageType));
        } else {
            sqlStmt = sqlSelect;
        }
        var sql = sqlStmt.generate().getStatement();
        var result = DataLayerConnection.getDataDB(sql);
        return getAdaptorData(result);
    }

    public Storage.AdaptorModel createAdaptor(Storage.AdaptorModel model) throws SQLException {
        var adaptorId = UUID.randomUUID().toString();
        var path = "";
        var adaptorSql = insert(adaptorTable.getTable())
                .columns(adaptorTable.getIdCol(), adaptorTable.getStorageTypeNameCol(), adaptorTable.getNameCol(),
                        adaptorTable.getVersionCol(), adaptorTable.getPathCol(), adaptorTable.getDriverCol())
                .values(adaptorId, model.getStorageType(), model.getName(), model.getVersion(), path, model.getDriver())
                .generate().getStatement();
        var authSql = insert(adaptorUsableAuthTable.getTable())
                .columns(adaptorUsableAuthTable.getAdaptorIdCol(), adaptorUsableAuthTable.getAuthTypeCol())
                .values(adaptorId, model.getAuthType(0))
                .generate().getStatement();
        List<Integer> result;
        if (!model.getSchemaList().isEmpty()) {
            var stmt = insert(connectionSchemaTable.getTable())
                    .columns(connectionSchemaTable.getAdaptorIdCol(), connectionSchemaTable.getKeyCol(),
                            connectionSchemaTable.getTypeCol(), connectionSchemaTable.getDefaultCol());
            for (var pair : model.getSchemaList()) {
                stmt = stmt.values(adaptorId, pair.getKey(), pair.getType(), pair.getRequired());
            }
            var schemaSql = stmt.generate().getStatement();
            result = DataLayerConnection.insertUpdateDataDB(adaptorSql, authSql, schemaSql);
        } else {
            result = DataLayerConnection.insertUpdateDataDB(adaptorSql, authSql);
        }
        System.out.println(result);
        if (result.stream().anyMatch(x -> x.equals(-1))) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }

    public Storage.AdaptorModel updateAdaptor(Storage.AdaptorModel model) {
//        var sql = update(adaptorTable.getTable())
//                .columns(storageTypeCol, nameCol, versionCol, urlCol, pathCol, driverCol)
//                .values(model.getStorageType(), model.getName(), model.getVersion(), model.getUrl(), model.getPath(), model.getDriver())
//                .where(Equal.of(idCol, model.getId()))
//                .generate().getStatement();
//
//        List<Integer> result;
//        if (!model.getSchemaList().isEmpty()) {
//            var deleteSql = delete(adaptorSchemaTable)
//                    .where(Equal.of(storageAdaptorIdCol, model.getId()))
//                    .generate().getStatement();
//            var stmt = insert(adaptorSchemaTable)
//                    .columns(storageAdaptorIdCol, schemaKeyCol, schemaTypeCol, schemaRequiredCol);
//            for (var pair : model.getSchemaList()) {
//                stmt = stmt.values(model.getId(), pair.getKey(), pair.getType(), pair.getRequired());
//            }
//            var schemaSql = stmt.generate().getStatement();
//            result = DataLayerConnection.insertUpdateDataDB(deleteSql, sql, schemaSql);
//        } else {
//            result = DataLayerConnection.insertUpdateDataDB(sql);
//        }
//        System.out.println(result);
//        if (result.stream().anyMatch(x -> x.equals(-1))) {
//            throw new RuntimeException("update fail");
//        }
        return model;
    }

}
