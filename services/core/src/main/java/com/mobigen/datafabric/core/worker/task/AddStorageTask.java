package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.model.DataStorageAdaptorTable;
import com.mobigen.datafabric.core.services.storage.DataStorageService;
import com.mobigen.datafabric.core.util.DataLayerConnection;
import com.mobigen.datafabric.core.util.JdbcConnector;
import com.mobigen.datafabric.core.worker.Job;
import com.mobigen.datafabric.share.protobuf.*;
import com.mobigen.libs.configuration.Config;
import com.mobigen.sqlgen.where.conditions.Equal;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mobigen.datafabric.core.util.DataLayerUtilFunction.convertInputField;
import static com.mobigen.sqlgen.maker.SelectMaker.select;

@Slf4j
public class AddStorageTask implements Runnable {
    private final Job job;
    private final DataStorageService ds;
    private final DataLayerConnection dc;

    public AddStorageTask( Job job ) {
        Config config = new Config();
        this.job = job;
        this.dc = new DataLayerConnection( config.getConfig().getBoolean( "data-layer.test", false ) );
        this.ds = new DataStorageService();
    }

    @Override
    public void run() {
        log.error( "[ New Storage ] : Start. Storage Connection ID[ {} ]", job.getStorageId() );

        // TODO : Get Storage From Data Layer
        StorageOuterClass.Storage storage = ds.advanced( job.getStorageId() );
//        postgres =    1b6c8550-a7f8-4c96-9d17-cd10770ace87
//        mysql =       2b6c8550-a7f8-4c96-9d17-cd10770ace87

        // 데이터 변경을 위해 빌더로 변경
        StorageOuterClass.Storage.Builder storagebuilder = StorageOuterClass.Storage.newBuilder().mergeFrom( storage );

        try( var connector = getConnector( storage ) ) {
            log.info( "[ New Storage ] : Update System Metadata" );
            var meta = getStorageMeta( storage, connector.cursor() );
            storagebuilder.addAllSystemMeta( meta );

            log.info( "[ New Storage ] : Get Data( And Build Data Model)" );
            if( storage.getSettings().hasAutoAddSetting() && storage.getSettings().getAutoAddSetting().getEnable() ) {
                // 자동 추가 설정 기준으로 데이터 필터링
                List<DataModelOuterClass.DataModel.Builder> dataModels = getDataList( storage, connector.cursor() );
                // 데이터 정보 채우기
                setMetadata( storage, connector.cursor(), dataModels );

                log.info( "[ New Storage ] : Save Data Model" );
                ds.NewDataModels(dataModels);
//                DataLayerConnection dc = new DataLayerConnection();
            }

            if( storage.getSettings().hasSyncSetting() && storage.getSettings().getSyncSetting().getEnable() ) {
                log.error( "[ New Storage ] : Set Sync Timer" );
                var syncSetting = storage.getSettings().getSyncSetting();
                if( syncSetting.getSyncType() == 0 ) {
                    log.info( "[ New Storage ] : Storage[ {} ] Sync Enable. Type[ Period ] [ {} ]",
                            storage.getName(), syncSetting.getPeriod() );
//                    Timer timer = Timer.getInstance();
//                    timer.Add( new TimerData( null, syncSetting.getPeriod() ) );
//                timer.Add( null, ( long )syncSetting.getPeriod(), true, new TimerCallback() {
//                    @Override
//                    public void callback( TimerData data ) {
//
//                    }
//                } );
                } else {
                    boolean[] week = new boolean[ 7 ];
                    for( int i = 0; i < 7; i++ ) {
                        int day = 0x01 << i;
                        week[ i ] = ( day & syncSetting.getWeek() ) > 0;
                    }
                    log.info( "[ New Storage ] : Storage[ {} ] Sync Enable. Type[ Week ] [ {} / {} ] RunTime[ {} ]",
                            storage.getName(), syncSetting.getWeek(), Arrays.toString( week ), syncSetting.getRunTime() );
                }
            }
            // TODO : 실행 결과 저장
//        ds.updateStorage();
            // TODO : 필요한 경우 알림 전송 개발 추가 - 아마도 필요
        } catch( Exception e ) {
            e.printStackTrace();
            log.error( e.getMessage() );
        }
    }

