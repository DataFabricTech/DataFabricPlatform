package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
//import com.mobigen.datafabric.core.services.storage.DataModelService;
//import com.mobigen.datafabric.core.services.storage.DataStorageService;
//import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.core.worker.Job;
import com.mobigen.datafabric.core.worker.timer.Timer;
//import com.mobigen.datafabric.share.protobuf.*;
//import com.mobigen.libs.configuration.Config;
//import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertInputField;
//import static com.mobigen.sqlgen.maker.SelectMaker.select;

@Slf4j
public class AddStorageTask implements Runnable {
//    private final Job job;
//    private final DataStorageService dataStorageService;
//    private final DataModelService dataModelService;
//    private final DataLayerConnection dc;

    public AddStorageTask( Job job ) {
//        Config config = new Config();
//        this.job = job;
//        this.dc = new DataLayerConnection( config.getConfig().getBoolean( "data-layer.test", false ) );
//        this.dataStorageService = new DataStorageService(this.dc);
//        this.dataModelService = new DataModelService(this.dc);
    }

    @Override
    public void run() {
//        log.error( "[ New Storage ] : Start. Storage Connection ID[ {} ]", job.getStorageId() );
//
//        // TODO : Get Storage From Data Layer
//        StorageOuterClass.Storage storage = dataStorageService.advanced( job.getStorageId() );
////        postgres =    1b6c8550-a7f8-4c96-9d17-cd10770ace87
////        mysql =       2b6c8550-a7f8-4c96-9d17-cd10770ace87
//
//        // 데이터 변경을 위해 빌더로 변경
//        StorageOuterClass.Storage.Builder storagebuilder = StorageOuterClass.Storage.newBuilder().mergeFrom( storage );
//
//        try( var connector = getConnector( storagebuilder) ) {
//            log.info( "[ New Storage ] : Update System Metadata" );
//            var meta = getStorageMeta( storagebuilder, connector.cursor() );
//            storagebuilder.addAllSystemMeta( meta );
//
//            log.info( "[ New Storage ] : Get Data( And Build Data Model)" );
//            if( storagebuilder.getSettings().hasAutoAddSetting() && storagebuilder.getSettings().getAutoAddSetting().getEnable() ) {
//                // 자동 추가 설정 기준으로 데이터 필터링
//                List<DataModelOuterClass.DataModel.Builder> dataModels = getDataList( storagebuilder, connector.cursor() );
//                if( !dataModels.isEmpty() ) {
//                    // 데이터 정보 채우기
//                    setMetadata( storagebuilder, connector.cursor(), dataModels );
//                    log.info( "[ New Storage ] : Save Data Model" );
//                    dataModelService.NewDataModels(dataModels);
//                }
//            }
//
//            if( storagebuilder.getSettings().hasSyncSetting() && storagebuilder.getSettings().getSyncSetting().getEnable() ) {
//                log.error( "[ New Storage ] : Set Sync Timer" );
//                var syncSetting = storagebuilder.getSettings().getSyncSetting();
//                if( syncSetting.getSyncType() == 0 ) {
//                    log.info( "[ New Storage ] : Storage[ {} ] Sync Enable. Type[ Period ] [ {} ]",
//                            storagebuilder.getName(), syncSetting.getPeriod() );
//                    Timer timer = Timer.getInstance();
//                    if( timer != null ) {
//                        var timerResult = timer.Add( null, ( long )( syncSetting.getPeriod() * 1000L ), true,
//                                new StorageSyncTask( Job.builder().type( Job.JobType.STORAGE_SYNC ).storageId( storage.getId() ).build() ) );
//                        if( timerResult < 0 ) {
//                            log.error( "[ New Storage ] Storage[ {} / {} ]. Timer Insert Error[ {} ]",
//                                    storage.getId(), storage.getName(), timerResult );
//                        }
//                    }
//                } else {
//                    boolean[] week = new boolean[ 7 ];
//                    for( int i = 0; i < 7; i++ ) {
//                        int day = 0x01 << i;
//                        week[ i ] = ( day & syncSetting.getWeek() ) > 0;
//                    }
//                    log.info( "[ New Storage ] : Storage[ {} ] Sync Enable. Type[ Week ] [ {} / {} ] RunTime[ {} ]",
//                            storagebuilder.getName(), String.format( "0x%02X", syncSetting.getWeek() ),
//                            Arrays.toString( week ), syncSetting.getRunTime() );
//                }
//            }
//            // TODO : Update Storage
////            ds.updateStorage(storagebuilder.build());
////             TODO : 필요한 경우 알림 전송 개발 추가 - 아마도 필요
//        } catch( Exception e ) {
//            log.error( e.getMessage(), e );
//        }
    }

//    public JdbcConnector getConnector( StorageOuterClass.Storage.Builder storage ) throws SQLException, ClassNotFoundException {
//        var sql = select( DataStorageAdaptorTable.storageTypeName, DataStorageAdaptorTable.driver )
//                .from( DataStorageAdaptorTable.table )
//                .where( Equal.of( DataStorageAdaptorTable.id, storage.getAdaptorId() ) )
//                .generate()
//                .getStatement();
//        var resultTable = dc.execute( sql ).getData().getTable();
//        if( resultTable.getRowsCount() != 1 || resultTable.getRows( 0 ).getCellCount() != 2 ) {
//            log.error( "[ New Storage ] Storage[ {} / {} ] Not found Adaptor ID[ {} ]",
//                    storage.getId(), storage.getName(), storage.getAdaptorId() );
//            return null;
//        }
//        var storageTypeName = resultTable.getRows( 0 ).getCell( 0 ).getStringValue();
//        var driver = resultTable.getRows( 0 ).getCell( 1 ).getStringValue();
//        storage.setStorageType( storageTypeName );
//
//        Map<String, Object> basic = new HashMap<>();
//        for (var op : storage.getBasicOptionsList()) {
//            basic.put(op.getKey().toLowerCase(), convertInputField(op));
//        }
//        Properties addition = new Properties();
//        for (var op : storage.getAdditionalOptionsList()) {
//            addition.put(op.getKey().toLowerCase(), convertInputField(op));
//        }
//
//        var connector = new JdbcConnector.Builder()
//                .withUrlFormat( storage.getUrl() )
//                .withUrlOptions( basic )
//                .withAdvancedOptions( addition )
//                .withDriver( driver )
//                .build();
//        var conn = connector.connect();
//        var cur = conn.cursor();
//        cur.execute( "select 1" );
//        var result = cur.getResultSet();
//        System.out.println( result );
//        result.next();
//        var value = result.getString( 1 );
//        if( value.equals( "1" ) ) {
//            return conn;
//        } else {
//            log.error( "[ New Storage ] [ {} / {} ] Connect Fail", storage.getId(), storage.getName() );
//            return null;
//        }
//    }
//
//    public ArrayList<Utilities.Meta> getStorageMeta( StorageOuterClass.Storage.Builder storage, JdbcConnector.Cursor cur ) throws Exception {
//        ArrayList<Utilities.Meta> metas = new ArrayList<>();
//
//        final String[] param = new String[ 2 ];
//        storage.getBasicOptionsList().forEach( x -> {
//            // 연결 정보의 데이터 베이스 값 획득
//            if( x.getKey().equalsIgnoreCase( "DATABASE" ) ) {
//                param[ 0 ] = x.getValue();
//            }
//            // 연결 정보의 아이디 값 획득
//            if( x.getKey().equalsIgnoreCase( "USER" ) ) {
//                param[ 1 ] = x.getValue();
//            }
//        } );
//        storage.getAdditionalOptionsList().forEach( x -> {
//            // 연결 정보의 데이터 베이스 값 획득
//            if( x.getKey().equalsIgnoreCase( "DATABASE" ) ) {
//                param[ 0 ] = x.getValue();
//            }
//            // 연결 정보의 아이디 값 획득
//            if( x.getKey().equalsIgnoreCase( "USER" ) ) {
//                param[ 1 ] = x.getValue();
//            }
//        } );
//
//        // Set Storage Name
//        if( param[0] != null && !param[0].isEmpty())
//            metas.add( Utilities.Meta.newBuilder().setKey( "DATABASE_NAME" ).setValue( param[ 0 ] ).build() );
//
//        // Set Storage Owner : Get Database Owner
//        if( storage.getStorageType().equalsIgnoreCase( "postgresql" ) ) {
//            var sql = new SQL_Generator().getDatabaseOwner( storage.getStorageType(), param[ 0 ] );
//            cur.execute( sql );
//            var rs = cur.getResultSet();
//            if( rs.next() ) {
//                metas.add( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( rs.getString( 1 ) ).build() );
//            }
//        } else {
//            if( param[ 1 ] != null && !param[ 1 ].isEmpty() ) {
//                metas.add( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( param[ 1 ] ).build() );
//            }
//        }
//
//        return metas;
//    }
//
//    private List<DataModelOuterClass.DataModel.Builder> getDataList( StorageOuterClass.Storage.Builder storage, JdbcConnector.Cursor cur ) throws Exception {
//        List<DataModelOuterClass.DataModel.Builder> dataModels = new ArrayList<>();
//        for( StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt : storage.getSettings().getAutoAddSetting().getOptionsList() ) {
//            List<DataModelOuterClass.DataModel.Builder> filterResult;
//            // TODO : Data Type Filter 처리 필요
//            // Regex, Data Format 필터링 + 메타 데이터(
//            filterResult = FilterNameAndFormat( storage, opt, cur );
//            // Size 필터링
//            filterResult = FilterSize( storage, opt, cur, filterResult );
//            // TODO : 시간 필터링 처리 필요
//            // 반환 데이터 리스트에 추가
//            dataModels.addAll( filterResult );
//        }
//        // 중복 제거
//        List<DataModelOuterClass.DataModel.Builder> result = new ArrayList<>();
//        dataModels.forEach( model -> {
//            if( !result.contains( model ) ) result.add( model );
//        } );
//        return result;
//    }
//
//    private List<DataModelOuterClass.DataModel.Builder> FilterNameAndFormat(
//            StorageOuterClass.Storage.Builder storage,
//            StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt,
//            JdbcConnector.Cursor cur ) throws Exception {
//
//        List<DataModelOuterClass.DataModel.Builder> dataModels = new ArrayList<>();
//        cur.execute( new SQL_Generator().getTablesInfo( storage.getStorageType() ) );
//        var rs = cur.getResultSet();
//        while( rs.next() ) {
//            if( !opt.getRegex().equalsIgnoreCase( "*" ) ) {
//                Pattern pattern = Pattern.compile( opt.getRegex() );
//                Matcher matcher = pattern.matcher( rs.getString( 1 ) );
//                if( !matcher.find() ) {
//                    log.debug( "Regex Not Match Pattern[ {} ] Target[ {} ]", opt.getRegex(), rs.getString( 1 ) );
//                    System.out.println( "Match not found" );
//                    continue;
//                }
//            }
//
//            DataModelOuterClass.DataModel.Builder dataModelBuilder = DataModelOuterClass.DataModel.newBuilder();
//            if( !opt.getDataFormat().isEmpty() ) {
//                if( opt.getDataFormat().equalsIgnoreCase( "table" ) ) {
//                    if( !rs.getString( 2 ).equalsIgnoreCase( "base table" ) ) continue;
//                    dataModelBuilder.setDataType( "STRUCTURED" );
//                    dataModelBuilder.setDataFormat( "TABLE" );
//                } else if( opt.getDataFormat().equalsIgnoreCase( "view" ) ) {
//                    if( rs.getString( 2 ).equalsIgnoreCase( "view" ) ) continue;
//                    dataModelBuilder.setDataType( "STRUCTURED" );
//                    dataModelBuilder.setDataFormat( "VIEW" );
//                }
//            }
//            // Set Name(Table Name)
//            dataModelBuilder.setName( rs.getString( 1 ) );
//            // Set Status
//            dataModelBuilder.setStatus( "CONNECTED" );
//            log.debug( "New Data Name[ {} ]", rs.getString( 1 ) );
//            // Set System Metadata ( owner )
//            if( rs.getMetaData().getColumnCount() == 3 ) {
//                dataModelBuilder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "NAME" ).setValue( rs.getString( 1 ) ).build() );
//                dataModelBuilder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( rs.getString( 3 ) ).build() );
//                log.debug( "Data Name[ {} ] Metadata[ owner ][ {} ]", rs.getString( 1 ), rs.getString( 3 ) );
//            }
//
//            // Set Data Location
//            for( int i = 0; i < storage.getSystemMetaCount(); i++ ) {
//                if( storage.getSystemMeta( i ).getKey().equalsIgnoreCase( "database_name" ) ) {
//                    var databaseName = storage.getSystemMeta( i ).getValue();
//                    dataModelBuilder.addDataLocation(
//                            DataModelOuterClass.DataLocation.newBuilder()
//                                    .setStorageId( storage.getId() )
//                                    .setDatabaseName( databaseName )
//                                    .setTableName( rs.getString( 1 ) )
//                                    .build() );
//                    break;
//                }
//            }
//
//            // Set Dat Refine
//            dataModelBuilder.setDataRefine( DataModelOuterClass.DataRefine.newBuilder().setQuery( "select * from " + rs.getString( 1 ) ).build() );
//
//            dataModels.add( dataModelBuilder );
//        }
//        return dataModels;
//    }
//
//    private List<DataModelOuterClass.DataModel.Builder> FilterSize(
//            StorageOuterClass.Storage.Builder storage,
//            StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt,
//            JdbcConnector.Cursor cur,
//            List<DataModelOuterClass.DataModel.Builder> builders ) throws Exception {
//
//        List<DataModelOuterClass.DataModel.Builder> dataModels = new ArrayList<>();
//        for( DataModelOuterClass.DataModel.Builder builder : builders ) {
//            cur.execute( new SQL_Generator().getTableSize( storage.getStorageType(), builder.getName() ) );
//            var rs = cur.getResultSet();
//            while( rs.next() ) {
//                var rows = Long.parseLong( rs.getString( 1 ) );
//                builder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "ROWS" ).setValue( String.valueOf( rows ) ).build() );
//                if( opt.getMinSize() > 0 && rows < opt.getMinSize() ) {
//                    log.info( "Data[ {} ] Size(Row)[ {} ] Filtering. Min[ {} ] / Max[ {} ]",
//                            builder.getName(), rows, opt.getMinSize(), opt.getMaxSize() );
//                    continue;
//                }
//                if( opt.getMaxSize() > 0 && opt.getMaxSize() < rows ) {
//                    log.info( "Data[ {} ] Size(Row)[ {} ] Filtering. Min[ {} ] / Max[ {} ]",
//                            builder.getName(), rows, opt.getMinSize(), opt.getMaxSize() );
//                    continue;
//                }
//                log.info( "Data[ {} ] Size(Row)[ {} ]", builder.getName(), rows );
//                dataModels.add( builder );
//            }
//        }
//        return dataModels;
//    }
//
//
//    private void setMetadata(
//            StorageOuterClass.Storage.Builder storage,
//            JdbcConnector.Cursor cursor,
//            List<DataModelOuterClass.DataModel.Builder> dataModels ) throws Exception {
//
//        for( DataModelOuterClass.DataModel.Builder dataModel : dataModels ) {
//            // 테이블 코멘트
//            var comment = getTableComment( storage, cursor, dataModel.getName() );
//            if( comment != null && !comment.isEmpty() ) {
//                dataModel.setDescription( comment );
//                dataModel.addSystemMeta( Utilities.Meta.newBuilder().setKey( "DESC" ).setValue( comment ).build() );
//                log.info( "Data[ {} ] Comment[ {} ]", dataModel.getName(), comment );
//            } else {
//                log.info( "Data[ {} ] No Have Comment", dataModel.getName() );
//            }
//            // 데이터 구조
//            dataModel.addAllDataStructure( getDataStructure( storage, cursor, dataModel.getName() ) );
//
//            // 메타 데이터 설정
//            dataModel.addSystemMeta( Utilities.Meta.newBuilder().setKey( "FORMAT" ).setValue( "TABLE" ).build() );
//            dataModel.addSystemMeta(
//                    Utilities.Meta.newBuilder().setKey( "COLUMNS" )
//                            .setValue( String.valueOf( dataModel.getDataStructureList().size() ) ).build() );
//
//            // 권한  :
//            dataModel.setPermission( DataModelOuterClass.Permission.newBuilder().setRead( true ).setWrite( true ).build() );
//            // 기타 데이터 설정
//            dataModel.setDownloadInfo( DataModelOuterClass.DownloadInfo.newBuilder().setStatus( DataModelOuterClass.DownloadInfo.DownloadStatus.READY ).build() );
//            // TODO : 사용자 정보
//            dataModel.setCreator(
//                    UserOuterClass.User.newBuilder()
//                            .setId( "336c8550-a7f8-4c96-9d17-cd10770ace87" )
//                            .setName( "test-user-name" )
//                            .setNickname( "test-user-nickname" )
//                            .setPhone( "+8210-1234-1234" )
//                            .setEmail( "test-user@mobigen.com" )
//                            .build() );
//        }
//    }
//
//    private String getTableComment( StorageOuterClass.Storage.Builder storage, JdbcConnector.Cursor cur, String tableName ) throws Exception {
//        cur.execute( new SQL_Generator().getTableComment( storage.getStorageType(), tableName ) );
//        var rs = cur.getResultSet();
//        while( rs.next() ) {
//            if( rs.getString( 1 ) != null ) return rs.getString( 1 );
//        }
//        return null;
//    }
//
//    private List<DataModelOuterClass.DataStructure> getDataStructure(
//            StorageOuterClass.Storage.Builder storage,
//            JdbcConnector.Cursor cur, String tableName ) throws Exception {
//        List<DataModelOuterClass.DataStructure> dataStructures = new ArrayList<>();
//
//        cur.execute( new SQL_Generator().getTableStructure( storage.getStorageType(), tableName ) );
//        var rs = cur.getResultSet();
//        while( rs.next() ) {
//            DataModelOuterClass.DataStructure.Builder dataStructureBuilder = DataModelOuterClass.DataStructure.newBuilder();
//
//            dataStructureBuilder.setOrder( Integer.parseInt( rs.getString( 1 ) ) );
//            dataStructureBuilder.setName( rs.getString( 2 ) );
//            dataStructureBuilder.setColType( rs.getString( 3 ) );
//            if( rs.getString( 4 ) != null && !rs.getString( 4 ).isEmpty() ) {
//                dataStructureBuilder.setLength( rs.getInt( 4 ) );
//            } else {
//                if( rs.getString( 5 ) != null && !rs.getString( 5 ).isEmpty() ) {
//                    dataStructureBuilder.setLength( rs.getLong( 5 ) > Integer.MAX_VALUE ? -1 : rs.getInt( 5 ) );
//                }
//            }
//            if( rs.getString( 6 ) != null && !rs.getString( 6 ).isEmpty()) {
//                var def = rs.getString( 6 );
//                def = URLEncoder.encode(def, StandardCharsets.UTF_8 );
//                dataStructureBuilder.setDefaultValue( def );
//            }
//            dataStructureBuilder.setDescription( rs.getString( 7 ) == null ? "" : rs.getString( 7 ) );
//
//            log.error( "Column : " + dataStructureBuilder.toString() );
//            dataStructures.add( dataStructureBuilder.build() );
//        }
//        return dataStructures;
//    }
}
