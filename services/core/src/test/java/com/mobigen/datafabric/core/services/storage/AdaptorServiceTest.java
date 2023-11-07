package com.mobigen.datafabric.core.services.storage;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.core.model.ConnectionSchemaTable;
import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.model.UrlFormatTable;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.Utilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class AdaptorServiceTest {
    DataLayerConnection dataLayerConnection = mock(DataLayerConnection.class);
    AdaptorService adaptorService = new AdaptorService(dataLayerConnection);

    @Test
    void getStorageTypesTest() throws IOException {
        var name = "postgresql";
        var postgresqlIcon = getClass().getResourceAsStream("/sql/icon-postgresql.png").readAllBytes();

        when(dataLayerConnection.execute(any()))
                .thenReturn(DataLayer.ResExecute.newBuilder()
                        .setData(DataLayer.ResExecute.Data.newBuilder()
                                .setTable(DataLayer.Table.newBuilder()
                                        .addAllRows(List.of(
                                                DataLayer.Row.newBuilder()
                                                        .addAllCell(List.of(
                                                                DataLayer.Cell.newBuilder()
                                                                        .setStringValue(name)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setBytesValue(ByteString.copyFrom(postgresqlIcon))
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build())
                                .build())
                        .build());

        var expect = List.of(
                AdaptorOuterClass.SupportedStorageType.newBuilder()
                        .setName(name)
                        .setIcon(ByteString.copyFrom(postgresqlIcon))
                        .build()
        );
        var result = adaptorService.getStorageTypes();

        assertEquals(expect, result);
    }

    @Test
    void getAdaptorsTest() {
        var id = UUID.randomUUID().toString();
        var storageTypeName = "postgresql";
        var name = "postgresql adaptor";
        var version = "v1";
        var path = "/tmp/aaa.jar";
        var driver = "org.postgresql.Driver";
        var url = "jdbc:postgresql://{host}:{port}/{database}";

        var adaptorTable = new DataStorageAdaptorTable();
        var urlFormatTable = new UrlFormatTable();
        var connectionSchemaTable = new ConnectionSchemaTable();

        when(dataLayerConnection.execute(any()))
                .thenReturn(DataLayer.ResExecute.newBuilder()
                        .setData(DataLayer.ResExecute.Data.newBuilder()
                                .setTable(DataLayer.Table.newBuilder()
                                        .addAllColumns(List.of(
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getId().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getStorageTypeName().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getName().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getVersion().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getPath().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(adaptorTable.getDriver().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(urlFormatTable.getFormat().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(connectionSchemaTable.getKey().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(connectionSchemaTable.getType().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(connectionSchemaTable.getDefaultCol().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(connectionSchemaTable.getRequired().getName())
                                                        .build(),
                                                DataLayer.Column.newBuilder()
                                                        .setColumnName(connectionSchemaTable.getBasic().getName())
                                                        .build()
                                        ))
                                        .addAllRows(List.of(
                                                DataLayer.Row.newBuilder()
                                                        .addAllCell(List.of(
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(0)
                                                                        .setStringValue(id)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(1)
                                                                        .setStringValue(storageTypeName)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(2)
                                                                        .setStringValue(name)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(3)
                                                                        .setStringValue(version)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(4)
                                                                        .setStringValue(path)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(5)
                                                                        .setStringValue(driver)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(6)
                                                                        .setStringValue(url)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(7)
                                                                        .setStringValue("host")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(8)
                                                                        .setStringValue("STRING")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(9)
                                                                        .setStringValue("localhost")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(10)
                                                                        .setBoolValue(true)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(11)
                                                                        .setBoolValue(true)
                                                                        .build()

                                                        ))
                                                        .build(),
                                                DataLayer.Row.newBuilder()
                                                        .addAllCell(List.of(
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(0)
                                                                        .setStringValue(id)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(1)
                                                                        .setStringValue(storageTypeName)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(2)
                                                                        .setStringValue(name)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(3)
                                                                        .setStringValue(version)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(4)
                                                                        .setStringValue(path)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(5)
                                                                        .setStringValue(driver)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(6)
                                                                        .setStringValue(url)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(7)
                                                                        .setStringValue("port")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(8)
                                                                        .setStringValue("INT32")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(9)
                                                                        .setStringValue("5432")
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(10)
                                                                        .setBoolValue(true)
                                                                        .build(),
                                                                DataLayer.Cell.newBuilder()
                                                                        .setColumnIndex(11)
                                                                        .setBoolValue(true)
                                                                        .build()

                                                        ))
                                                        .build()
                                        ))
                                        .build())
                                .build())
                        .build());

        var expect = List.of(
                AdaptorOuterClass.Adaptor.newBuilder()
                        .setId(id)
                        .setStorageType(storageTypeName)
                        .setName(name)
                        .setVersion(version)
                        .setPath(path)
                        .setClass_(driver)
                        .addAllSupportedURL(Set.of(
                                url
                        ))
                        .addAllBasicOptions(Set.of(
                                StorageCommon.InputField.newBuilder()
                                        .setKey("host")
                                        .setValueType(Utilities.DataType.STRING)
                                        .setDefault("localhost")
                                        .setRequired(true)
                                        .build(),
                                StorageCommon.InputField.newBuilder()
                                        .setKey("port")
                                        .setValueType(Utilities.DataType.INT32)
                                        .setDefault("5432")
                                        .setRequired(true)
                                        .build()
                        ))
                        .build()
        );
        var result = adaptorService.getAdaptors();

        assertEquals(expect, result);
    }
}