    private StorageOuterClass.Storage getSampleStorage( Job job ) {
        if( job.getStorageId().equals( "postgres" ) ) return postgreStorage();
        else return mariaStorage();
    }

    public StorageOuterClass.Storage postgreStorage() {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        storageBuilder
                .setId( "test" )
                .setName( "data-fabric-storage" )
                .setDescription( "data-fabric-storage" )
                .setStorageType( "PostgreSQL" )
                .setAdaptorId( "PostgreSQL" )
                .setUrl( "jdbc:postgresql://{HOST}:{PORT}/{DATABASE}" );

        var optBuilder = StorageCommon.InputField.newBuilder();

        optBuilder.setKey( "HOST" ).setValue( "192.168.107.28" ).build();
        storageBuilder.addBasicOptions( optBuilder.build() );

        optBuilder.setKey( "PORT" ).setValue( "14632" ).build();
        storageBuilder.addBasicOptions( 1, optBuilder.build() );

        optBuilder.setKey( "DATABASE" ).setValue( "testdb" ).build();
        storageBuilder.addBasicOptions( 2, optBuilder.build() );

        optBuilder.setKey( "USER" ).setValue( "testUser" ).build();
        storageBuilder.addBasicOptions( 3, optBuilder.build() );

        optBuilder.setKey( "PASSWORD" ).setValue( "testUser" ).build();
        storageBuilder.addBasicOptions( 4, optBuilder.build() );

        StorageOuterClass.StorageSetting.Builder settingBuilder = StorageOuterClass.StorageSetting.newBuilder();

        StorageOuterClass.AutoAddSetting.Builder autoAddSettingBuilder = StorageOuterClass.AutoAddSetting.newBuilder();
        autoAddSettingBuilder.setEnable( true );

        StorageOuterClass.AutoAddSetting.AutoAddSettingOption tableOpt = StorageOuterClass.AutoAddSetting.
                AutoAddSettingOption.newBuilder().setRegex( "*" ).setDataFormat( "TABLE" ).setMinSize( 1 ).setMaxSize( 100000 ).build();
        autoAddSettingBuilder.addOptions( tableOpt );

        StorageOuterClass.SyncSetting.Builder syncSettingBuilder = StorageOuterClass.SyncSetting.newBuilder();
        syncSettingBuilder.setEnable( true );
        syncSettingBuilder.setSyncType( 0 ); // period, week
        syncSettingBuilder.setPeriod( 1000 * 60 );

        StorageOuterClass.MonitoringSetting.Builder monitoringBuilder = StorageOuterClass.MonitoringSetting.newBuilder();
        monitoringBuilder.setEnable( true );
        monitoringBuilder.setProtocol( StorageOuterClass.MonitoringProtocol.SQL );
        monitoringBuilder.setHost( "192.168.107.28" );
        monitoringBuilder.setPort( "14632" );
        monitoringBuilder.setPeriod( 30 );
        monitoringBuilder.setTimeout( 5 );
        monitoringBuilder.setSuccessThreshold( 1 );
        monitoringBuilder.setFailThreshold( 2 );

        settingBuilder.setAutoAddSetting( autoAddSettingBuilder.build() );
        settingBuilder.setSyncSetting( syncSettingBuilder.build() );
        settingBuilder.setMonitoringSetting( monitoringBuilder.build() );

        storageBuilder.setSettings( settingBuilder.build() );
        storageBuilder.setCreatedBy( UserOuterClass.User.newBuilder().setId( "user-foo-id-001" ).setName( "foo" ).setNickname( "bar" ).build() );
        storageBuilder.setCreatedAt( Utilities.DateTime.newBuilder().setStrDateTime( "2023-11-05 12:23:34.123" ).build() );
        return storageBuilder.build();
    }

