package com.mobigen.datafabric.core.services.storage;

import com.mobigen.datafabric.core.collector.DataCollectorFactory;
import com.mobigen.datafabric.core.model.*;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.core.util.Tuple;
import com.mobigen.datafabric.share.protobuf.DataLayer;
import com.mobigen.datafabric.share.protobuf.StorageCommon;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.configuration.Config;
import com.mobigen.sqlgen.maker.JoinMaker;
import com.mobigen.sqlgen.maker.MakerInterface;
import com.mobigen.sqlgen.maker.OrderUsable;
import com.mobigen.sqlgen.maker.WhereUsable;
import com.mobigen.sqlgen.model.JoinMethod;
import com.mobigen.sqlgen.model.SqlColumn;
import com.mobigen.sqlgen.model.SqlTable;
import com.mobigen.sqlgen.order.Order;
import com.mobigen.sqlgen.where.Condition;
import com.mobigen.sqlgen.where.conditions.Equal;
import com.mobigen.sqlgen.where.conditions.In;
import com.mobigen.sqlgen.where.conditions.Like;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertDataOfDataLayer;
import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertInputField;
import static com.mobigen.sqlgen.SqlBuilder.*;
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

    static Config config = new Config();
    DataLayerConnection dataLayerConnection;

    public DataStorageService() {
        this( new DataLayerConnection(
                config.getConfig().getBoolean( "data-layer.test", false )
        ) );
    }

    public DataStorageService( DataLayerConnection dataLayerConnection ) {
        this.dataLayerConnection = dataLayerConnection;
    }

    private JoinMaker getDataStorageStmt() {
        return select(
                DataStorageTable.id,
                DataStorageTable.name,
                DataStorageAdaptorTable.storageTypeName
        )
                .from( DataStorageTable.table )
                .join( DataStorageAdaptorTable.table,
                        JoinMethod.LEFT,
                        Equal.of( DataStorageTable.adaptorId, DataStorageAdaptorTable.id )
                );
    }

    private StorageOuterClass.Storage.Builder getStorageBuilder(
            List<DataLayer.Column> columns,
            DataLayer.Row row
    ) {
        StorageOuterClass.Storage.Builder builder = StorageOuterClass.Storage.newBuilder();
        for( int i = 0; i < columns.size(); i++ ) {
            var data = convertDataOfDataLayer( columns.get( i ), row.getCell( i ) );
            var colNameCaseIgnore = columns.get( i ).getColumnName().toLowerCase();
            if( colNameCaseIgnore.equals( DataStorageTable.id.getName() ) ) {
                builder.setId( ( String )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.adaptorId.getName() ) ) {
                builder.setAdaptorId( ( String )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.name.getName() ) ) {
                builder.setName( ( String )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.userDesc.getName() ) ) {
                builder.setDescription( ( String )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.status.getName() ) ) {
                if( data == "" ) {
                    data = "INIT";
                }
                builder.setStatus( Utilities.Status.valueOf( ( String )data ) );
            } else if( colNameCaseIgnore.equals( DataStorageTable.url.getName() ) ) {
                builder.setUrl( ( String )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.createdAt.getName() ) ) {
                builder.setCreatedAt( ( Utilities.DateTime )data );
            } else if( colNameCaseIgnore.equals( DataStorageTable.updatedAt.getName() ) ) {
                builder.setLastModifiedAt( ( Utilities.DateTime )data );
            }
        }
//        for (var cell : row.getCellList()) {
//            var cellHead = columns.get(cell.getColumnIndex());
//            var data = convertDataOfDataLayer(cellHead, cell);
//            var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
//            if (colNameCaseIgnore.equals(DataStorageTable.id.getName())) {
//                builder.setId((String) data);
//            } else if (colNameCaseIgnore.equals(DataStorageTable.adaptorId.getName())) {
//                builder.setAdaptorId((String) data);
//            } else if (colNameCaseIgnore.equals(DataStorageTable.name.getName())) {
//                builder.setName((String) data);
//            } else if (colNameCaseIgnore.equals(DataStorageTable.userDesc.getName())) {
//                builder.setDescription((String) data);
//            } else if (colNameCaseIgnore.equals(DataStorageTable.status.getName())) {
//                if (data == "") {
//                    data = "INIT";
//                }
//                builder.setStatus(Utilities.Status.valueOf((String) data));
//            } else if (colNameCaseIgnore.equals(DataStorageTable.url.getName())) {
//                builder.setUrl((String) data);
//            }
//        }
        return builder;
    }

    private Tuple<List<Utilities.Meta>, List<Utilities.Meta>> getMetas(
            List<DataLayer.Column> columns,
            List<DataLayer.Row> rows
    ) {
        List<Utilities.Meta> systemMeta = new ArrayList<>();
        List<Utilities.Meta> userMeta = new ArrayList<>();

        for( var row : rows ) {
            var metaBuilder = Utilities.Meta.newBuilder();
            var isSystem = true;
            for( var cell : row.getCellList() ) {
                var cellHead = columns.get( cell.getColumnIndex() );
                var data = convertDataOfDataLayer( cellHead, cell );
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if( colNameCaseIgnore.equals( DataStorageMetadataTable.key.getName() ) ) {
                    metaBuilder.setKey( ( String )data );
                } else if( colNameCaseIgnore.equals( DataStorageMetadataTable.value.getName() ) ) {
                    metaBuilder.setValue( ( String )data );
                } else if( colNameCaseIgnore.equals( DataStorageMetadataTable.isSystem.getName() ) ) {
//                    isSystem = (boolean) data;
                    isSystem = cell.getBoolValue();
                }
            }
            if( isSystem ) {
                systemMeta.add( metaBuilder.build() );
            } else {
                userMeta.add( metaBuilder.build() );
            }
        }
        return new Tuple<>( systemMeta, userMeta );
    }

    private Tuple<List<StorageCommon.InputField>, List<StorageCommon.InputField>> getConnectionOptions(
            List<DataLayer.Column> columns,
            List<DataLayer.Row> rows
    ) {
        List<StorageCommon.InputField> basic = new ArrayList<>();
        List<StorageCommon.InputField> additional = new ArrayList<>();
        for( var row : rows ) {
            var builder = StorageCommon.InputField.newBuilder();
            var isBasic = false;
            for( var cell : row.getCellList() ) {
                var cellHead = columns.get( cell.getColumnIndex() );
                var data = convertDataOfDataLayer( cellHead, cell );
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if( colNameCaseIgnore.equals( ConnInfoTable.key.getName() ) ) {
                    builder.setKey( ( String )data );
                } else if( colNameCaseIgnore.equals( ConnInfoTable.value.getName() ) ) {
                    builder.setValue( ( String )data );
                } else if( colNameCaseIgnore.equals( ConnInfoTable.type.getName() ) ) {
                    builder.setValueType( Utilities.DataType.valueOf( ( String )data ) );
                } else if( colNameCaseIgnore.equals( ConnInfoTable.required.getName() ) ) {
                    builder.setRequired( ( Boolean )data );
                } else if( colNameCaseIgnore.equals( ConnInfoTable.basic.getName() ) ) {
                    isBasic = ( Boolean )data;
                }
            }
            if( isBasic ) {
                basic.add( builder.build() );
            } else {
                additional.add( builder.build() );
            }
        }
        return new Tuple<>( basic, additional );
    }

    private List<String> getTags( List<DataLayer.Column> columns,
                                  List<DataLayer.Row> rows ) {
        List<String> tags = new ArrayList<>();
        for( var row : rows ) {
            for( var cell : row.getCellList() ) {
                var cellHead = columns.get( cell.getColumnIndex() );
                var data = convertDataOfDataLayer( cellHead, cell );
                var colNameCaseIgnore = cellHead.getColumnName().toLowerCase();
                if( colNameCaseIgnore.equals( DataStorageTagTable.tag.getName() ) ) {
                    tags.add( ( String )data );
                }
            }
        }
        return tags;
    }

    public List<StorageOuterClass.Storage> search(
            StorageOuterClass.StorageSearchFilter filter,
            List<Utilities.Sort> sorts
    ) {
        MakerInterface sqlBuilder = select(
                DataStorageTable.id,
                DataStorageTable.name,
                DataStorageTable.status,
                DataStorageAdaptorTable.storageTypeName
        )
                .from( DataStorageTable.table )
                .join( DataStorageAdaptorTable.table,
                        JoinMethod.LEFT,
                        Equal.of( DataStorageTable.adaptorId, DataStorageAdaptorTable.id ) );

        List<Condition> conditions = new ArrayList<>();
        if( !filter.getName().isBlank() ) {
            conditions.add( Like.of( DataStorageTable.name, "%" + filter.getName() + "%" ) );
        }
        if( filter.getStorageTypeCount() > 0 ) {
            conditions.add( In.of( DataStorageAdaptorTable.storageTypeName,
                    filter.getStorageTypeList()
            ) );
        }
        if( filter.getStatusCount() > 0 ) {
            conditions.add( In.of( DataStorageTable.status,
                    filter.getStatusList().stream()
                            .map( x -> x.getValueDescriptor().getName() )
                            .collect( Collectors.toList() )
            ) );
        }

        if( !conditions.isEmpty() ) {
            sqlBuilder = ( ( WhereUsable )sqlBuilder )
                    .where( conditions.toArray( new Condition[]{} ) );
        }
        if( !sorts.isEmpty() ) {
            sqlBuilder = ( ( OrderUsable )sqlBuilder )
                    .orderBy(
                            sorts.stream()
                                    .map( x -> new Order( x.getOrder(), x.getField(), x.getDirection().name() ) )
                                    .toArray( Order[]::new )
                    );
        }

        var dbResult = dataLayerConnection.execute( sqlBuilder.generate().getStatement() );
        var tableData = dbResult.getData().getTable();
        List<StorageOuterClass.Storage> result = new ArrayList<>();
        for( var row : tableData.getRowsList() ) {
            var storageBuilder = getStorageBuilder( tableData.getColumnsList(), row );
            storageBuilder.setStorageType( row.getCell( 3 ).getStringValue() );
            result.add( storageBuilder.build() );
        }
        return result;
    }

    private StorageOuterClass.Storage.Builder getStorageBuilderById( String id ) {
        var dataStorageSql = select().from( DataStorageTable.table )
                .where( Equal.of( DataStorageTable.id, id ) )
                .generate().getStatement();
        var metadataSql = select().from( DataStorageMetadataTable.table )
                .where( Equal.of( DataStorageMetadataTable.datastorageId, id ) )
                .generate().getStatement();
        var connInfoSql = select().from( ConnInfoTable.table )
                .where( Equal.of( ConnInfoTable.datastorageId, id ) )
                .generate().getStatement();
        var tagSql = select().from( DataStorageTagTable.table )
                .where( Equal.of( DataStorageTagTable.datastorageId, id ) )
                .generate().getStatement();

        var result = dataLayerConnection.execute( dataStorageSql ).getData().getTable();

        if( result.getRowsCount() == 0 ) {
            log.warn( "No data of id = " + id );
            return StorageOuterClass.Storage.newBuilder();
        }
        var storageBuilder = getStorageBuilder(
                result.getColumnsList(),
                result.getRows( 0 )
        );

        result = dataLayerConnection.execute( metadataSql ).getData().getTable();
        var metaTuple = getMetas( result.getColumnsList(), result.getRowsList() );
        storageBuilder.addAllSystemMeta( metaTuple.getLeft() );
        storageBuilder.addAllUserMeta( metaTuple.getRight() );

        result = dataLayerConnection.execute( connInfoSql ).getData().getTable();
        var connInfoTuple = getConnectionOptions( result.getColumnsList(), result.getRowsList() );
        storageBuilder.addAllBasicOptions( connInfoTuple.getLeft() );
        storageBuilder.addAllAdditionalOptions( connInfoTuple.getRight() );

        result = dataLayerConnection.execute( tagSql ).getData().getTable();
        storageBuilder.addAllTags( getTags( result.getColumnsList(), result.getRowsList() ) );

        return storageBuilder;
    }

    public StorageOuterClass.Storage status( String id ) {
        var storageBuilder = getStorageBuilderById( id );
        // TODO: statistic, history, event 정보 추가 필요
        return storageBuilder.build();
    }

    public StorageOuterClass.Storage default_( String id ) {
        var storageBuilder = getStorageBuilderById( id );
        return storageBuilder.build();
    }

    public StorageOuterClass.Storage advanced( String id ) {
        var storageBuilder = getStorageBuilderById( id );
        var storageAutoAddSettingSql = select(
                StorageAutoAddSettingTable.regex,
                StorageAutoAddSettingTable.dataType,
                StorageAutoAddSettingTable.dataFormat,
                StorageAutoAddSettingTable.minSize,
                StorageAutoAddSettingTable.maxSize,
                StorageAutoAddSettingTable.startDate,
                StorageAutoAddSettingTable.endDate
        ).from( StorageAutoAddSettingTable.table )
                .where( Equal.of( StorageAutoAddSettingTable.datastorageId, id ) )
                .generate().getStatement();
        var syncMonitoringSql = select(
                DataStorageTable.syncEnable,
                DataStorageTable.syncType,
                DataStorageTable.syncWeek,
                DataStorageTable.syncRunTime,

                DataStorageTable.monitoringEnable,
                DataStorageTable.monitoringProtocol,
                DataStorageTable.monitoringHost,
                DataStorageTable.monitoringPort,
                DataStorageTable.monitoringSql,
                DataStorageTable.monitoringPeriod,
                DataStorageTable.monitoringTimeout,
                DataStorageTable.monitoringSuccessThreshold,
                DataStorageTable.monitoringFailThreshold,

                DataStorageTable.autoAddSettingEnable
        ).from( DataStorageTable.table )
                .where( Equal.of( DataStorageTable.id, id ) )
                .generate().getStatement();

        var autoAddSettingResult = dataLayerConnection.execute( storageAutoAddSettingSql ).getData().getTable();
        var syncMonitoringResult = dataLayerConnection.execute( syncMonitoringSql ).getData().getTable();

        var builder = StorageOuterClass.StorageSetting.newBuilder();
        builder.setSyncSetting( StorageOuterClass.SyncSetting.newBuilder()
                .setEnable( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 0 ).getBoolValue() )
                .setSyncType( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 1 ).getInt32Value() )
                .setWeek( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 2 ).getInt32Value() )
                .setRunTime( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 3 ).getStringValue() )
                .build() );
        var monitoringBuilder = StorageOuterClass.MonitoringSetting.newBuilder()
                .setEnable( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 4 ).getBoolValue() )
                .setHost( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 6 ).getStringValue() )
                .setPort( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 7 ).getStringValue() )
                .setSql( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 8 ).getStringValue() )
                .setPeriod( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 9 ).getInt32Value() )
                .setTimeout( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 10 ).getInt32Value() )
                .setSuccessThreshold( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 11 ).getInt32Value() )
                .setFailThreshold( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 12 ).getInt32Value() );
        if( !syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 5 ).getStringValue().isEmpty() ) {
            monitoringBuilder.setProtocol( StorageOuterClass.MonitoringProtocol.valueOf( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 5 ).getStringValue() ) );
        }
        builder.setMonitoringSetting( monitoringBuilder.build() );
        builder.setAutoAddSetting( StorageOuterClass.AutoAddSetting.newBuilder()
                .setEnable( syncMonitoringResult.getRowsOrBuilder( 0 ).getCellOrBuilder( 13 ).getBoolValue() )
                .addAllOptions(
                        autoAddSettingResult.getRowsList().stream().map( x -> StorageOuterClass.AutoAddSetting.AutoAddSettingOption.newBuilder()
                                .setRegex( x.getCellOrBuilder( 0 ).getStringValue() )
                                .setDataType( x.getCellOrBuilder( 1 ).getStringValue() )
                                .setDataFormat( x.getCellOrBuilder( 2 ).getStringValue() )
                                .setMinSize( x.getCellOrBuilder( 3 ).getInt32Value() )
                                .setMaxSize( x.getCellOrBuilder( 4 ).getInt32Value() )
                                .setStartDate( x.getCellOrBuilder( 5 ).getStringValue() )
                                .setEndDate( x.getCellOrBuilder( 6 ).getStringValue() )
                                .build() ).collect( Collectors.toList() )
                )
        );
        storageBuilder.setSettings( builder.build() );

        // TODO: setting 정보 추가 필요
        return storageBuilder.build();
    }

    public StorageOuterClass.StorageBrowse browse( String dataStorageId, String path, Integer depth, String name ) {
        var sql = select(
                DataStorageTable.url,
                ConnInfoTable.key,
                ConnInfoTable.value,
                ConnInfoTable.basic,
                DataStorageAdaptorTable.storageTypeName,
                DataStorageAdaptorTable.driver
        )
                .from( ConnInfoTable.table )
                .join( DataStorageTable.table,
                        JoinMethod.RIGHT,
                        Equal.of( ConnInfoTable.datastorageId, DataStorageTable.id ) )
                .join( DataStorageAdaptorTable.table,
                        JoinMethod.RIGHT,
                        Equal.of( DataStorageAdaptorTable.id, DataStorageTable.adaptorId ) )
                .where( Equal.of( ConnInfoTable.datastorageId, dataStorageId ) )
                .generate()
                .getStatement();
        var infos = dataLayerConnection.execute( sql );
        var table = infos.getData().getTable();
        var builder = StorageOuterClass.StorageBrowse.newBuilder()
                .setId( dataStorageId )
                .setPath( path );
        if( table.getRowsCount() < 1 ) {
            return builder.build();
        }
        var url = table.getRows( 0 ).getCell( 0 ).getStringValue();
        Map<String, Object> basicOptions = new HashMap<>();
        Properties advOptions = new Properties();
        for( var row : table.getRowsList() ) {
            var key = row.getCell( 1 ).getStringValue().toLowerCase();
            var valueCell = row.getCell( 2 );
            var value = convertDataOfDataLayer( table.getColumns( valueCell.getColumnIndex() ), valueCell );

            if( row.getCell( 3 ).getBoolValue() ) {
                basicOptions.put( key, value );
            } else {
                advOptions.put( key, value );
            }
        }
        var collector = DataCollectorFactory.getCollector(
                table.getRows( 0 ).getCell( 4 ).getStringValue() );
        assert collector != null; // 삭제 할것 default 만들 던지 에러 처리 하던지
        collector.setDepth( depth );
        collector.setPath( path );
        var driver = table.getRows( 0 ).getCell( 5 ).getStringValue();
        return builder
                .addAllData( collector.collect( url, basicOptions, advOptions, driver ) )
                .build();
    }

    private String getInsertOrUpdateSql( Boolean isInsert, SqlTable table, List<SqlColumn> columns, List<Object> values, Condition condition ) {
        if( isInsert ) {
            return insert( table )
                    .columns( columns.toArray( SqlColumn[]::new ) )
                    .values( values.toArray() )
                    .generate().getStatement();
        } else {
            return update( table )
                    .columns( columns.toArray( SqlColumn[]::new ) )
                    .values( values.toArray() )
                    .where( condition )
                    .generate().getStatement();
        }
    }

    private List<String> getInsertOrUpdateQueries( StorageOuterClass.Storage inputData, boolean isInsert ) {
        List<String> sqlList = new ArrayList<>();
        String id;

        List<SqlColumn> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Condition condition = Equal.of( 0, 1 );
        if( isInsert ) {
            id = UUID.randomUUID().toString();
            columns.add( DataStorageTable.id );
            values.add( id );
        } else {
            id = inputData.getId();
            condition = Equal.of( DataStorageTable.id, id );
        }
        columns.addAll( List.of(
                DataStorageTable.adaptorId,
                DataStorageTable.name,
                DataStorageTable.url,
                DataStorageTable.userDesc,

                DataStorageTable.syncEnable,
                DataStorageTable.syncType,
                DataStorageTable.syncWeek,
                DataStorageTable.syncRunTime,

                DataStorageTable.monitoringEnable,
                DataStorageTable.monitoringProtocol,
                DataStorageTable.monitoringHost,
                DataStorageTable.monitoringPort,
                DataStorageTable.monitoringSql,
                DataStorageTable.monitoringPeriod,
                DataStorageTable.monitoringTimeout,
                DataStorageTable.monitoringSuccessThreshold,
                DataStorageTable.monitoringFailThreshold,

                DataStorageTable.autoAddSettingEnable ) );
        values.addAll( List.of(
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

                inputData.getSettings().getAutoAddSetting().getEnable() ) );

        sqlList.add( getInsertOrUpdateSql( isInsert, DataStorageTable.table, columns, values, condition ) );

        if( inputData.getSettings().getAutoAddSetting().getOptionsCount() > 0 ) {
            sqlList.add( delete( StorageAutoAddSettingTable.table )
                    .where( Equal.of( StorageAutoAddSettingTable.datastorageId, id ) )
                    .generate().getStatement()
            );
            var authAddSettingSqlBuilder = insert( StorageAutoAddSettingTable.table )
                    .columns(
                            StorageAutoAddSettingTable.datastorageId,
                            StorageAutoAddSettingTable.regex,
                            StorageAutoAddSettingTable.dataType,
                            StorageAutoAddSettingTable.dataFormat,
                            StorageAutoAddSettingTable.minSize,
                            StorageAutoAddSettingTable.maxSize,
                            StorageAutoAddSettingTable.startDate,
                            StorageAutoAddSettingTable.endDate
                    );
            for( var s : inputData.getSettings().getAutoAddSetting().getOptionsList() ) {
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
            sqlList.add( authAddSettingSqlBuilder.generate().getStatement() );
        }

        if( inputData.getBasicOptionsCount() > 0 || inputData.getAdditionalOptionsCount() > 0 ) {
            sqlList.add( delete( ConnInfoTable.table )
                    .where( Equal.of( ConnInfoTable.datastorageId, id ) )
                    .generate().getStatement()
            );
            var connInfoSqlBuilder = insert( ConnInfoTable.table )
                    .columns(
                            ConnInfoTable.datastorageId,
                            ConnInfoTable.key,
                            ConnInfoTable.type,
                            ConnInfoTable.value,
                            ConnInfoTable.required
                    );

            for( var c : inputData.getBasicOptionsList() ) {
                connInfoSqlBuilder = connInfoSqlBuilder
                        .values(
                                id,
                                c.getKey(),
                                c.getValueType().getValueDescriptor().getName(),
                                c.getValue(),
                                true
                        );
            }
            for( var c : inputData.getAdditionalOptionsList() ) {
                connInfoSqlBuilder = connInfoSqlBuilder
                        .values(
                                id,
                                c.getKey(),
                                c.getValueType().getValueDescriptor().getName(),
                                c.getValue(),
                                false
                        );
            }
            sqlList.add( connInfoSqlBuilder.generate().getStatement() );
        }

        if( inputData.getTagsCount() > 0 ) {
            sqlList.add( delete( DataStorageTagTable.table )
                    .where( Equal.of( DataStorageTagTable.datastorageId, id ) )
                    .generate().getStatement()
            );
            var tagSqlBuilder = insert( DataStorageTagTable.table )
                    .columns( DataStorageTagTable.datastorageId,
                            DataStorageTagTable.tag );
            for( var t : inputData.getTagsList() ) {
                tagSqlBuilder = tagSqlBuilder.values( id, t );
            }
            sqlList.add( tagSqlBuilder.generate().getStatement() );
        }

        if( inputData.getUserMetaCount() > 0 ) {
            sqlList.add( delete( DataStorageMetadataTable.table )
                    .where( Equal.of( DataStorageMetadataTable.datastorageId, id ) )
                    .generate().getStatement()
            );
            var metadataSqlBuilder = insert( DataStorageMetadataTable.table )
                    .columns( DataStorageMetadataTable.datastorageId,
                            DataStorageMetadataTable.key,
                            DataStorageMetadataTable.value,
                            DataStorageMetadataTable.isSystem
                    );
            for( var m : inputData.getUserMetaList() ) {
                metadataSqlBuilder = metadataSqlBuilder.values(
                        id,
                        m.getKey(),
                        m.getValue(),
                        false
                );
            }
            sqlList.add( metadataSqlBuilder.generate().getStatement() );
        }

        return sqlList;
    }

    public void addStorage( StorageOuterClass.Storage inputData ) {
        var sqlList = getInsertOrUpdateQueries( inputData, true );
        var result = dataLayerConnection.executeBatch( sqlList );
        log.info( "insert result: " + result.getDataList() );
        // TODO: Queue 로 메세지 를 송신, event_type, id
    }

    public void updateStorage( StorageOuterClass.Storage inputData ) {
        var result = dataLayerConnection.execute( select( DataStorageTable.id )
                .from( DataStorageTable.table )
                .where( Equal.of( DataStorageTable.id, inputData.getId() ) )
                .generate().getStatement() );
        if( result.getData().getTable().getRowsCount() != 1 ) {
            throw new RuntimeException( "no id " + inputData.getId() );
        }
        var sqlList = getInsertOrUpdateQueries( inputData, false );
        var batchResult = dataLayerConnection.executeBatch( sqlList );
        log.info( "update result: " + batchResult.getDataList() );
    }

    public void deleteStorage( String id ) {
        // data storage id 를 참조하는 테이블의 값은 on delete cascade 이므로 함께 삭제됨
        var sql = delete( DataStorageTable.table )
                .where( Equal.of( DataStorageTable.id, id ) )
                .generate()
                .getStatement();
        var result = dataLayerConnection.execute( sql );
        log.info( "delete result: " + result.getData().getResponse() );
    }

    public Tuple<Boolean, String> connectTest(
            String id,
            List<StorageCommon.InputField> basicOptions,
            List<StorageCommon.InputField> advancedOptions,
            String urlFormat
    ) {
        var sql = select( DataStorageAdaptorTable.driver )
                .from( DataStorageAdaptorTable.table )
                .where( Equal.of( DataStorageAdaptorTable.id, id ) )
                .generate()
                .getStatement();
        var resultTable = dataLayerConnection.execute( sql ).getData().getTable();
        if( resultTable.getRowsCount() != 1 || resultTable.getRows( 0 ).getCellCount() != 1 ) {
            return new Tuple<>( false, "해당 id 의 driver 값을 찾지 못했다." );
        }
        var driver = resultTable.getRows( 0 ).getCell( 0 ).getStringValue();

        Map<String, Object> basic = new HashMap<>();

        for( var op : basicOptions ) {
            basic.put( op.getKey().toLowerCase(), convertInputField( op ) );
        }

        Properties addition = new Properties();

        for( var op : advancedOptions ) {
            addition.put( op.getKey().toLowerCase(), convertInputField( op ) );
        }

        try( var connector = new JdbcConnector.Builder()
                .withUrlFormat( urlFormat )
                .withUrlOptions( basic )
                .withAdvancedOptions( addition )
                .withDriver( driver )
                .build()
        ) {
            var conn = connector.connect();
            var cur = conn.cursor();
            cur.execute( "select 1" );
            var result = cur.getResultSet();
            System.out.println( result );
            result.next();
            var value = result.getString( 1 );
            if( value.equals( "1" ) ) {
                return new Tuple<>( true, "연결 성공" );
            } else {
                return new Tuple<>( false, "연결 실패" );
            }
        } catch( ClassNotFoundException | SQLException e ) {
            log.error( e.getMessage(), e );
            return new Tuple<>( false, e.getMessage() );
        }
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
