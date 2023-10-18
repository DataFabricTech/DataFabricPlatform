package com.mobigen.datafabric.core.util;

import com.mobigen.libs.grpc.DataLayer;

import java.util.List;
import java.util.UUID;

public class DataLayerConnection {

    public static DataLayer.QueryGRPCResponseMessage getData(String sql) {
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
