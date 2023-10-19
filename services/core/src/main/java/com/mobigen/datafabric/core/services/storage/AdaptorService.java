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
    SqlTable table = SqlTable.of("DataStorageAdaptor");
    SqlColumn idCol = SqlColumn.of("id", table, JDBCType.VARCHAR);
    SqlColumn storageTypeCol = SqlColumn.of("datastorage_type_name", table, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn versionCol = SqlColumn.of("version", table, JDBCType.VARCHAR);
    SqlColumn urlCol = SqlColumn.of("url", table, JDBCType.VARCHAR);
    SqlColumn pathCol = SqlColumn.of("path", table, JDBCType.VARCHAR);
    SqlColumn driverCol = SqlColumn.of("driver", table, JDBCType.VARCHAR);
    SqlColumn createdByCol = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);

    public List<Storage.AdaptorModel> getAdaptors(@Nullable String storageType) {
        var sqlSelect = select(idCol, nameCol, versionCol, urlCol, driverCol)
                .from(table);
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
        var sql = insert(table)
                .columns(idCol, storageTypeCol, nameCol, versionCol, urlCol, pathCol, driverCol)
                .values(UUID.randomUUID().toString(), model.getStorageType(), model.getName(), model.getVersion(), model.getUrl(), model.getPath(), model.getDriver())
                .generate().getStatement();
        var result = DataLayerConnection.insertUpdateDataDB(sql);
        System.out.println(result);
        if (result != 1) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }

    public Storage.AdaptorModel updateAdaptor(Storage.AdaptorModel model) {
        var sql = update(table)
                .columns(storageTypeCol, nameCol, versionCol, urlCol, pathCol, driverCol)
                .values(model.getStorageType(), model.getName(), model.getVersion(), model.getUrl(), model.getPath(), model.getDriver())
                .where(Equal.of(idCol, model.getId()))
                .generate().getStatement();
        var result = DataLayerConnection.insertUpdateDataDB(sql);
        System.out.println(result);
        if (result != 1) {
            throw new RuntimeException("update fail");
        }
        return model;
    }

}