    public StorageOuterClass.Storage mariaStorage() {
        var storageBuilder = StorageOuterClass.Storage.newBuilder();
        storageBuilder
                .setId( "test" )
                .setName( "data-fabric-storage" )
                .setDescription( "data-fabric-storage" )
                .setStorageType( "MariaDB" )
                .setAdaptorId( "MariaDB" )
                .setUrl( "jdbc:mariadb://{HOST}:{PORT}/{DATABASE}" );

        var optBuilder = StorageCommon.InputField.newBuilder();

        optBuilder.setKey( "HOST" ).setValue( "192.168.107.28" ).build();
        storageBuilder.addBasicOptions( optBuilder.build() );

        optBuilder.setKey( "PORT" ).setValue( "39310" ).build();
        storageBuilder.addBasicOptions( 1, optBuilder.build() );

        optBuilder.setKey( "DATABASE" ).setValue( "cache_server" ).build();
        storageBuilder.addBasicOptions( 2, optBuilder.build() );

        optBuilder.setKey( "USER" ).setValue( "cacheserver" ).build();
        storageBuilder.addBasicOptions( 3, optBuilder.build() );

        optBuilder.setKey( "PASSWORD" ).setValue( "cacheserver.platform" ).build();
        storageBuilder.addBasicOptions( 4, optBuilder.build() );

        StorageOuterClass.StorageSetting.Builder settingBuilder = StorageOuterClass.StorageSetting.newBuilder();

        StorageOuterClass.AutoAddSetting.Builder autoAddSettingBuilder = StorageOuterClass.AutoAddSetting.newBuilder();
        autoAddSettingBuilder.setEnable( true );

        StorageOuterClass.AutoAddSetting.AutoAddSettingOption tableOpt = StorageOuterClass.AutoAddSetting.
                AutoAddSettingOption.newBuilder().setRegex( "*" ).setDataFormat( "TABLE" ).build();
        autoAddSettingBuilder.addOptions( tableOpt );

        StorageOuterClass.SyncSetting.Builder syncSettingBuilder = StorageOuterClass.SyncSetting.newBuilder();
        syncSettingBuilder.setEnable( true );
        syncSettingBuilder.setSyncType( 0 ); // period, week
        syncSettingBuilder.setPeriod( 1000 * 60 );

        StorageOuterClass.MonitoringSetting.Builder monitoringBuilder = StorageOuterClass.MonitoringSetting.newBuilder();
        monitoringBuilder.setEnable( true );
        monitoringBuilder.setProtocol( StorageOuterClass.MonitoringProtocol.TCP );
        monitoringBuilder.setHost( "192.168.107.28" );
        monitoringBuilder.setPort( "39310" );
        monitoringBuilder.setPeriod( 60 );
        monitoringBuilder.setTimeout( 5 );
        monitoringBuilder.setSuccessThreshold( 1 );
        monitoringBuilder.setFailThreshold( 2 );

        settingBuilder.setAutoAddSetting( autoAddSettingBuilder.build() );
        settingBuilder.setSyncSetting( syncSettingBuilder.build() );
        settingBuilder.setMonitoringSetting( monitoringBuilder.build() );

        storageBuilder.setSettings( settingBuilder.build() );
        storageBuilder.setCreatedBy( UserOuterClass.User.newBuilder().setId( "user-alpha-id-001" ).setName( "alpha" ).setNickname( "beta" ).build() );
        storageBuilder.setCreatedAt( Utilities.DateTime.newBuilder().setStrDateTime( "2023-11-03 12:23:34.123" ).build() );
        return storageBuilder.build();
    }

