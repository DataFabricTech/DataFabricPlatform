package com.mobigen.monitoring.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.monitoring.config.ServiceModelRegistry;
import com.mobigen.monitoring.config.ServiceTypeConfig;
import com.mobigen.monitoring.domain.*;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.enums.ConnectionStatus;
import com.mobigen.monitoring.enums.DatabasePort;
import com.mobigen.monitoring.enums.DatabaseType;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.repository.MonitoringHistoryRepository;
import com.mobigen.monitoring.repository.ServicesRepository;
import com.mobigen.monitoring.repository.SlowQueryStatisticRepository;
import com.mobigen.monitoring.service.ConnectionService;
import com.mobigen.monitoring.service.ModelService;
import com.mobigen.monitoring.service.k8s.K8SService;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.utils.Utils;
import com.mobigen.monitoring.vo.*;
import com.zaxxer.hikari.HikariDataSource;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import static com.mobigen.monitoring.dto.request.DatabaseConnectionRequest.*;
import static com.mobigen.monitoring.enums.Common.*;
import static com.mobigen.monitoring.enums.ConnectionStatus.*;
import static com.mobigen.monitoring.enums.DatabaseType.*;
import static com.mobigen.monitoring.enums.OpenMetadataEnum.*;
import static com.mobigen.monitoring.enums.Queries.*;
import static com.mobigen.monitoring.service.query.QueryLoader.loadQuery;
import static com.mobigen.monitoring.utils.UnixTimeUtil.getCurrentMillis;
import static java.lang.Boolean.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseManagementServiceImpl implements DatabaseManagementService {
    // repositories
    private final ServicesRepository servicesRepository;
    private final SlowQueryStatisticRepository slowQueryStatisticRepository;
    private final MonitoringHistoryRepository monitoringHistoryRepository;

    private final Utils utils = new Utils();
    private final ServiceModelRegistry serviceModelRegistry;
    private final ServiceTypeConfig serviceTypeConfig;

    // services
    private final ModelRegistrationService modelRegistrationService;
    private final ConnectionService connectionService;
    private final MetadataService metadataService;
    private final K8SService k8sService;
    private final ModelService modelService;

    // kubernetes api 사용을 위한 host 주소
    @Value("${k8s.ip}")
    private String hostIP;

    /**
     * application 실행 시 실행
     * db에 데이터가 없을 경우 open metadata 에서 정보 받아서 디비에 저장
     * in memory 에 open metadata 로 부터 받은 데이터를 저장
     */
    @PostConstruct
    public void init() {
        final List<Services> allService = servicesRepository.findAll();

        if (allService.isEmpty()) {
            // db 에 값이 없을 경우 service, connection, model 정보들을 디비에 저장
            initializeServiceTable(SCHEDULER.getName());
        } else {
            // in-memory 에만 저장
            modelService.getServiceListFromFabricServer();
        }

        // connection 정보를 in-memory 에 저장하는 로직
        for (Services service : allService) {
            if (service.getServiceType().equals(MINIO.getName())) {
                GetObjectStorageResponseDto storageService = serviceModelRegistry.getStorageServices().get(service.getServiceID().toString());

                if (storageService != null)
                    storageService.setConnectionStatus(service.getConnectionStatus());
            } else {
                GetDatabasesResponseDto databaseService = serviceModelRegistry.getDatabaseServices().get(service.getServiceID().toString());

                if (databaseService != null)
                    databaseService.setConnectionStatus(service.getConnectionStatus());
            }
        }
    }

    /**
     * Service(object storage, database) 의 connection 을 확인하는 함수
     */
    @Override
    public Boolean checkServiceConnection(DatabaseConnectionRequest request) {
        if (!isDatabaseService(request.getDbType())) {
            return checkMinioConnection(request) != null ? TRUE : FALSE;
        }

        DataSource dataSource = createDataSource(request);

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);  // 2초 내에 응답 확인
        } catch (SQLException e) {
            log.error("Connection refused");

            return false;
        }
    }

    @Override
    public DataSource createDataSource(DatabaseConnectionRequest request) {
        HikariDataSource dataSource = new HikariDataSource();

        String jdbcUrl = buildJdbcUrl(request);

        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(request.getUsername());
        dataSource.setPassword(request.getPassword());
        dataSource.setDriverClassName(getDriverClassName(request.getDbType()));

        dataSource.setMaximumPoolSize(2);  // 연결 풀 최소화
        dataSource.setConnectionTimeout(2000);  // 2초 안에 연결 시도
        dataSource.setMinimumIdle(5);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(60000);

        dataSource.addDataSourceProperty("socketTimeout", "5000");  // 5초 제한
        dataSource.addDataSourceProperty("connectTimeout", "3000"); // 3초 제한
        dataSource.addDataSourceProperty("tcpKeepAlive", "true");   // 연결 유지

        // Java DNS 캐싱 비활성화
        System.setProperty("networkaddress.cache.ttl", "0");
        System.setProperty("networkaddress.cache.negative.ttl", "0");

        return dataSource;
    }

    @Override
    public String buildJdbcUrl(DatabaseConnectionRequest request) {
        if (request.getDbType().equalsIgnoreCase(ORACLE.getName())) {
            if (request.getDatabaseName() == null)
                throw new CustomException("Database");
            return "jdbc:oracle:thin:@" + request.getHost() + ":" + request.getPort() + "/" + request.getDatabaseName();
        } else if (request.getDbType().equalsIgnoreCase(POSTGRES.getName())) {
            return "jdbc:postgresql://" + request.getHost() + ":" + request.getPort() + "/" + request.getDatabaseName();
        } else if (request.getDbType().equalsIgnoreCase(MARIADB.getName()) || request.getDbType().equalsIgnoreCase(MYSQL.getName())) {
            return "jdbc:" + request.getDbType().toLowerCase() + "://" + request.getHost() + ":" + request.getPort();
        } else {
            log.error("Unknown database type: {}", request.getDbType());

            throw new CustomException(
                    ResponseCode.DFM5000,
                    String.format("Unsupported database type %s", request.getDbType()),
                    request.getDbType()
            );
        }
    }

    /**
     * RDB Database 리스트를 가져오는 함수
     */
    @Override
    public List<String> getDatabases(DatabaseConnectionRequest request) {
        String query;
        List<String> databases = new ArrayList<>();

        if (request.getDbType().equalsIgnoreCase("postgres")) {
            query = POSTGRES_GET_DATABASES.getQueryString();

            for (Map<String, Object> response : executeQuery(request, query)) {
                databases.add((String) response.get("datname"));
            }
        } else if (request.getDbType().equalsIgnoreCase("oracle")) {
            query = ORACLE_GET_DATABASES.getQueryString();

            for (Map<String, Object> response : executeQuery(request, query)) {
                databases.add(response.get("DATABASE").toString());
            }
        } else if (request.getDbType().equalsIgnoreCase("mysql") ||
                request.getDbType().equalsIgnoreCase("mariadb")) {
            query = RDB_GET_DATABASES.getQueryString();

            for (Map<String, Object> response : executeQuery(request, query)) {
                databases.add(response.get("Database").toString());
            }
        } else {
            log.error("Unsupported database type: {}", request.getDbType());
        }

        return databases;
    }

    /**
     * Table 리스트를 가져오는 함수
     */
    @Override
    public Map<String, List<String>> getTables(DatabaseConnectionRequest request) {
        String query;
        if (request.getDbType().equalsIgnoreCase(ORACLE.getName())) {
            query = GET_TABLES_FROM_ORACLE.getQueryString();
        } else {
            query = GET_TABLES.getQueryString();
        }

        final List<Map<String, Object>> data = executeQuery(request, query);
        Map<String, List<String>> schemaMap = new HashMap<>();

        for (Map<String, Object> entry : data) {
            String schema = (String) entry.get("table_schema");
            String tableName = (String) entry.get("table_name");

            schemaMap.computeIfAbsent(schema, k -> new ArrayList<>()).add(tableName);
        }

        return schemaMap;
    }

    @Override
    @Transactional
    public Object getRows(DatabaseConnectionRequest request) {
        String query;

        if (request.getDbType().equalsIgnoreCase(ORACLE.getName())) {
            query = GET_ALL_TABLE_ROWS_FROM_ORACLE.getQueryString();
        } else {
            query = GET_ALL_TABLE_ROWS.getQueryString();
        }

        List<String> queries = new ArrayList<>();

        final List<Map<String, Object>> maps = executeQuery(request, query);

        for (Map<String, Object> entry : maps) {
            if (request.getDbType().equalsIgnoreCase(ORACLE.getName())) queries.add((String) entry.get("QUERY"));
            else queries.add((String) entry.get("query"));
        }

        DataSource dataSource = createDataSource(request);
        Connection conn = DataSourceUtils.getConnection(dataSource);

        List<Object> results = new ArrayList<>();

        try {
            conn.setAutoCommit(false); // 수동 커밋 모드 (트랜잭션 유지)

            for (String queryString : queries) {
                log.info("queryString: {}", queryString);
                try (PreparedStatement stmt = conn.prepareStatement(queryString);
                     ResultSet rs = stmt.executeQuery()) { // SELECT 실행
                    List<Map<String, Object>> resultSetData = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();

                    log.info("metadata: {}", metaData);
                    int columnCount = metaData.getColumnCount();

                    log.info("columnCount: {}", columnCount);

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }

                        if (resultSetData.size() <= 1) {
                            results.add(row);
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException rollbackEx) {
                log.error("rollback exception: {}", rollbackEx.getMessage(), rollbackEx);
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }

        return results;
    }

    @Override
    public Object getSchema(DatabaseConnectionRequest request) {
        String query = GET_SCHEMA.getQueryString();
        final List<Map<String, Object>> maps = executeQuery(request, query);
        Map<String, List<TableSchemaInfo>> response = new HashMap<>();

        for (Map<String, Object> entry : maps) {
            String key = String.format("%s.%s", entry.get("table_schema"), entry.get("table_name"));

            response.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(TableSchemaInfo.builder()
                            .columnName(entry.get("column_name").toString())
                            .dataType(entry.get("data_type").toString())
                            .isNullable(entry.get("is_nullable").toString())
                            .columnDefault(entry.get("column_default") == null ? "null" : entry.get("column_default").toString())
                            .build());
        }

        return response;
    }

    @Override
    public List<Map<String, Object>> executeQuery(DatabaseConnectionRequest request, String query) {
        DataSource dataSource = createDataSource(request);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            return jdbcTemplate.queryForList(query);
        } finally {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
    }

    @Override
    public String getDriverClassName(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "postgres" -> "org.postgresql.Driver";
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            case "mariadb" -> "org.mariadb.jdbc.Driver";
            case "oracle" -> "oracle.jdbc.OracleDriver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    @Override
    public Integer checkMinioConnection(DatabaseConnectionRequest request) {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://" + request.getHost() + ":" + request.getPort())
                    .credentials(request.getUsername(), request.getPassword())
                    .build();

            return minioClient.listBuckets().size();  // Minio 연결 테스트
        } catch (MinioException | IllegalArgumentException e) {
            log.debug("Minio 연결 실패: " + e.getMessage());
            return null;
        } catch (IOException e) {
            log.error("[MINIO CONNECTION ERROR] Cannot accept minio", e);

            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error("[MINIO CONNECTION ERROR] Authentication failed", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * db 에 service 정보 저장
     * service, model registration, metadata, connection, connection history, monitoring history 등록
     */
    @Override
    public void initializeServiceTable(String userName) {
        // service 등록
        // Create Service entity
        final List<Services> services = saveServicesFromFabricServer();

        // connection 등록
        // model registration 저장
        saveConnections(services);

        // model registration 저장
        modelService.setServiceModels(modelService.getServiceListFromFabricServer());

        // collect data num 등록
        metadataService.save(services.size());

        // monitoring history 저장
        // cpu, memory, request, slow query
        for (Services service : services) {
            if (isRDBMS(service.getServiceType()) && service.getConnectionStatus().equals(ConnectionStatus.CONNECTED))
                saveMonitoringHistory(service, userName, getCurrentMillis());
        }
    }

    // connection 정보 저장
    public void saveConnections(final List<Services> services) {
        // response time == 직접 디비에서 show databases; 햔 시간
        // executed_at, execute_by, query_execution_time, service_id
        List<ConnectionDao> connections = new ArrayList<>();
        List<ConnectionHistory> connectionHistories = new ArrayList<>();
        final Long now = getCurrentMillis();

        for (Services service : services) {
            // check response time
            final CheckConnectionResponseVo checkDatabaseResponse = getQueryExecutedTime(service);

            // connection entity 생성
            connections.add(
                    ConnectionDao.builder()
                            .executeAt(getCurrentMillis())
                            .executeBy(SCHEDULER.getName())
                            .queryExecutionTime(checkDatabaseResponse.getResponseTime())
                            .serviceID(service.getServiceID())
                            .build()
            );

            // status update
            service.setConnectionStatus(checkDatabaseResponse.getStatus());

            // connection history
            connectionHistories.add(
                    ConnectionHistory.builder()
                            .connectionStatus(checkDatabaseResponse.getStatus())
                            .serviceID(service.getServiceID())
                            .updatedAt(getCurrentMillis())
                            .build()
            );

            // save model registration
            saveModelRegistration(service);

            // 시간 업데이트
            service.setUpdatedAt(now);
            service.setCreatedAt(now);
        }

        connectionService.saveAllConnection(connections);
        connectionService.saveAllConnectionHistory(connectionHistories);

        servicesRepository.saveAll(services);
    }

    public void saveModelRegistration(Services service) {
        if (!service.getServiceType().equalsIgnoreCase("Trino")) {
            if (service.getConnectionStatus().equals(ConnectionStatus.CONNECTED)) {
                if (service.getServiceType().equalsIgnoreCase("MinIO")) {
                    log.info("service is minio");
                    // minio 의 버킷 수 세기
                    final GetObjectStorageResponseDto objectStorageResponseDto = serviceModelRegistry.getStorageServices().get(service.getServiceID().toString());
                    final String endPointURL = objectStorageResponseDto.getConnection().getMinioConfig().getEndPointURL();

                    URI uri = URI.create(endPointURL);

                    modelRegistrationService.save(
                            ModelRegistration.builder()
                                    .serviceId(service.getServiceID())
                                    .updatedAt(getCurrentMillis())
                                    .omModelCount(modelService.getStorageModelCountFromOM(service.getServiceID()))
                                    .modelCount(
                                            checkMinioConnection(
                                                    builder()
                                                            .dbType(objectStorageResponseDto.getServiceType())
                                                            .host(uri.getHost())
                                                            .port(uri.getPort())
                                                            .databaseName(null)
                                                            .username(objectStorageResponseDto.getConnection().getMinioConfig().getAccessKeyId())
                                                            .password(objectStorageResponseDto.getConnection().getMinioConfig().getSecretKey())
                                                            .build()
                                            )
                                    )
                                    .build()
                    );
                } else {
                    modelRegistrationService.save(
                            ModelRegistration.builder()
                                    .serviceId(service.getServiceID())
                                    .updatedAt(getCurrentMillis())
                                    .omModelCount(modelService.getModelCountFromOM(service.getServiceID()).getTotal())
                                    .modelCount(getModelCount(service))
                                    .build()
                    );
                }
            } else {
                modelRegistrationService.save(
                        ModelRegistration.builder()
                                .serviceId(service.getServiceID())
                                .updatedAt(getCurrentMillis())
                                .omModelCount(modelService.getModelCountFromOM(service.getServiceID()).getTotal())
                                .modelCount(0)
                                .build()
                );
            }
        }
    }

    @Override
    public Integer getModelCount(Services service) {
        // table 개수 세기
        // 연결 안되는 서비스는 model 이 0
        if (!service.getConnectionStatus().equals(ConnectionStatus.CONNECTED)) return 0;

        if (isRDBMS(service.getServiceType())) {
            final String serviceId = service.getServiceID().toString();
            final GetDatabasesResponseDto getDatabasesResponseDto = serviceModelRegistry.getDatabaseServices().get(serviceId);
            final HostPortVo hostPortVo = convertStringToHostPortVo(getDatabasesResponseDto);
            String database = null;

            if (getDatabasesResponseDto.getConnection().getDatabaseName() != null) {
                database = getDatabasesResponseDto.getConnection().getDatabaseName();
            }

            if (getDatabasesResponseDto.getConnection().getDatabase() != null) {
                database = getDatabasesResponseDto.getConnection().getDatabase();
            }

            if (getDatabasesResponseDto.getConnection().getOracleConnectionType() != null) {
                database = getDatabasesResponseDto.getConnection().getOracleConnectionType().getOracleServiceName();
            }

            // database 정보가 없는 경우
            if (database == null) {
                log.debug("service: {}", getDatabasesResponseDto);
            }

            DatabaseConnectionRequest request = builder()
                    .username(getDatabasesResponseDto.getConnection().getUsername())
                    .dbType(service.getServiceType())
                    .host(hostPortVo.getHost())
                    .port(hostPortVo.getPort())
                    .databaseName(database)
                    .password(getDatabasesResponseDto.getConnection().getPassword() == null ?
                            getDatabasesResponseDto.getConnection().getAuthType().getPassword()
                            : getDatabasesResponseDto.getConnection().getPassword())
                    .build();

            final Map<String, List<String>> tables = getTables(request);

            return tables.values().stream().mapToInt(List::size).sum();
        } else {
            // minio 의 버킷 수 세기
            final GetObjectStorageResponseDto objectStorageResponseDto = serviceModelRegistry.getStorageServices().get(service.getServiceID().toString());
            final String endPointURL = objectStorageResponseDto.getConnection().getMinioConfig().getEndPointURL();

            URI uri = URI.create(endPointURL);

            final DatabaseConnectionRequest build = builder()
                    .dbType(objectStorageResponseDto.getServiceType())
                    .host(uri.getHost())
                    .port(uri.getPort())
                    .databaseName(null)
                    .username(objectStorageResponseDto.getConnection().getMinioConfig().getAccessKeyId())
                    .password(objectStorageResponseDto.getConnection().getMinioConfig().getSecretKey())
                    .build();

            return checkMinioConnection(build);
        }
    }

    @Override
    public CheckConnectionResponseVo getQueryExecutedTime(final Services service) {
        // measure time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.debug("[Connection Check] START]");

        ConnectionStatus status;
        Boolean isDatabaseService = DatabaseType.isDatabaseService(service.getServiceType());

        // trino or 존재하지 않는 Database Type 일 경우
        if (serviceTypeConfig.findType(service.getServiceType()) == null) {
            log.debug("service type: {}", service.getServiceType());

            return CheckConnectionResponseVo.builder()
                    .status(DISCONNECTED)
                    .responseTime(stopWatch.getTotalTimeMillis())
                    .build();
        }

        final DatabaseConnectionRequest request;

        //rdb
        if (isDatabaseService(service.getServiceType())) {
            request = getDatabaseConnectionRequest(service.getServiceID().toString());
        } else {
            // minio
            final GetObjectStorageResponseDto storageResponse = serviceModelRegistry.getStorageServices().get(service.getServiceID().toString());

            String endpointUrl = storageResponse.getConnection().getMinioConfig().getEndPointURL();

            try {
                URL url = new URL(endpointUrl);

                request = builder()
                        .dbType(storageResponse.getServiceType())
                        .host(url.getHost())
                        .port(url.getPort())
                        .databaseName(null)
                        .username(storageResponse.getConnection().getMinioConfig().getAccessKeyId())
                        .password(storageResponse.getConnection().getMinioConfig().getSecretKey())
                        .build();
            } catch (MalformedURLException e) {
                log.error("Invalid form endpoint URL: {}", endpointUrl);

                throw new CustomException("Invalid form endpoint URL");
            }
        }

        // db connection check
        final Boolean connectionCheckResponse = checkServiceConnection(request);

        // connection check failed
        if (!connectionCheckResponse) {
            log.error("[ MONITORING ] DB Connection Error");

            return CheckConnectionResponseVo.builder()
                    .status(DISCONNECTED)
                    .responseTime(stopWatch.getTotalTimeMillis())
                    .build();
        } else {
            log.info("[ MONITORING ] DB Connection Success");

            status = CONNECTED;
        }

        // get databases
        // CONNECTED 인 service
        final List<String> databases = getDatabases(request);

        log.info("[ MONITORING ] Successfully get databases: {}", databases.size());

        stopWatch.stop();

        log.debug("[Connection Check] START]");

        final long responseTime = stopWatch.getTotalTimeMillis();

        // in-memory 에 있는 서비스들의 connection 정보 업데이트
        if (isDatabaseService) {
            GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(service.getServiceID().toString());

            serviceInfo.setResponseTime(responseTime);
            serviceInfo.setConnectionStatus(status);
        } else {
            GetObjectStorageResponseDto serviceInfo = serviceModelRegistry.getStorageServices().get(service.getServiceID().toString());

            serviceInfo.setConnectionStatus(status);
            serviceInfo.setResponseTime(responseTime);
        }

        // 실행 시간은 ms
        return CheckConnectionResponseVo.builder()
                .status(status)
                .responseTime(responseTime)
                .build();
    }

    @Override
    public List<Services> saveServicesFromFabricServer() {
        List<JsonNode> currentServices = modelService.getServiceListFromFabricServer();

        List<Services> services = new ArrayList<>();

        // Create Service entity
        for (JsonNode currentService : currentServices) {
            Long dateTime = currentService.get(UPDATED_AT.getName()).asLong();
            services.add(
                    Services.builder()
                            .serviceID(
                                    UUID.fromString(
                                            currentService.get(ID.getName()).asText().replace("\"", ""))
                            )
                            .name(currentService.get(NAME.getName()).asText())
                            .displayName(utils.getAsTextOrNull(currentService.get(DISPLAY_NAME.getName())))
                            .createdAt(dateTime)
                            .updatedAt(dateTime)
                            .serviceType(currentService.get(SERVICE_TYPE.getName()).asText())
                            .ownerName(currentService.get(UPDATED_BY.getName()).asText())
                            .connectionStatus(DISCONNECTED)
                            .build()
            );
        }

        return servicesRepository.saveAll(services);
    }

    @Override
    public DatabaseConnectionRequest getDatabaseConnectionRequest(String serviceId) {
        final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
        final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);
        String databaseName;

        if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
            if (serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema() != null) {
                databaseName = serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema();
            } else {
                databaseName = serviceInfo.getConnection().getOracleConnectionType().getOracleServiceName();
            }
        } else {
            databaseName = serviceInfo.getConnection().getDatabaseName() == null ?
                    serviceInfo.getConnection().getDatabase() : serviceInfo.getConnection().getDatabaseName();
        }

        return builder()
                .dbType(serviceInfo.getServiceType())
                .host(hostPortVo.getHost())
                .port(hostPortVo.getPort())
                .databaseName(databaseName)
                .username(serviceInfo.getConnection().getUsername())
                .password(
                        serviceInfo.getConnection().getPassword() == null ?
                                serviceInfo.getConnection().getAuthType().getPassword() :
                                serviceInfo.getConnection().getPassword()
                )
                .build();
    }

    @Override
    public CpuUsedVo getCpuSpentTime(String serviceId) {
        try {
            final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
            String query;

            if (serviceInfo != null) {
                if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName())) {
                    query = loadQuery(GET_MYSQL_CPU_SPENT_TIME.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                    query = loadQuery(GET_MARIADB_CPU_SPENT_TIME.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
                    query = loadQuery(GET_ORACLE_CPU_SPENT_TIME.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(POSTGRES.getName())) {
                    query = loadQuery(GET_POSTGRES_CPU_SPENT_TIME.getQuery());
                } else {
                    throw new CustomException(
                            ResponseCode.DFM5000,
                            "Unknown service type: " + serviceInfo.getServiceType(),
                            serviceInfo.getServiceType()
                    );
                }
            } else {
                throw new CustomException(
                        ResponseCode.DFM6000,
                        String.format("service not found [%s]", serviceId),
                        serviceId
                );
            }

            final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);

            final List<Map<String, Object>> maps = executeQuery(
                    builder()
                            .dbType(serviceInfo.getServiceType())
                            .host(hostPortVo.getHost())
                            .port(hostPortVo.getPort())
                            .databaseName(getDatabaseName(serviceInfo))
                            .username(serviceInfo.getConnection().getUsername())
                            .password(
                                    serviceInfo.getConnection().getPassword() == null ?
                                            serviceInfo.getConnection().getAuthType().getPassword() :
                                            serviceInfo.getConnection().getPassword()
                            )
                            .build(),
                    query
            );

            Map<String, Object> row = maps.getFirst();
            Object cpuUsedObj = row.get("cpu_used");

            double cpuUsed = cpuUsedObj instanceof Number
                    ? ((Number) cpuUsedObj).doubleValue()
                    : 0;

            return CpuUsedVo.builder()
                    .cpuUsed(cpuUsed)
                    .build();
        } catch (BadSqlGrammarException e) {
            log.debug("Insufficient privileges");

            return null;
        }
    }

    @Override
    public Map<String, CpuUsedVo> getCpuSpentTime() {
        // 모든 서비스들의 cpu 사용 시간 조회
        Map<String, CpuUsedVo> result = new HashMap<>();

        for (GetDatabasesResponseDto serviceInfo : serviceModelRegistry.getDatabaseServices().values()) {
            if (serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                try {
                    final CpuUsedVo maps = getCpuSpentTime(serviceInfo.getId());

                    result.put(serviceInfo.getName(), maps);
                } catch (BadSqlGrammarException e) {
                    log.error("This service is insufficient privileges: {}, {}", serviceInfo.getName(), serviceInfo.getConnection().getUsername());
                }
            }
        }

        return result;
    }

    @Override
    public MemoryUsedVo getMemoryUsage(String serviceId) {
        try {
            final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
            String query;

            if (serviceInfo != null) {
                if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName())) {
                    query = loadQuery(GET_MYSQL_MEMORY_USAGE.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                    query = loadQuery(GET_MARIADB_MEMORY_USAGE.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
                    query = loadQuery(GET_ORACLE_MEMORY_USAGE.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(POSTGRES.getName())) {
                    query = loadQuery(GET_POSTGRES_MEMORY_USAGE.getQuery());
                } else {
                    throw new CustomException("service type is invalid");
                }
            } else {
                throw new CustomException("Service not found");
            }

            final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);

            final List<Map<String, Object>> maps = executeQuery(
                    builder()
                            .dbType(serviceInfo.getServiceType())
                            .host(hostPortVo.getHost())
                            .port(hostPortVo.getPort())
                            .databaseName(getDatabaseName(serviceInfo))
                            .username(serviceInfo.getConnection().getUsername())
                            .password(
                                    serviceInfo.getConnection().getPassword() == null ?
                                            serviceInfo.getConnection().getAuthType().getPassword() :
                                            serviceInfo.getConnection().getPassword()
                            )
                            .build(),
                    query
            );

            Map<String, Object> row = maps.getFirst();
            Object memoryUsedMB = row.get("memory_usage_mb");

            double memoryUsed = memoryUsedMB instanceof Number
                    ? ((Number) memoryUsedMB).doubleValue()
                    : 0;

            return MemoryUsedVo.builder()
                    .memoryUsed(memoryUsed)
                    .build();
        } catch (BadSqlGrammarException e) {
            log.debug("Insufficient privileges");
            return null;
        }
    }

    @Override
    public Map<String, MemoryUsedVo> getMemoryUsage() {
        Map<String, MemoryUsedVo> result = new HashMap<>();

        for (GetDatabasesResponseDto serviceInfo : serviceModelRegistry.getDatabaseServices().values()) {
            try {
                if (serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                    final MemoryUsedVo maps = getMemoryUsage(serviceInfo.getId());

                    result.put(serviceInfo.getName(), maps);
                }
            } catch (BadSqlGrammarException e) {
                log.error("Insufficient privileges: {}, {}", serviceInfo.getName(), serviceInfo.getConnection().getUsername());
            }
        }

        return result;
    }

    @Override
    public Map<String, Double> getDiskUsage(String serviceId) {
        final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
        String query;

        if (serviceInfo != null) {
            if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName()) || serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                query = loadQuery(GET_MYSQL_DISK_USAGE.getQuery());
            } else if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
                query = loadQuery(GET_ORACLE_DISK_USAGE.getQuery());
            } else if (serviceInfo.getServiceType().equalsIgnoreCase(POSTGRES.getName())) {
                query = loadQuery(GET_POSTGRES_DISK_USAGE.getQuery());
            } else {
                throw new CustomException("service type is invalid");
            }
        } else {
            throw new CustomException("Service not found");
        }

        final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);

        final List<Map<String, Object>> maps = executeQuery(
                builder()
                        .dbType(serviceInfo.getServiceType())
                        .host(hostPortVo.getHost())
                        .port(hostPortVo.getPort())
                        .databaseName(getDatabaseName(serviceInfo))
                        .username(serviceInfo.getConnection().getUsername())
                        .password(
                                serviceInfo.getConnection().getPassword() == null ?
                                        serviceInfo.getConnection().getAuthType().getPassword() :
                                        serviceInfo.getConnection().getPassword()
                        )
                        .build(),
                query
        );

        if (maps == null) {
            log.error("user is not admin");

            return Map.of("totalDbSizeMB", 0.0);
        } else {
            Map<String, Object> row = maps.getFirst();
            Object totalDbSizeMb = row.get("total_db_size_mb");

            double memoryUsed = totalDbSizeMb instanceof Number
                    ? ((Number) totalDbSizeMb).doubleValue()
                    : 0;

            return Map.of("totalDbSizeMB", memoryUsed);
        }
    }

    @Override
    public Object getAllDiskUsage() {
        Map<String, Object> result = new HashMap<>();

        for (GetDatabasesResponseDto serviceInfo : serviceModelRegistry.getDatabaseServices().values()) {
            try {
                if (serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                    final Map<String, Double> maps = getDiskUsage(serviceInfo.getId());

                    result.put(serviceInfo.getName(), maps);
                }
            } catch (BadSqlGrammarException e) {
                log.error("Insufficient privileges: {}, {}", serviceInfo.getName(), serviceInfo.getConnection().getUsername());
            }
        }

        return result;
    }

    @Override
    public QueryStatisticsVo getAverageQueryOutcome(String serviceId) {
        try {
            final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
            String query;

            if (serviceInfo != null) {
                if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName())) {
                    query = loadQuery(GET_MYSQL_REQUEST_RATE_QUERY.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                    query = loadQuery(GET_MARIADB_REQUEST_RATE_QUERY.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
                    query = loadQuery(GET_ORACLE_REQUEST_RATE_QUERY.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(POSTGRES.getName())) {
                    query = loadQuery(GET_POSTGRES_REQUEST_RATE_QUERY.getQuery());
                } else {
                    throw new CustomException("service type is invalid");
                }
            } else {
                throw new CustomException("Service not found");
            }

            final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);

            final List<Map<String, Object>> maps = executeQuery(
                    builder()
                            .dbType(serviceInfo.getServiceType())
                            .host(hostPortVo.getHost())
                            .port(hostPortVo.getPort())
                            .databaseName(getDatabaseName(serviceInfo))
                            .username(serviceInfo.getConnection().getUsername())
                            .password(
                                    serviceInfo.getConnection().getPassword() == null ?
                                            serviceInfo.getConnection().getAuthType().getPassword() :
                                            serviceInfo.getConnection().getPassword()
                            )
                            .build(),
                    query
            );

            Map<String, Object> row = maps.getFirst();
            Object successQueriesObj = row.get("success_queries");
            Object failedQueriesObj = row.get("failed_queries");

            int successQueries = successQueriesObj instanceof Number
                    ? ((Number) successQueriesObj).intValue()
                    : 0;

            int failedQueries = failedQueriesObj instanceof Number
                    ? ((Number) failedQueriesObj).intValue()
                    : 0;

            return QueryStatisticsVo.builder()
                    .successQueries(successQueries)
                    .failedQueries(failedQueries)
                    .build();
        } catch (BadSqlGrammarException e) {
            log.debug("Insufficient privileges");
            return null;
        }
    }

    @Override
    public Map<String, QueryStatisticsVo> getAverageQueryOutcome() {
        Map<String, QueryStatisticsVo> result = new HashMap<>();

        for (GetDatabasesResponseDto serviceInfo : serviceModelRegistry.getDatabaseServices().values()) {
            try {
                if (serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                    final QueryStatisticsVo maps = getAverageQueryOutcome(serviceInfo.getId());

                    result.put(serviceInfo.getName(), maps);
                }
            } catch (BadSqlGrammarException e) {
                log.error("Insufficient privileges: {}, {}", serviceInfo.getName(), serviceInfo.getConnection().getUsername());
            }
        }

        return result;
    }

    @Override
    public List<SlowQueryVo> getSlowQueries(String serviceId) {
        try {
            final GetDatabasesResponseDto serviceInfo = serviceModelRegistry.getDatabaseServices().get(serviceId);
            String query;

            List<SlowQueryVo> response = new ArrayList<>();

            if (serviceInfo != null) {
                if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName()) || serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                    query = loadQuery(GET_MYSQL_SLOW_QUERY.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
                    query = loadQuery(GET_ORACLE_SLOW_QUERY.getQuery());
                } else if (serviceInfo.getServiceType().equalsIgnoreCase(POSTGRES.getName())) {
                    query = loadQuery(GET_POSTGRES_SLOW_QUERY.getQuery());
                } else {
                    throw new CustomException("service type is invalid");
                }
            } else {
                throw new CustomException("Service not found");
            }

            final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);

            final List<Map<String, Object>> slowQueries = executeQuery(
                    builder()
                            .dbType(serviceInfo.getServiceType())
                            .host(hostPortVo.getHost())
                            .port(hostPortVo.getPort())
                            .databaseName(getDatabaseName(serviceInfo))
                            .username(serviceInfo.getConnection().getUsername())
                            .password(
                                    serviceInfo.getConnection().getPassword() == null ?
                                            serviceInfo.getConnection().getAuthType().getPassword() :
                                            serviceInfo.getConnection().getPassword()
                            )
                            .build(),
                    query
            );

            // response dto 형태로 변환
            if (serviceInfo.getServiceType().equalsIgnoreCase(MYSQL.getName())) {
                for (Map<String, Object> slowQuery : slowQueries) {
                    response.add(
                            SlowQueryVo.builder()
                                    .sqlText(
                                            new String((byte[]) slowQuery.get("sql_text"))
                                    )
                                    .totalCount(Integer.valueOf(slowQuery.get("total_count").toString()))
                                    .avgExecTime(Float.valueOf(slowQuery.get("avg_exec_time").toString()))
                                    .build()
                    );
                }
            } else if (serviceInfo.getServiceType().equalsIgnoreCase(MARIADB.getName())) {
                for (Map<String, Object> slowQuery : slowQueries) {
                    response.add(
                            SlowQueryVo.builder()
                                    .sqlText(slowQuery.get("sql_text").toString())
                                    .totalCount(Integer.valueOf(slowQuery.get("total_count").toString()))
                                    .avgExecTime(Float.valueOf(slowQuery.get("avg_exec_time").toString()))
                                    .build()
                    );
                }
            } else {
                for (Map<String, Object> slowQuery : slowQueries) {
                    response.add(
                            SlowQueryVo.builder()
                                    .sqlText(slowQuery.get("sql_text").toString())
                                    .totalCount(Integer.valueOf(slowQuery.get("total_count").toString()))
                                    .avgExecTime(Float.valueOf(slowQuery.get("avg_exec_time").toString()))
                                    .build()
                    );
                }
            }

            return response;
        } catch (BadSqlGrammarException e) {
            log.debug("Insufficient privileges");
            return List.of();
        }
    }

    @Override
    public Map<String, List<SlowQueryVo>> getSlowQueries() {
        Map<String, List<SlowQueryVo>> result = new HashMap<>();

        servicesRepository.findAll().forEach(serviceInfo -> {
            try {
                if (isRDBMS(serviceInfo.getServiceType()) && serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                    result.put(serviceInfo.getServiceID().toString(), getSlowQueries(serviceInfo.getServiceID().toString()));
                }
            } catch (BadSqlGrammarException e) {
                log.error("Not sufficient privileges");
            }
        });

        return result;
    }

    /**
     * TODO exception 처리
     */
    @Override
    public void updateDatabaseInfo(String serviceId) {
        // connection, connection history, service, slow query, model registration
        if (serviceId == null || serviceId.isEmpty()) {
            // all update
            log.debug("[UPDATE INFO] START update all service database info");

            List<Services> allService = servicesRepository.findAll();

            saveConnections(allService);

            // Connected status service
            allService.forEach(serviceInfo -> {
                if (isRDBMS(serviceInfo.getServiceType()) && serviceInfo.getConnectionStatus().equals(CONNECTED)) {
                    log.debug("[UPDATE INFO] Save all service's monitoring history");

                    saveDatabaseMonitoringInfo(serviceInfo.getServiceID().toString(), SCHEDULER.getName());
                }
            });

            log.debug("[UPDATE INFO] END update all service database info");
        } else {
            log.debug("[UPDATE INFO] START update single service database info");

            Services service = servicesRepository.findById(UUID.fromString(serviceId)).orElseThrow(
                    () -> new CustomException("service not found")
            );

            // service, connection 업데이트
            saveConnections(List.of(service));

            log.debug("[UPDATE INFO] Save monitoring history");

            if (isRDBMS(service.getServiceType()) && service.getConnectionStatus().equals(CONNECTED))
                saveDatabaseMonitoringInfo(serviceId, SCHEDULER.getName());

            log.debug("[UPDATE INFO] END update single service database info");
        }
    }

    @Override
    public void saveDatabaseMonitoringInfo(String serviceId, String ownerName) {
        // insert request info
        // insert cpu, memory info
        // insert slow query info
        Long now = getCurrentMillis();

        if (serviceId == null || serviceId.isEmpty()) {
            for (Services service : servicesRepository.findAll()) {
                if (isRDBMS(service.getServiceType()) && service.getConnectionStatus().equals(CONNECTED))
                    saveMonitoringHistory(service, ownerName, now);
            }
        } else {
            Services service = servicesRepository.findById(UUID.fromString(serviceId)).orElseThrow(
                    () -> new CustomException("service not found")
            );

            if (isRDBMS(service.getServiceType()) && service.getConnectionStatus().equals(CONNECTED))
                saveMonitoringHistory(service, ownerName, now);
        }
    }

    @Override
    public void disableMonitoring(final String serviceId) {
        Services service = servicesRepository.findById(UUID.fromString(serviceId)).orElseThrow(
                () -> new CustomException(ResponseCode.DFM1000, "service not found")
        );

        service.setMonitoring(false);

        servicesRepository.save(service);
    }

    @Override
    public void enableMonitoring(final String serviceId, Integer period) {
        Services services = servicesRepository.findById(UUID.fromString(serviceId)).orElseThrow(
                () -> new CustomException(ResponseCode.DFM1000, "service not found")
        );

        services.setMonitoring(true);
        services.setMonitoringPeriod(period);

        servicesRepository.save(services);
    }

    // slow query, cpu / memory, request 통계 내용 저장
    private void saveMonitoringHistory(Services service, String ownerName, Long now) {
        final QueryStatisticsVo averageQueryOutcome = getAverageQueryOutcome(service.getServiceID().toString());
        final List<SlowQueryVo> slowQueries = getSlowQueries(service.getServiceID().toString());
        final CpuUsedVo cpuSpentTime = getCpuSpentTime(service.getServiceID().toString());
        final MemoryUsedVo memoryUsage = getMemoryUsage(service.getServiceID().toString());

        // cpu, memory, request 내용 save
        monitoringHistoryRepository.save(
                MonitoringHistory.builder()
                        .serviceId(service.getServiceID())
                        .ownerName(ownerName)
                        .createdAt(now)
                        .cpuUsed(cpuSpentTime == null ? null : cpuSpentTime.getCpuUsed())
                        .memoryUsed(memoryUsage == null ? null : memoryUsage.getMemoryUsed())
                        .successRequest(averageQueryOutcome == null ? null : Long.valueOf(averageQueryOutcome.getSuccessQueries()))
                        .failedRequest(averageQueryOutcome == null ? null : Long.valueOf(averageQueryOutcome.getFailedQueries()))
                        .build()
        );

        // slow query 내용 저장
        if (slowQueries.isEmpty()) {
            log.debug("Insufficient privileges");
        } else {
            for (SlowQueryVo slowQuery : slowQueries) {
                slowQueryStatisticRepository.save(
                        SlowQueryStatistic.builder()
                                .serviceId(service.getServiceID())
                                .query(slowQuery.getSqlText())
                                .totalCount(slowQuery.getTotalCount())
                                .averageExecutedTime(slowQuery.getAvgExecTime())
                                .createdAt(now)
                                .build()
                );
            }
        }
    }

    private HostPortVo convertStringToHostPortVo(GetDatabasesResponseDto serviceInfo) {
        String host;
        int port;

        // 192.168.109.254:32355, mariadb-svc:3306
        if (serviceInfo.getConnection().getHostPort().contains(":")) {
            String[] parts = serviceInfo.getConnection().getHostPort().split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        } else { // mariadb-svc
            host = serviceInfo.getConnection().getHostPort();
            port = DatabasePort.getPortFromHost(serviceInfo.getServiceType());
        }

        // 영어 포함
        // 영어로 된 이름일 경우 k8s service라고 간주
        if (host.matches(".*[a-zA-Z].*")) {
            final Integer nodePort = k8sService.getNodePort(host, port);

            host = hostIP;
            port = nodePort;
        }

        return HostPortVo.builder()
                .host(host)
                .port(port)
                .build();
    }

    private String getDatabaseName(GetDatabasesResponseDto serviceInfo) {
        if (serviceInfo.getServiceType().equalsIgnoreCase(ORACLE.getName())) {
            return serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema() == null ?
                    serviceInfo.getConnection().getOracleConnectionType().getOracleServiceName() : serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema();
        } else {
            return serviceInfo.getConnection().getDatabaseName() != null ?
                    serviceInfo.getConnection().getDatabaseName() : serviceInfo.getConnection().getDatabase();
        }
    }
}