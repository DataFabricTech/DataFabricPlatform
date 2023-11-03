package com.mobigen.datafabric.core.services.storage;

import com.google.protobuf.ByteString;
import com.mobigen.datafabric.core.model.ConnectionSchemaTable;
import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.model.DataStorageTypeTable;
import com.mobigen.datafabric.core.model.UrlFormatTable;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.Utilities;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.mobigen.sqlgen.SqlBuilder.select;

/**
 * Adaptor Service
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class AdaptorService {

    DataStorageTypeTable dataStorageTypeTable = new DataStorageTypeTable();
    DataStorageAdaptorTable adaptorTable = new DataStorageAdaptorTable();
    UrlFormatTable urlFormatTable = new UrlFormatTable();
    ConnectionSchemaTable connectionSchemaTable = new ConnectionSchemaTable();


    public List<AdaptorOuterClass.SupportedStorageType> getStorageTypes() {
        var sql = select(dataStorageTypeTable.getNameCol(), dataStorageTypeTable.getIconCol())
                .from(dataStorageTypeTable.getTable())
                .generate()
                .getStatement();
        log.info("sql: " + sql);

        // TODO: Connect Datalayer Service

        return List.of(
                AdaptorOuterClass.SupportedStorageType.newBuilder()
                        .setName("postgres")
                        .setIcon(ByteString.copyFrom("test".getBytes()))
                        .build()
        );
    }

    public List<AdaptorOuterClass.Adaptor> getAdaptors() {
        var adaptorData = select(
                adaptorTable.getId(),
                adaptorTable.getStorageTypeName(),
                adaptorTable.getName(),
                adaptorTable.getVersion(),
                adaptorTable.getPath(),
                adaptorTable.getDriver()
        )
                .from(adaptorTable.getTable())
                .generate()
                .getStatement();
        var urlFormats = select(
                urlFormatTable.getAdaptorId(),
                urlFormatTable.getFormat()
        )
                .from(urlFormatTable.getTable())
                .generate()
                .getStatement();
        var connectionSchemaData = select(
                connectionSchemaTable.getAdaptorIdCol(),
                connectionSchemaTable.getKeyCol(),
                connectionSchemaTable.getTypeCol(),
                connectionSchemaTable.getDefaultCol(),
                connectionSchemaTable.getRequiredCol()
        )
                .from(connectionSchemaTable.getTable())
                .generate()
                .getStatement();
        // TODO: Connect Datalayer Service
        return List.of(
                AdaptorOuterClass.Adaptor.newBuilder()
                        .setId("244e4f0f-e3e5-4c54-a04b-61b83a1f09af")
                        .setStorageType("postgresql")
                        .setName("Postgres Adaptor")
                        .setVersion("16.1-fwani")
                        .setPath("/tmp/postgresql-16.1.jar")
                        .setClass_("org.postgresql.Driver")
                        .addAllSupportedURL(List.of(
                                "jdbc:postgresql://{host}:{port}/{database}"
                        ))
                        .addAllBasicOptions(List.of(
                                StorageCommon.InputField.newBuilder()
                                        .setKey("host")
                                        .setRequired(true)
                                        .setValueType(Utilities.DataType.STRING)
                                        .setDefault("localhost")
                                        .setDescription("host name")
                                        .setValue("localhost")
                                        .build(),
                                StorageCommon.InputField.newBuilder()
                                        .setKey("port")
                                        .setRequired(true)
                                        .setValueType(Utilities.DataType.INT32)
                                        .setDefault("5432")
                                        .setDescription("port")
                                        .setValue("5432")
                                        .build(),
                                StorageCommon.InputField.newBuilder()
                                        .setKey("database")
                                        .setRequired(true)
                                        .setValueType(Utilities.DataType.STRING)
                                        .setDefault("postgres")
                                        .setDescription("host name")
                                        .setValue("postgres")
                                        .build()
                        ))
                        .addAllAdditionalOptions(List.of(
                                StorageCommon.InputField.newBuilder()
                                        .setKey("user")
                                        .setRequired(true)
                                        .setValueType(Utilities.DataType.STRING)
                                        .setDescription("user name")
                                        .setValue("postgres")
                                        .build(),
                                StorageCommon.InputField.newBuilder()
                                        .setKey("password")
                                        .setRequired(true)
                                        .setValueType(Utilities.DataType.STRING)
                                        .setDescription("password")
                                        .setValue("test")
                                        .build()
                        ))
                        .build()
        );
    }
}
