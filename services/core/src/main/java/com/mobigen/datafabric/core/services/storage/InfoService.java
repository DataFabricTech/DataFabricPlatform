package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.libs.grpc.Storage;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

public class InfoService {
    SqlTable table = SqlTable.of("DataStorage");
    SqlColumn idCol = SqlColumn.of("id", table, JDBCType.VARCHAR);
    SqlColumn storageTypeCol = SqlColumn.of("type", table, JDBCType.VARCHAR);
    SqlColumn adaptorCol = SqlColumn.of("adaptor", table, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);
    SqlColumn userDescCol = SqlColumn.of("user_desc", table, JDBCType.VARCHAR);
    SqlColumn totalDataCol = SqlColumn.of("total_data", table, JDBCType.INTEGER);
    SqlColumn regiDataCol = SqlColumn.of("regi_data", table, JDBCType.INTEGER);
    SqlColumn createdByCol = SqlColumn.of("created_by", table, JDBCType.VARCHAR);
    SqlColumn createdAtCol = SqlColumn.of("created_at", table, JDBCType.TIMESTAMP);
    SqlColumn updatedByCol = SqlColumn.of("updated_by", table, JDBCType.VARCHAR);
    SqlColumn updatedAtCol = SqlColumn.of("updated_at", table, JDBCType.TIMESTAMP);
    SqlColumn deletedByCol = SqlColumn.of("deleted_by", table, JDBCType.VARCHAR);
    SqlColumn deletedAtCol = SqlColumn.of("deleted_at", table, JDBCType.TIMESTAMP);
    SqlColumn statusCol = SqlColumn.of("status", table, JDBCType.INTEGER);
    SqlColumn lastConnectionCheckedAtCol = SqlColumn.of("last_connection_checked_at", table, JDBCType.TIMESTAMP);
    SqlColumn lastSyncAtCol = SqlColumn.of("last_sync_at", table, JDBCType.TIMESTAMP);

    public List<Storage.InfoModel> getInfos() {
        var sql = select(idCol, storageTypeCol, adaptorCol, nameCol, userDescCol, totalDataCol, regiDataCol, statusCol)
                .from(table).generate().getStatement();
        var result = DataLayerConnection.getDataDB(sql);
        List<Storage.InfoModel> models = new ArrayList<>();
        try {
            while (result.next()) {
                models.add(Storage.InfoModel.newBuilder()
                        .setId(result.getString(1))
                        .setStorageType(result.getString(2))
                        .setAdaptorId(result.getString(3))
                        .setName(result.getString(4))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return models;
    }

    public Storage.InfoModel createInfo(Storage.InfoModel model) {
        var sql = insert(table)
                .columns(idCol, storageTypeCol, adaptorCol, nameCol)
                .values(UUID.randomUUID().toString(), model.getStorageType(), model.getAdaptorId(), model.getName())
                .generate().getStatement();
        var result = DataLayerConnection.insertUpdateDataDB(sql);
        System.out.println(result);
        if (result != 1) {
            throw new RuntimeException("insert fail");
        }
        return model;
    }
}
