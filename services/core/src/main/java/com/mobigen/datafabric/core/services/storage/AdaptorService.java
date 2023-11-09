package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.model.ConnectionSchemaTable;
import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.model.DataStorageTypeTable;
import com.mobigen.datafabric.core.model.UrlFormatTable;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.configuration.Config;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

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
    static Config config = new Config();
    DataLayerConnection dataLayerConnection;

    public AdaptorService() {
        this(new DataLayerConnection(
                config.getConfig().getBoolean("data-layer.test", false)
        ));
    }

    public AdaptorService(DataLayerConnection dataLayerConnection) {
        this.dataLayerConnection = dataLayerConnection;
    }


    public List<AdaptorOuterClass.SupportedStorageType> getStorageTypes() {
        var sql = select(DataStorageTypeTable.nameCol, DataStorageTypeTable.iconCol)
                .from(DataStorageTypeTable.table)
                .generate()
                .getStatement();
        log.info("sql: " + sql);

        var result = dataLayerConnection.execute(sql);
        var resultTable = result.getData().getTable();

        return resultTable.getRowsList().stream().map(row -> AdaptorOuterClass.SupportedStorageType.newBuilder()
                        .setName(row.getCellOrBuilder(0).getStringValue())
                        .setIcon(row.getCellOrBuilder(1).getBytesValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AdaptorOuterClass.Adaptor> getAdaptors() {
        var sql = select(
                DataStorageAdaptorTable.id,
                DataStorageAdaptorTable.storageTypeName,
                DataStorageAdaptorTable.name,
                DataStorageAdaptorTable.version,
                DataStorageAdaptorTable.path,
                DataStorageAdaptorTable.driver,

                UrlFormatTable.format,

                ConnectionSchemaTable.key,
                ConnectionSchemaTable.type,
                ConnectionSchemaTable.defaultCol,
                ConnectionSchemaTable.required,
                ConnectionSchemaTable.basic
        )
                .from(DataStorageAdaptorTable.table)
                .join(UrlFormatTable.table,
                        JoinMethod.LEFT,
                        Equal.of(DataStorageAdaptorTable.id, UrlFormatTable.adaptorId))
                .join(ConnectionSchemaTable.table,
                        JoinMethod.LEFT,
                        Equal.of(DataStorageAdaptorTable.id, ConnectionSchemaTable.adaptorId))
                .generate()
                .getStatement();

        var result = dataLayerConnection.execute(sql);
        var resultTable = result.getData().getTable();
        Map<String, AdaptorOuterClass.Adaptor.Builder> adaptorBuilder = new HashMap<>();
        HashMap<String, Set<String>> urls = new HashMap<>();
        Map<String, Set<StorageCommon.InputField>> basic = new HashMap<>();
        Map<String, Set<StorageCommon.InputField>> additional = new HashMap<>();

        for (var row : resultTable.getRowsList()) {
            String idOfRow = row.getCell(0).getStringValue();

            if (!adaptorBuilder.containsKey(idOfRow)) {
                adaptorBuilder.put(idOfRow, AdaptorOuterClass.Adaptor.newBuilder());
            }
            if (!urls.containsKey(idOfRow)) {
                urls.put(idOfRow, new HashSet<>());
            }
            if (!basic.containsKey(idOfRow)) {
                basic.put(idOfRow, new HashSet<>());
            }
            if (!additional.containsKey(idOfRow)) {
                additional.put(idOfRow, new HashSet<>());
            }
            var isBasic = false;
            var inputFieldBuilder = StorageCommon.InputField.newBuilder();
            for (var cell : row.getCellList()) {
                var header = resultTable.getColumnsList().get(cell.getColumnIndex());
                if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.id.getName())) {
                    adaptorBuilder.get(idOfRow).setId(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.storageTypeName.getName())) {
                    adaptorBuilder.get(idOfRow).setStorageType(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.name.getName())) {
                    adaptorBuilder.get(idOfRow).setName(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.version.getName())) {
                    adaptorBuilder.get(idOfRow).setVersion(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.path.getName())) {
                    adaptorBuilder.get(idOfRow).setPath(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(DataStorageAdaptorTable.driver.getName())) {
                    adaptorBuilder.get(idOfRow).setClass_(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(UrlFormatTable.format.getName())) {
                    if (cell.getStringValue().isBlank()) {
                        continue;
                    }
                    urls.get(idOfRow).add(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(ConnectionSchemaTable.key.getName())) {
                    if (cell.getStringValue().isBlank()) {
                        continue;
                    }
                    inputFieldBuilder.setKey(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(ConnectionSchemaTable.defaultCol.getName())) {
                    if (cell.getStringValue().isBlank()) {
                        continue;
                    }
                    inputFieldBuilder.setDefault(cell.getStringValue());
                } else if (header.getColumnName().toLowerCase().equals(ConnectionSchemaTable.required.getName())) {
                    inputFieldBuilder.setRequired(cell.getBoolValue());
                } else if (header.getColumnName().toLowerCase().equals(ConnectionSchemaTable.type.getName())) {
                    if (cell.getStringValue().isBlank()) {
                        continue;
                    }
                    inputFieldBuilder.setValueType(Utilities.DataType.valueOf(cell.getStringValue()));
                } else if (header.getColumnName().toLowerCase().equals(ConnectionSchemaTable.basic.getName())) {
                    isBasic = cell.getBoolValue();
                }
            }
            if (isBasic) {
                basic.get(idOfRow).add(inputFieldBuilder.build());
            } else {
                additional.get(idOfRow).add(inputFieldBuilder.build());
            }
        }

        for (var id : adaptorBuilder.keySet()) {
            adaptorBuilder.get(id).addAllSupportedURL(urls.get(id));
            adaptorBuilder.get(id).addAllBasicOptions(basic.get(id));
            adaptorBuilder.get(id).addAllAdditionalOptions(additional.get(id));
        }

        return adaptorBuilder.values().stream().map(AdaptorOuterClass.Adaptor.Builder::build)
                .collect(Collectors.toList());
    }
}
