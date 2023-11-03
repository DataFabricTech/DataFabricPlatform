package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.model.*;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.core.util.Tuple;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.configuration.Config;
import com.mobigen.sqlgen.maker.JoinMaker;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertDataOfDataLayer;
import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;
import static com.mobigen.sqlgen.maker.DeleteMaker.delete;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class DataStorageService {
    DataStorageTable dataStorageTable = new DataStorageTable();
    StorageAutoAddSettingTable storageAutoAddSettingTable = new StorageAutoAddSettingTable();
    ConnInfoTable connInfoTable = new ConnInfoTable();
    DataStorageAdaptorTable adaptorTable = new DataStorageAdaptorTable();
    DataStorageTagTable dataStorageTagTable = new DataStorageTagTable();
    DataStorageMetadataTable dataStorageMetadataTable = new DataStorageMetadataTable();

    static Config config = new Config();
    DataLayerConnection dataLayerConnection;

    public DataStorageService() {
        this(new DataLayerConnection(
                config.getConfig().getBoolean("data-layer.test", false)
        ));
    }

    public DataStorageService(DataLayerConnection dataLayerConnection) {
        this.dataLayerConnection = dataLayerConnection;
    }

    private JoinMaker getDataStorageStmt() {
        return select(
                dataStorageTable.getId(),
                dataStorageTable.getName(),
                adaptorTable.getStorageTypeName()
        )
                .from(dataStorageTable.getTable())
                .join(adaptorTable.getTable(),
                        JoinMethod.LEFT,
                        Equal.of(dataStorageTable.getAdaptorId(), adaptorTable.getId())
                );
    }

    private StorageOuterClass.Storage convertStorage(DataLayer.Row row, List<DataLayer.Column> columns) {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        for (var cell : row.getCellList()) {
            var cellHead = columns.get(cell.getColumnIndex());
            var data = convertDataOfDataLayer(cellHead, cell);
            var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
            if (colNameCaseIgnore.equals(dataStorageTable.getId().getName())) {
                storageBuilder.setId((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getAdaptorId().getName())) {
                storageBuilder.setAdaptorId((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getName().getName())) {
                storageBuilder.setName((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getUserDesc().getName())) {
                storageBuilder.setDescription((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getStatus().getName())) {
                storageBuilder.setStatus(Utilities.Status.valueOf((String) data));
            }
        }
        return storageBuilder.build();
    }

    @Deprecated
    public List<StorageOuterClass.Storage> getStorageList() {
        var dataStorageSql = getDataStorageStmt()
                .generate()
                .getStatement();
        var systemMetadataSql = "";
        var userMetadataSql = "";
        var storageTagSql = "";
        var autoAddSettingSql = storageAutoAddSettingTable.selectAll()
                .generate()
                .getStatement();

        // TODO: DataLayer 연동
        // storage builder 를 이용 해서 data storage 데이터를 넣고,
        // id 로 순회하면서 setting 정보를 넣어야함
//        var dbResult = dataLayerConnection.execute(dataStorageSql);
        var dbResult = dataLayerConnection.getStorage(dataStorageSql);

        var tableData = dbResult.getData().getTable();
        List<StorageOuterClass.Storage> result = new ArrayList<>();
        for (var row : tableData.getRowsList()) {
            result.add(convertStorage(row, tableData.getColumnsList()));
        }
        return result;
    }


    @Deprecated
    public StorageOuterClass.Storage getStorage(String id) {
        var dataStorageSql = getDataStorageStmt()
                .where(Equal.of(dataStorageTable.getId(), id))
                .generate()
                .getStatement();
        var systemMetadataSql = "";
        var userMetadataSql = "";
        var storageTagSql = "";
        var autoAddSettingSql = storageAutoAddSettingTable.selectAll()
                .where(Equal.of(storageAutoAddSettingTable.getDatastorageId(), id))
                .generate()
                .getStatement();

        // TODO: DataLayer 연동
        // storage builder 를 이용 해서 data storage 데이터를 넣고,
        // id 로 순회하면서 setting 정보를 넣어야함
//        var dbResult = dataLayerConnection.execute(dataStorageSql);
        var dbResult = dataLayerConnection.getStorage(dataStorageSql);

        var tableData = dbResult.getData().getTable();
        var row = tableData.getRows(0);
        return convertStorage(row, tableData.getColumnsList());
    }

    private StorageOuterClass.Storage.Builder getStorageBuilder(
            List<DataLayer.Column> columns,
            DataLayer.Row row
    ) {
        StorageOuterClass.Storage.Builder builder = StorageOuterClass.Storage.newBuilder();
        for (var cell : row.getCellList()) {
            var cellHead = columns.get(cell.getColumnIndex());
            var data = convertDataOfDataLayer(cellHead, cell);
            var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
            if (colNameCaseIgnore.equals(dataStorageTable.getId().getName())) {
                builder.setId((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getAdaptorId().getName())) {
                builder.setAdaptorId((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getName().getName())) {
                builder.setName((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getUserDesc().getName())) {
                builder.setDescription((String) data);
            } else if (colNameCaseIgnore.equals(dataStorageTable.getStatus().getName())) {
                if (data == "") {
                    data = "INIT";
                }
                builder.setStatus(Utilities.Status.valueOf((String) data));
            } else if (colNameCaseIgnore.equals(dataStorageTable.getUrl().getName())) {
                builder.setUrl((String) data);
            }
        }
        return builder;
    }

    private Tuple<List<Utilities.Meta>, List<Utilities.Meta>> getMetas(
            List<DataLayer.Column> columns,
            List<DataLayer.Row> rows
    ) {
        List<Utilities.Meta> systemMeta = new ArrayList<>();
        List<Utilities.Meta> userMeta = new ArrayList<>();

        for (var row : rows) {
            var metaBuilder = Utilities.Meta.newBuilder();
            var isSystem = true;
            for (var cell : row.getCellList()) {
                var cellHead = columns.get(cell.getColumnIndex());
                var data = convertDataOfDataLayer(cellHead, cell);
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if (colNameCaseIgnore.equals(dataStorageMetadataTable.getKey().getName())) {
                    metaBuilder.setKey((String) data);
                } else if (colNameCaseIgnore.equals(dataStorageMetadataTable.getValue().getName())) {
                    metaBuilder.setValue((String) data);
                } else if (colNameCaseIgnore.equals(dataStorageMetadataTable.getIsSystem().getName())) {
                    isSystem = (boolean) data;
                }
            }
            if (isSystem) {
                systemMeta.add(metaBuilder.build());
            } else {
                userMeta.add(metaBuilder.build());
            }
        }
        return new Tuple<>(systemMeta, userMeta);
    }

    private Tuple<List<StorageCommon.InputField>, List<StorageCommon.InputField>> getConnectionOptions(
            List<DataLayer.Column> columns,
            List<DataLayer.Row> rows
    ) {
        List<StorageCommon.InputField> basic = new ArrayList<>();
        List<StorageCommon.InputField> additional = new ArrayList<>();
        for (var row : rows) {
            var builder = StorageCommon.InputField.newBuilder();
            var isBasic = false;
            for (var cell : row.getCellList()) {
                var cellHead = columns.get(cell.getColumnIndex());
                var data = convertDataOfDataLayer(cellHead, cell);
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if (colNameCaseIgnore.equals(connInfoTable.getKey().getName())) {
                    builder.setKey((String) data);
                } else if (colNameCaseIgnore.equals(connInfoTable.getValue().getName())) {
                    builder.setValue((String) data);
                } else if (colNameCaseIgnore.equals(connInfoTable.getType().getName())) {
                    isBasic = (boolean) data;
                }
            }
            if (isBasic) {
                basic.add(builder.build());
            } else {
                additional.add(builder.build());
            }
        }
        return new Tuple<>(basic, additional);
    }

    private List<String> getTags(List<DataLayer.Column> columns,
                                 List<DataLayer.Row> rows) {
        List<String> tags = new ArrayList<>();
        for (var row : rows) {
            for (var cell : row.getCellList()) {
                var cellHead = columns.get(cell.getColumnIndex());
                var data = convertDataOfDataLayer(cellHead, cell);
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if (colNameCaseIgnore.equals(dataStorageTagTable.getTag().getName())) {
                    tags.add((String) data);
                }
            }
        }
        return tags;
    }

    public List<StorageOuterClass.Storage> search() {
        var sqlBuilder = select(
                dataStorageTable.getId(),
                dataStorageTable.getName(),
                dataStorageTable.getStatus(),
                adaptorTable.getStorageTypeName()
        )
                .from(dataStorageTable.getTable())
                .join(adaptorTable.getTable(),
                        JoinMethod.LEFT,
                        Equal.of(dataStorageTable.getAdaptorId(), adaptorTable.getId()));

        var dbResult = dataLayerConnection.execute(sqlBuilder.generate().getStatement());
        var tableData = dbResult.getData().getTable();
        List<StorageOuterClass.Storage> result = new ArrayList<>();
        for (var row : tableData.getRowsList()) {
            var storageBuilder = getStorageBuilder(tableData.getColumnsList(), row);
            storageBuilder.setStorageType(row.getCell(3).getStringValue());
            result.add(storageBuilder.build());
        }
        return result;
    }

    public StorageOuterClass.Storage status(String id) {
        var dataStorageSql = select().from(dataStorageTable.getTable())
                .where(Equal.of(dataStorageTable.getId(), id))
                .generate().getStatement();
        var metadataSql = select().from(dataStorageMetadataTable.getTable())
                .where(Equal.of(dataStorageMetadataTable.getDatastorageId(), id))
                .generate().getStatement();
        var connInfoSql = select().from(connInfoTable.getTable())
                .where(Equal.of(connInfoTable.getDatastorageId(), id))
                .generate().getStatement();
        var storageAutoAddSettingSql = select().from(connInfoTable.getTable())
                .where(Equal.of(connInfoTable.getDatastorageId(), id))
                .generate().getStatement();
        var tagSql = select().from(dataStorageTagTable.getTable())
                .where(Equal.of(dataStorageTagTable.getDatastorageId(), id))
                .generate().getStatement();

        var result = dataLayerConnection.execute(dataStorageSql).getData().getTable();

        if (result.getRowsCount() == 0) {
            throw new RuntimeException("No data of id = " + id);
        }
        var storageBuilder = getStorageBuilder(
                result.getColumnsList(),
                result.getRows(0)
        );

        result = dataLayerConnection.execute(metadataSql).getData().getTable();
        var metaTuple = getMetas(result.getColumnsList(), result.getRowsList());
        storageBuilder.addAllSystemMeta(metaTuple.getLeft());
        storageBuilder.addAllUserMeta(metaTuple.getRight());

        result = dataLayerConnection.execute(connInfoSql).getData().getTable();
        var connInfoTuple = getConnectionOptions(result.getColumnsList(), result.getRowsList());
        storageBuilder.addAllBasicOptions(connInfoTuple.getLeft());
        storageBuilder.addAllAdditionalOptions(connInfoTuple.getRight());

        result = dataLayerConnection.execute(tagSql).getData().getTable();
        storageBuilder.addAllTags(getTags(result.getColumnsList(), result.getRowsList()));

        return storageBuilder.build();
    }


    public void addStorage(StorageOuterClass.Storage inputData) {
        List<String> sqlList = new ArrayList<>();
        var id = UUID.randomUUID().toString();
        var dataStorageSql = insert(dataStorageTable.getTable())
                .columns(
                        dataStorageTable.getId(),
                        dataStorageTable.getAdaptorId(),
                        dataStorageTable.getName(),
                        dataStorageTable.getUrl(),
                        dataStorageTable.getUserDesc(),

                        dataStorageTable.getSyncEnable(),
                        dataStorageTable.getSyncType(),
                        dataStorageTable.getSyncWeek(),
                        dataStorageTable.getSyncRunTime(),

                        dataStorageTable.getMonitoringEnable(),
                        dataStorageTable.getMonitoringProtocol(),
                        dataStorageTable.getMonitoringHost(),
                        dataStorageTable.getMonitoringPort(),
                        dataStorageTable.getMonitoringSql(),
                        dataStorageTable.getMonitoringPeriod(),
                        dataStorageTable.getMonitoringTimeout(),
                        dataStorageTable.getMonitoringSuccessThreshold(),
                        dataStorageTable.getMonitoringFailThreshold(),

                        dataStorageTable.getAutoAddSettingEnable()
                )
                .values(
                        id,
                        inputData.getAdaptorId(),
                        inputData.getName(),
                        inputData.getUrl(),
                        inputData.getDescription(),

                        inputData.getSettings().getSyncSetting().getEnable(),
                        inputData.getSettings().getSyncSetting().getSyncType(),
                        inputData.getSettings().getSyncSetting().getWeek(),
                        inputData.getSettings().getSyncSetting().getRunTime(),

                        inputData.getSettings().getMonitoringSetting().getEnable(),
                        inputData.getSettings().getMonitoringSetting().getProtocol().getValueDescriptor().getName(),
                        inputData.getSettings().getMonitoringSetting().getHost(),
                        inputData.getSettings().getMonitoringSetting().getPort(),
                        inputData.getSettings().getMonitoringSetting().getSql(),
                        inputData.getSettings().getMonitoringSetting().getPeriod(),
                        inputData.getSettings().getMonitoringSetting().getTimeout(),
                        inputData.getSettings().getMonitoringSetting().getSuccessThreshold(),
                        inputData.getSettings().getMonitoringSetting().getFailThreshold(),

                        inputData.getSettings().getAutoAddSetting().getEnable()
                )
                .generate()
                .getStatement();
        sqlList.add(dataStorageSql);

        if (inputData.getSettings().getAutoAddSetting().getOptionsCount() > 0) {
            var authAddSettingSqlBuilder = insert(storageAutoAddSettingTable.getTable())
                    .columns(
                            storageAutoAddSettingTable.getDatastorageId(),
                            storageAutoAddSettingTable.getRegex(),
                            storageAutoAddSettingTable.getDataType(),
                            storageAutoAddSettingTable.getDataFormat(),
                            storageAutoAddSettingTable.getMinSize(),
                            storageAutoAddSettingTable.getMaxSize(),
                            storageAutoAddSettingTable.getStartDate(),
                            storageAutoAddSettingTable.getEndDate()
                    );
            for (var s : inputData.getSettings().getAutoAddSetting().getOptionsList()) {
                authAddSettingSqlBuilder = authAddSettingSqlBuilder
                        .values(
                                id,
                                s.getRegex(),
                                s.getDataType(),
                                s.getDataFormat(),
                                s.getMinSize(),
                                s.getMaxSize(),
                                s.getStartDate(),
                                s.getEndDate()
                        );
            }
            sqlList.add(authAddSettingSqlBuilder.generate().getStatement());
        }

        if (inputData.getBasicOptionsCount() > 0 || inputData.getAdditionalOptionsCount() > 0) {
            var connInfoSqlBuilder = insert(connInfoTable.getTable())
                    .columns(
                            connInfoTable.getDatastorageId(),
                            connInfoTable.getKey(),
                            connInfoTable.getType(),
                            connInfoTable.getValue(),
                            connInfoTable.getRequired()
                    );

            for (var c : inputData.getBasicOptionsList()) {
                connInfoSqlBuilder = connInfoSqlBuilder
                        .values(
                                id,
                                c.getKey(),
                                c.getValueType().getValueDescriptor().getName(),
                                c.getValue(),
                                true
                        );
            }
            for (var c : inputData.getAdditionalOptionsList()) {
                connInfoSqlBuilder = connInfoSqlBuilder
                        .values(
                                id,
                                c.getKey(),
                                c.getValueType().getValueDescriptor().getName(),
                                c.getValue(),
                                false
                        );
            }
            sqlList.add(connInfoSqlBuilder.generate().getStatement());
        }

        if (inputData.getTagsCount() > 0) {
            var tagSqlBuilder = insert(dataStorageTagTable.getTable())
                    .columns(dataStorageTagTable.getDatastorageId(),
                            dataStorageTagTable.getTag());
            for (var t : inputData.getTagsList()) {
                tagSqlBuilder = tagSqlBuilder.values(id, t);
            }
            sqlList.add(tagSqlBuilder.generate().getStatement());
        }

        if (inputData.getUserMetaCount() > 0) {
            var metadataSqlBuilder = insert(dataStorageMetadataTable.getTable())
                    .columns(dataStorageMetadataTable.getDatastorageId(),
                            dataStorageMetadataTable.getKey(),
                            dataStorageMetadataTable.getValue(),
                            dataStorageMetadataTable.getIsSystem()
                    );
            for (var m : inputData.getUserMetaList()) {
                metadataSqlBuilder = metadataSqlBuilder.values(
                        id,
                        m.getKey(),
                        m.getValue(),
                        false
                );
            }
            sqlList.add(metadataSqlBuilder.generate().getStatement());
        }

        var result = dataLayerConnection.executeBatch(sqlList);
        log.info("insert result: " + result.getDataList());
        // TODO: queue 로 메세지 를 송신, event_type, id
    }

    public void deleteStorage(String id) {
        // data storage id 를 참조하는 테이블의 값은 on delete cascade 이므로 함께 삭제됨
        var sql = delete(dataStorageTable.getTable())
                .where(Equal.of(dataStorageTable.getId(), id))
                .generate()
                .getStatement();
        var result = dataLayerConnection.execute(sql);
        log.info("delete result: " + result.getData().getResponse());
    }

//    public List<Map<String, String>> getStorageList() {
//        var sql = select(
//                dataStorageTable.getId(),
//                dataStorageTable.getName(),
//                adaptorTable.getStorageTypeNameCol()
//        )
//                .from(dataStorageTable.getTable())
//                .join(adaptorTable.getTable(),
//                        JoinMethod.LEFT,
//                        Equal.of(dataStorageTable.getAdaptor(), adaptorTable.getIdCol())
//                )
//                .generate()
//                .getStatement();
//        // TODO: DataLayer 연동
//
//        return List.of(
//                Map.of(
//                        "id", "111",
//                        "name", "iris",
//                        "storageType", "iris",
//                        "status", "connected"
//                )
//        );
//    }
}
