package com.mobigen.datafabric.core.services.storage;

import com.mobigen.libs.grpc.Storage.StorageTypeModel;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.where.conditions.Equal;

import java.sql.JDBCType;
import java.util.List;
import java.util.stream.Collectors;

import static com.mobigen.datafabric.core.util.DataLayerConnection.getData;
import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;


public class StorageTypeService {
    SqlTable table = SqlTable.of("DataStorageType");
    SqlColumn idCol = SqlColumn.of("id", table, JDBCType.VARCHAR);
    SqlColumn nameCol = SqlColumn.of("name", table, JDBCType.VARCHAR);

    public List<StorageTypeModel> getStorageTypeModels(String id) {
        var sql = select(idCol, nameCol)
                .from(table)
                .where(Equal.of(idCol, id))
                .generate()
                .getStatement();
        var resOfDataLayer = getData(sql);
        return resOfDataLayer.getRowsList().stream().map(x -> {
            var typeId = x.getRow(0).getStringValue();
            var name = x.getRow(1).getStringValue();
            return StorageTypeModel.newBuilder()
                    .setId(typeId)
                    .setName(name)
                    .build();
        }).collect(Collectors.toList());
    }

    public StorageTypeModel createStorageType(String name) {
        var sql = insert(table)
                .columns(nameCol)
                .values(name)
                .generate()
                .getStatement();
        var resOfDataLayer = getData(sql);
        var row = resOfDataLayer.getRows(0);
        return StorageTypeModel.newBuilder()
                .setId(row.getRow(0).getStringValue())
                .setName(row.getRow(1).getStringValue())
                .build();
    }

}
