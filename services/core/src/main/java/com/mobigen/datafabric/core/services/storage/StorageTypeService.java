package com.mobigen.datafabric.core.services.storage;

import com.mobigen.libs.grpc.DataLayer;
import com.mobigen.libs.grpc.Storage.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StorageTypeService {
    public List<StorageTypeModel> getStorageTypeModels(String id) {
        var resOfDataLayer = getData("select id, name from DataStorageType where id = id;");
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
        var resOfDataLayer = getData("insert into DataStorageType(name) values (name)");
        var row = resOfDataLayer.getRows(0);
        return StorageTypeModel.newBuilder()
                .setId(row.getRow(0).getStringValue())
                .setName(row.getRow(1).getStringValue())
                .build();
    }

    private DataLayer.QueryGRPCResponseMessage getData(String sql) {

        var req = DataLayer.QueryGRPCRequestMessage.newBuilder()
                .setQuery(sql)
                .build();
        return DataLayer.QueryGRPCResponseMessage.newBuilder()
                .addAllColumn(List.of(
                        DataLayer.Column.newBuilder().setColumnName("id").setType("string").build(),
                        DataLayer.Column.newBuilder().setColumnName("name").setType("string").build()
                ))
                .addAllRows(List.of(
                        DataLayer.Rows.newBuilder().addAllRow(
                                List.of(
                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
                                        DataLayer.Cell.newBuilder().setStringValue("mysql").build()
                                )
                        ).build(),
                        DataLayer.Rows.newBuilder().addAllRow(
                                List.of(
                                        DataLayer.Cell.newBuilder().setStringValue(UUID.randomUUID().toString()).build(),
                                        DataLayer.Cell.newBuilder().setStringValue("postgresql").build()
                                )
                        ).build()
                ))
                .build();
    }
}