    public JdbcConnector getConnector( StorageOuterClass.Storage storage ) {
        var sql = select( DataStorageAdaptorTable.driver )
                .from( DataStorageAdaptorTable.table )
                .where( Equal.of( DataStorageAdaptorTable.id, storage.getAdaptorId() ) )
                .generate()
                .getStatement();
        var resultTable = dc.execute( sql ).getData().getTable();
        if( resultTable.getRowsCount() != 1 || resultTable.getRows( 0 ).getCellCount() != 1 ) {
            log.error( "[ New Storage ] Storage[ {} / {} ] Not found Adaptor ID[ {} ]",
                    storage.getId(), storage.getName(), storage.getAdaptorId() );
            return null;
        }
        var driver = resultTable.getRows( 0 ).getCell( 0 ).getStringValue();


        Map<String, Object> basic = new HashMap<>();
        for (var op : storage.getBasicOptionsList()) {
            basic.put(op.getKey().toLowerCase(), convertInputField(op));
        }
        Properties addition = new Properties();
        for (var op : storage.getAdditionalOptionsList()) {
            addition.put(op.getKey().toLowerCase(), convertInputField(op));
        }

        try( var connector = new JdbcConnector.Builder()
                .withUrlFormat( storage.getUrl() )
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
                return conn;
            } else {
                log.error( "[ New Storage ] [ {} / {} ] Connect Fail", storage.getId(), storage.getName() );
                return null;
            }
        } catch( ClassNotFoundException | SQLException e ) {
            log.error( e.getMessage(), e );
            log.error( "[ New Storage ] [ {} / {} ] Connect Fail", storage.getId(), storage.getName() );
            return null;
        }
    }

    public ArrayList<Utilities.Meta> getStorageMeta( StorageOuterClass.Storage storage, JdbcConnector.Cursor cur ) throws Exception {
        ArrayList<Utilities.Meta> metas = new ArrayList<>();

        final String[] param = new String[ 2 ];
        storage.getBasicOptionsList().forEach( x -> {
            if( x.getKey().equalsIgnoreCase( "DATABASE" ) ) {
                param[ 0 ] = x.getValue();
            }
            if( x.getKey().equalsIgnoreCase( "USER" ) ) {
                param[ 1 ] = x.getValue();
            }
        } );

        // Set Storage Name
        metas.add( Utilities.Meta.newBuilder().setKey( "DATABASE_NAME" ).setValue( param[ 0 ] ).build() );

        // Set Storage Owner : Get Database Owner
        if( storage.getStorageType().equalsIgnoreCase( "postgresql" ) ) {
            var sql = new SQL_Generator().getDatabaseOwner( storage.getStorageType(), param[ 0 ] );
            cur.execute( sql );
            var rs = cur.getResultSet();
            if( rs.next() ) {
                metas.add( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( rs.getString( 1 ) ).build() );
            }
        } else {
            metas.add( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( param[ 0 ] ).build() );
        }

        return metas;
    }

    private List<DataModelOuterClass.DataModel.Builder> getDataList( StorageOuterClass.Storage storage, JdbcConnector.Cursor cur ) throws Exception {
        for( StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt : storage.getSettings().getAutoAddSetting().getOptionsList() ) {
            // TODO : Data Type Filter 처리 필요
            var regexResult = FilterNameAndFormat( storage, opt, cur );
            return FilterSize( storage, opt, cur, regexResult );
            // TODO : 시간 필터링 처리 필요
        }

        return null;
    }

    private List<DataModelOuterClass.DataModel.Builder> FilterNameAndFormat(
            StorageOuterClass.Storage storage,
            StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt,
            JdbcConnector.Cursor cur ) throws Exception {

        List<DataModelOuterClass.DataModel.Builder> dataModels = new ArrayList<>();
        cur.execute( new SQL_Generator().getTablesInfo( storage.getStorageType() ) );
        var rs = cur.getResultSet();
        while( rs.next() ) {
            if( !opt.getRegex().equalsIgnoreCase( "*" ) ) {
                Pattern pattern = Pattern.compile( opt.getRegex() );
                Matcher matcher = pattern.matcher( rs.getString( 1 ) );
                if( !matcher.find() ) {
                    log.debug( "Regex Not Match Pattern[ {} ] Target[ {} ]", opt.getRegex(), rs.getString( 1 ) );
                    System.out.println( "Match not found" );
                    continue;
                }
            }

            DataModelOuterClass.DataModel.Builder dataModelBuilder = DataModelOuterClass.DataModel.newBuilder();
            if( !opt.getDataFormat().isEmpty() ) {
                if( opt.getDataFormat().equalsIgnoreCase( "table" ) ) {
                    if( !rs.getString( 2 ).equalsIgnoreCase( "base table" ) ) continue;
                    dataModelBuilder.setDataType( "STRUCTURED" );
                    dataModelBuilder.setDataFormat( "TABLE" );
                } else if( opt.getDataFormat().equalsIgnoreCase( "view" ) ) {
                    if( rs.getString( 2 ).equalsIgnoreCase( "view" ) ) continue;
                    dataModelBuilder.setDataType( "STRUCTURED" );
                    dataModelBuilder.setDataFormat( "VIEW" );
                }
            }
            // Set Name(Table Name)
            dataModelBuilder.setName( rs.getString( 1 ) );
            // Set Status
            dataModelBuilder.setStatus( "CONNECTED" );
            log.debug( "New Data Name[ {} ]", rs.getString( 1 ) );
            // Set System Metadata ( owner )
            if( rs.getMetaData().getColumnCount() == 3 ) {
                dataModelBuilder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "NAME" ).setValue( rs.getString( 1 ) ).build() );
                dataModelBuilder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "OWNER" ).setValue( rs.getString( 3 ) ).build() );
                log.debug( "Data Name[ {} ] Metadata[ owner ][ {} ]", rs.getString( 1 ), rs.getString( 3 ) );
            }

            // Set Data Location
            for( int i = 0; i < storage.getSystemMetaCount(); i++ ) {
                if( storage.getSystemMeta( i ).getKey().equalsIgnoreCase( "database_name" ) ) {
                    var databaseName = storage.getSystemMeta( i ).getValue();
                    dataModelBuilder.addDataLocation(
                            DataModelOuterClass.DataLocation.newBuilder()
                                    .setStorageId( storage.getId() )
                                    .setDatabaseName( databaseName )
                                    .setTableName( rs.getString( 1 ) )
                                    .build() );
                    break;
                }
            }

            // Set Dat Refine
            dataModelBuilder.setDataRefine( DataModelOuterClass.DataRefine.newBuilder().setQuery( "select * from " + rs.getString( 1 ) ).build() );

            dataModels.add( dataModelBuilder );
        }
        return dataModels;
    }

    private List<DataModelOuterClass.DataModel.Builder> FilterSize(
            StorageOuterClass.Storage storage,
            StorageOuterClass.AutoAddSetting.AutoAddSettingOption opt,
            JdbcConnector.Cursor cur,
            List<DataModelOuterClass.DataModel.Builder> builders ) throws Exception {

        if( opt.getMinSize() <= 0 && opt.getMaxSize() <= 0 ) return builders;

        List<DataModelOuterClass.DataModel.Builder> dataModels = new ArrayList<>();
        for( DataModelOuterClass.DataModel.Builder builder : builders ) {
            cur.execute( new SQL_Generator().getTableSize( storage.getStorageType(), builder.getName() ) );
            var rs = cur.getResultSet();
            while( rs.next() ) {
                var rows = Long.parseLong( rs.getString( 1 ) );
                if( rows < opt.getMinSize() || opt.getMaxSize() < rows ) {
                    log.info( "Data[ {} ] Size(Row)[ {} ] Not Match Min[ {} ] / Max[ {} ]",
                            builder.getName(), rows, opt.getMinSize(), opt.getMaxSize() );
                    continue;
                }
                log.info( "Data[ {} ] Size(Row)[ {} ]", builder.getName(), rows );
                builder.addSystemMeta( Utilities.Meta.newBuilder().setKey( "ROWS" ).setValue( String.valueOf( rows ) ).build() );
                dataModels.add( builder );
            }
        }
        return dataModels;
    }


    private void setMetadata(
            StorageOuterClass.Storage storage,
            JdbcConnector.Cursor cursor,
            List<DataModelOuterClass.DataModel.Builder> dataModels ) throws Exception {

        for( int i = 0; i < dataModels.size(); i++ ) {
            // 테이블 코멘트
            var comment = getTableComment( storage, cursor, dataModels.get( i ).getName() );
            if( comment != null && !comment.isEmpty() ) {
                dataModels.get( i ).setDescription( comment );
                dataModels.get( i ).addSystemMeta( Utilities.Meta.newBuilder().setKey( "DESC" ).setValue( comment ).build() );
                log.info( "Data[ {} ] Comment[ {} ]", dataModels.get( i ).getName(), comment );
            } else {
                log.info( "Data[ {} ] No Have Comment", dataModels.get( i ).getName() );
            }
            // 데이터 구조
            dataModels.get( i ).addAllDataStructure( getDataStructure( storage, cursor, dataModels.get( i ).getName() ) );

            // 메타 데이터 설정
            dataModels.get( i ).addSystemMeta( Utilities.Meta.newBuilder().setKey( "FORMAT" ).setValue( "TABLE" ).build() );
            dataModels.get( i ).addSystemMeta(
                    Utilities.Meta.newBuilder().setKey( "COLUMNS" )
                            .setValue( String.valueOf( dataModels.get( i ).getDataStructureList().size() ) ).build() );

            // 권한  :
            dataModels.get( i ).setPermission( DataModelOuterClass.Permission.newBuilder().setRead( true ).setWrite( true ).build() );
            // 기타 데이터 설정
            dataModels.get( i ).setDownloadInfo( DataModelOuterClass.DownloadInfo.newBuilder().setStatus( DataModelOuterClass.DownloadInfo.DownloadStatus.READY ).build() );
            // 사용자 정보
            dataModels.get( i ).setCreator(
                    UserOuterClass.User.newBuilder()
                            .setId( "test-user-id-01" )
                            .setName( "test-user-name" )
                            .setNickname( "test-user-nickname" )
                            .setPhone( "+8210-1234-1234" )
                            .setEmail( "test-user@mobigen.com" )
                            .build() );
            // 생성 시간
            dataModels.get( i ).setCreatedAt(
                    Utilities.DateTime.newBuilder()
                            .setUtcTime( System.currentTimeMillis() ).build() );
        }
    }

    private String getTableComment( StorageOuterClass.Storage storage, JdbcConnector.Cursor cur, String tableName ) throws Exception {
        cur.execute( new SQL_Generator().getTableComment( storage.getStorageType(), tableName ) );
        var rs = cur.getResultSet();
        while( rs.next() ) {
            if( rs.getString( 1 ) != null ) return rs.getString( 1 );
        }
        return null;
    }

    private List<DataModelOuterClass.DataStructure> getDataStructure(
            StorageOuterClass.Storage storage,
            JdbcConnector.Cursor cur, String tableName ) throws Exception {
        List<DataModelOuterClass.DataStructure> dataStructures = new ArrayList<>();

        cur.execute( new SQL_Generator().getTableStructure( storage.getStorageType(), tableName ) );
        var rs = cur.getResultSet();
        while( rs.next() ) {
            DataModelOuterClass.DataStructure.Builder dataStructureBuilder = DataModelOuterClass.DataStructure.newBuilder();

            dataStructureBuilder.setOrder( Integer.parseInt( rs.getString( 1 ) ) );
            dataStructureBuilder.setName( rs.getString( 2 ) );
            dataStructureBuilder.setColType( rs.getString( 3 ) );
            if( rs.getString( 4 ) != null && !rs.getString( 4 ).isEmpty() ) {
                dataStructureBuilder.setLength( rs.getInt( 4 ) );
            } else {
                if( rs.getString( 5 ) != null && !rs.getString( 5 ).isEmpty() ) {
                    dataStructureBuilder.setLength( rs.getLong( 5 ) > Integer.MAX_VALUE ? -1 : rs.getInt( 5 ) );
                }
            }
            dataStructureBuilder.setDefaultValue( rs.getString( 6 ) == null ? "" : rs.getString( 6 ) );
            dataStructureBuilder.setDescription( rs.getString( 7 ) == null ? "" : rs.getString( 7 ) );

            log.error( "Column : " + dataStructureBuilder.toString() );
            dataStructures.add( dataStructureBuilder.build() );
        }
        return dataStructures;
    }
}
