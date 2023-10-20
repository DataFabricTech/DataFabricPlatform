package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.libs.grpc.Storage;
import com.mobigen.sqlgen.maker.MakerInterface;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.conditions.Equal;

import javax.annotation.Nullable;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mobigen.sqlgen.SqlBuilder.*;

public class AdaptorService {
    SqlTable adaptorTable = SqlTable.of("DataStorageAdaptor");
    SqlColumn idCol = SqlColumn.of("id", adaptorTable, JDBCType.VARCHAR);
    SqlColumn storageTypeCol = SqlColumn.of("datastorage_type_name", adaptorTable, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", adaptorTable, JDBCType.VARCHAR);
    SqlColumn versionCol = SqlColumn.of("version", adaptorTable, JDBCType.VARCHAR);
    SqlColumn urlCol = SqlColumn.of("url", adaptorTable, JDBCType.VARCHAR);
    SqlColumn pathCol = SqlColumn.of("path", adaptorTable, JDBCType.VARCHAR);
    SqlColumn driverCol = SqlColumn.of("driver", adaptorTable, JDBCType.VARCHAR);
    SqlColumn createdByCol = SqlColumn.of("created_by", adaptorTable, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", adaptorTable, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", adaptorTable, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", adaptorTable, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", adaptorTable, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", adaptorTable, JDBCType.TIMESTAMP);

    SqlTable adaptorSchemaTable = SqlTable.of("DataStorageConnectionSchema");
    SqlColumn storageAdaptorIdCol = SqlColumn.of("datastorage_adaptor_id", adaptorTable, JDBCType.VARCHAR);
    SqlColumn schemaKeyCol = SqlColumn.of("key", adaptorSchemaTable, JDBCType.VARCHAR);
    SqlColumn schemaTypeCol = SqlColumn.of("type", adaptorSchemaTable, JDBCType.VARCHAR);
    SqlColumn schemaRequiredCol = SqlColumn.of("required", adaptorSchemaTable, JDBCType.VARCHAR);


    public List<Storage.AdaptorModel> getAdaptors(@Nullable String storageType) {
        var sqlSelect = select(idCol, nameCol, versionCol, urlCol, driverCol)
                .from(adaptorTable);
        MakerInterface sqlStmt;
        if (storageType != null && !storageType.isBlank()) {
            sqlStmt = sqlSelect.where(Equal.of(storageTypeCol, storageType));
        } else {
            sqlStmt = sqlSelect;
        }
        var sql = sqlStmt.generate().getStatement();
        var result = DataLayerConnection.getDataDB(sql);
        List<Storage.AdaptorModel> models = new ArrayList<>();
        try {
            while (result.next()) {
                models.add(Storage.AdaptorModel.newBuilder()
                        .setId(result.getString(1))
                        .setName(result.getString(2))
                        .setVersion(result.getString(3))
                        .setUrl(result.getString(4))
                        .setDriver(result.getString(5))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return models;
    }

    public Storage.AdaptorModel createAdaptor(Storage.AdaptorModel model) {
        var adaptorId = UUID.randomUUID().toString();
        var sql = insert(adaptorTable)
                .columns(idCol, storageTypeCol, nameCol, versionCol, urlCol, pathCol, driverCol)
                .values(adaptorId, model.getStorageType(), model.getName(), model.getVersion(), model.getUrl(), model.getPath(), model.getDriver())
                .generate().getStatement();

        List<Integer> result;
        if (!model.getSchemaList().isEmpty()) {
            var stmt = insert(adaptorSchemaTable)
                    .columns(storageAdaptorIdCol, schemaKeyCol, schemaTypeCol, schemaRequiredCol);
            for (var pair : model.getSchemaList()) {
                stmt = stmt.values(adaptorId, pair.getKey(), pair.getType(), pair.getRequired());
            }
            var schemaSql = stmt.generate().getStatement();
            result = DataLayerConnection.insertUpdateDataDB(sql, schemaSql);
        } else {
            result = DataLayerConnection.insertUpdateDataDB(sql);
        }
        System.out.println(result);
        if (result.stream().anyMatch(x -> x.equals(0))) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }

    public Storage.AdaptorModel updateAdaptor(Storage.AdaptorModel model) {
        var sql = update(adaptorTable)
                .columns(storageTypeCol, nameCol, versionCol, urlCol, pathCol, driverCol)
                .values(model.getStorageType(), model.getName(), model.getVersion(), model.getUrl(), model.getPath(), model.getDriver())
                .where(Equal.of(idCol, model.getId()))
                .generate().getStatement();
        var result = DataLayerConnection.insertUpdateDataDB(sql);
        System.out.println(result);
        if (result.stream().anyMatch(x -> x.equals(0))) {
            throw new RuntimeException("update fail");
        }
        return model;
    }

}
