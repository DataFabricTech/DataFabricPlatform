package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.model.DataStorageTable;
import com.mobigen.datafabric.core.model.StorageAutoAddSettingTable;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.sqlgen.maker.JoinMaker;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertDataOfDataLayer;
import static com.mobigen.sqlgen.SqlBuilder.insert;
import static com.mobigen.sqlgen.SqlBuilder.select;

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
    DataStorageAdaptorTable adaptorTable = new DataStorageAdaptorTable();

    DataLayerConnection dataLayerConnection = new DataLayerConnection(true);

    private JoinMaker getDataStorageStmt() {
        return select(
                dataStorageTable.getId(),
                dataStorageTable.getName(),
                adaptorTable.getStorageTypeNameCol()
        )
                .from(dataStorageTable.getTable())
                .join(adaptorTable.getTable(),
                        JoinMethod.LEFT,
                        Equal.of(dataStorageTable.getAdaptorId(), adaptorTable.getIdCol())
                );
    }

    private StorageOuterClass.Storage convertStorage(DataLayer.Row row, DataLayer.Table tableData) {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        for (var cell : row.getCellList()) {
            var cellHead = tableData.getColumnsList().get(cell.getColumnIndex());
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
            result.add(convertStorage(row, tableData));
        }
        return result;
    }


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
        return convertStorage(row, tableData);
    }

    public void addStorage(StorageOuterClass.Storage inputData) {
        var dataStorageSql = insert(dataStorageTable.getTable())
                .columns(
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
                        dataStorageTable.getMonitoringFailThreshold()
                )
                .values(
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
                        inputData.getSettings().getMonitoringSetting().getFailThreshold()
                )
                .generate()
                .getStatement();

        var authAddSettingSqlBuilder = insert(storageAutoAddSettingTable.getTable())
                .columns(
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
                            s.getRegex(),
                            s.getDataType(),
                            s.getDataFormat(),
                            s.getMinSize(),
                            s.getMaxSize(),
                            s.getStartDate(),
                            s.getEndDate()
                    );
        }
        var authAddSettingSql = authAddSettingSqlBuilder.generate().getStatement();

        var result = dataLayerConnection.executeBatch(dataStorageSql, authAddSettingSql);
        log.info("insert result: " + result.getDataList());
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
