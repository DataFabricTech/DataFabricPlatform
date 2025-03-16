package com.mobigen.monitoring.service.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobigen.monitoring.domain.ConnectionDao;
import com.mobigen.monitoring.domain.ConnectionHistory;
import com.mobigen.monitoring.domain.ModelRegistration;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.dto.response.fabric.GetDatabasesResponseDto;
import com.mobigen.monitoring.dto.response.fabric.GetObjectStorageResponseDto;
import com.mobigen.monitoring.dto.response.fabric.ObjectStorageConnectionInfo;
import com.mobigen.monitoring.enums.ConnectionStatus;
import com.mobigen.monitoring.enums.DatabasePort;
import com.mobigen.monitoring.enums.DatabaseType;
import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.service.ConnectionService;
import com.mobigen.monitoring.service.k8s.K8SService;
import com.mobigen.monitoring.service.openMetadata.OpenMetadataService;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.service.scheduler.DatabaseConnectionInfo;
import com.mobigen.monitoring.utils.UnixTimeUtil;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import static com.mobigen.monitoring.enums.Common.*;
import static com.mobigen.monitoring.enums.ConnectionStatus.*;
import static com.mobigen.monitoring.enums.DatabaseType.*;
import static com.mobigen.monitoring.enums.OpenMetadataEnums.*;
import static com.mobigen.monitoring.enums.OpenMetadataEnums.CONFIG;
import static com.mobigen.monitoring.vo.Queries.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseManagementServiceImpl implements DatabaseManagementService {
    private final OpenMetadataService openMetadataService;
    private final ServicesService servicesService;
    private final ModelRegistrationService modelRegistrationService;
    private final Utils utils = new Utils();
    private final ConnectionService connectionService;
    private final ObjectMapper objectMapper;
    private final K8SService k8sService;

    // database service 정보
    private Map<String, GetDatabasesResponseDto> databaseServices = new HashMap<>();

    // storage service 정보
    private Map<String, GetObjectStorageResponseDto> storageServices = new HashMap<>();

    // table 정보 및 table 개수
    private Map<String, TableModelInfo> tableModels = new HashMap<>();

    private Map<String, ModelInfoVo> serviceModels = new HashMap<>();

    // table 변경 이력
    private Map<String, TableAuditInfo> tableAuditInfos = new HashMap<>();

    @Value("${k8s.ip}")
    private String hostIP;

    @PostConstruct
    public void init() {
        final List<Services> allService = servicesService.getAllService();

//        initializeServiceTable(SCHEDULER.getName());
        if (allService.isEmpty()) {
            initializeServiceTable(SCHEDULER.getName());
        } else {
            getServiceListFromFabricServer();
        }

        log.info("DatabaseManagementServiceImpl init: {}", serviceModels.size());
    }

    @Override
    public Boolean checkDatabaseConnection(DatabaseConnectionRequest request) {
        if (!isDatabaseService(request.getDbType())) {
            return testMinioConnection(request);
        }

        DataSource dataSource = createDataSource(request);

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);  // 2초 내에 응답 확인
        } catch (SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
            return false;  // 연결 실패
        }
    }

    @Override
    public DataSource createDataSource(DatabaseConnectionRequest request) {
        HikariDataSource dataSource = new HikariDataSource();

        String jdbcUrl = buildJdbcUrl(request);

        log.info("jdbcUrl: {}", jdbcUrl);

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
            return "jdbc:oracle:thin:@" + request.getHost() + ":" + request.getPort() + "/" + request.getDatabaseName();
        } else if (request.getDbType().equalsIgnoreCase(POSTGRES.getName())) {
            return "jdbc:postgresql://" + request.getHost() + ":" + request.getPort() + "/" + request.getDatabaseName();
        } else if (request.getDbType().equalsIgnoreCase(MARIADB.getName()) || request.getDbType().equalsIgnoreCase(MYSQL.getName())) {
            return "jdbc:" + request.getDbType().toLowerCase() + "://" + request.getHost() + ":" + request.getPort();
        } else {
            throw new CustomException("Unsupported database type: " + request.getDbType());
        }
    }

    /**
     * TODO database type 추가
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
            log.error("Unsupported database type: " + request.getDbType());
        }

        return databases;
    }

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

    @Transactional
    @Override
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
            log.info("query: {}", entry);
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

            conn.commit(); // 필요하면 유지
        } catch (SQLException e) {
            try {
                conn.rollback(); // 오류 발생 시 롤백
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource); // ConnectionDao 반환
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
        log.info("query: {}", query);
        DataSource dataSource = createDataSource(request);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        return jdbcTemplate.queryForList(query);
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
    public Boolean testMinioConnection(DatabaseConnectionRequest request) {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://" + request.getHost() + ":" + request.getPort())
                    .credentials(request.getUsername(), request.getPassword())
                    .build();

            minioClient.listBuckets();  // Minio 연결 테스트
            return true;
        } catch (MinioException | IllegalArgumentException e) {
            System.err.println("Minio 연결 실패: " + e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initializeServiceTable(String userName) {
        // service 등록
        // Create Service entity
        final List<Services> services = saveServicesToDatabase();

        // connection 등록
        saveConnections(services);

        // model registration 등록
        setServiceModels(getServiceListFromFabricServer());

//        // connectionCheck & get Tables or Files
//        for (var currentService : currentServices) {
//            var param = String.format("?q=%s&index=%s&from=0&size=0&deleted=false" +
//                            "&query_filter={\"query\":{\"bool\":{}}}", currentService.get(NAME.getName()).asText(),
//                    currentService.get(SERVICE_TYPE.getName()).asText().equalsIgnoreCase("s3") ||
//                            currentService.get(SERVICE_TYPE.getName()).asText().equalsIgnoreCase("minio") ?
//                            "container_search_index" : "table_search_index");
//            var omDBItems = openMetadataService.getQuery(param).get("hits").get("total").get("value").asInt();
//            connectionService.getDBItems(currentService, omDBItems, userName);
//        }
    }

    @Override
    public void saveConnections(final List<Services> services) {
        // response time == 직접 디비에서 show databases; 햔 시간
        // executed_at, execute_by, query_execution_time, service_id
        List<ConnectionDao> connections = new ArrayList<>();
        List<ConnectionHistory> connectionHistories = new ArrayList<>();

        for (Services service : services) {
            // check response time
            final CheckConnectionResponseVo checkDatabaseResponse = getQueryExecutedTime(service);

            // connection entity 생성
            connections.add(
                    ConnectionDao.builder()
                            .executeAt(UnixTimeUtil.getCurrentMillis())
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
                            .updatedAt(UnixTimeUtil.getCurrentMillis())
                            .build()
            );

            if (service.getServiceType().equalsIgnoreCase("mysql") || service.getServiceType().equalsIgnoreCase("mariadb")
                    || service.getServiceType().equalsIgnoreCase("postgresql")) {
                modelRegistrationService.save(
                        ModelRegistration.builder()
                                .serviceId(service.getServiceID())
                                .updatedAt(UnixTimeUtil.getCurrentMillis())
                                .omModelCount(getModelCountFromOM(service.getServiceID()).getTotal())
                                .modelCount(getModelCount(service))
                                .build()
                );
            }
        }

        log.info("connections: {}", connections);

        connectionService.saveAllConnection(connections);
        connectionService.saveAllConnectionHistory(connectionHistories);

        for (Services service : services) {
            final Services services1 = servicesService.saveService(service);

            log.info("update status: {}", services1.getServiceID());
        }
    }

    @Override
    public TableModelInfo getModelCountFromOM(final UUID serviceID) {
        final List<JsonNode> serviceListFromFabricServer = getServiceListFromFabricServer();

        String fullyQualifiedName = databaseServices.get(serviceID.toString()).getFullyQualifiedName();

        final JsonNode tableModels = openMetadataService.getTableModels(fullyQualifiedName);

        final TableModelInfo tableModelInfo = objectMapper.convertValue(tableModels, TableModelInfo.class);

        this.tableModels.put(serviceID.toString(), tableModelInfo);

        return tableModelInfo;
    }

    @Override
    public Integer getModelCount(Services service) {
        // table 개수 세기
        if (isRDBMS(service.getServiceType())) {
            final String serviceId = service.getServiceID().toString();
            final GetDatabasesResponseDto getDatabasesResponseDto = databaseServices.get(serviceId);
            final HostPortVo hostPortVo = convertStringToHostPortVo(getDatabasesResponseDto);

            DatabaseConnectionRequest request = DatabaseConnectionRequest.builder()
                    .username(getDatabasesResponseDto.getConnection().getUsername())
                    .dbType(service.getServiceType())
                    .host(hostPortVo.getHost())
                    .port(hostPortVo.getPort())
                    .databaseName(getDatabasesResponseDto.getConnection().getDatabaseName())
                    .password(getDatabasesResponseDto.getConnection().getPassword() == null ?
                            getDatabasesResponseDto.getConnection().getAuthType().getPassword()
                            : getDatabasesResponseDto.getConnection().getPassword())
                    .build();

            final Map<String, List<String>> tables = getTables(request);

            return tables.values().stream().mapToInt(List::size).sum();
        } else {
            // minio 의 버킷 수 세기
            return 0;
        }
    }

    @Override
    public CheckConnectionResponseVo getQueryExecutedTime(final Services service) {
        // measure time
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ConnectionStatus status;

        // trino 때문
        if (fromString(service.getServiceType()) == null) {
            return CheckConnectionResponseVo.builder()
                    .status(NOT_TARGET)
                    .responseTime(stopWatch.getTotalTimeMillis())
                    .build();
        }

        final DatabaseConnectionRequest request = getDatabaseConnectionRequest(service.getServiceID().toString());

        // db connection check
        final Boolean connectionCheckResponse = checkDatabaseConnection(request);

        // connection check failed
        if (connectionCheckResponse == null) {
            log.error("[ MONITORING ] DB connection check pass");

            return CheckConnectionResponseVo.builder()
                    .status(NOT_TARGET)
                    .responseTime(stopWatch.getTotalTimeMillis())
                    .build();
        } else if (!connectionCheckResponse) {
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

        // 실행 시간은 ms
        return CheckConnectionResponseVo.builder()
                .status(status)
                .responseTime(stopWatch.getTotalTimeMillis())
                .build();
    }

    @Override
    public List<Services> saveServicesToDatabase() {
        List<JsonNode> currentServices = getServiceListFromFabricServer();

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

        return servicesService.saveServices(services);
    }

    @Override
    public void setDatabaseServices(JsonNode databaseServices) {
        List<JsonNode> data = new ArrayList<>();

        if (databaseServices != null && databaseServices.isArray()) {  // 배열인지 확인
            for (JsonNode node : databaseServices) {
                data.add(node);
            }
        }

        for (JsonNode databaseService : data) {
            try {
                GetDatabasesResponseDto value = GetDatabasesResponseDto.builder()
                        .id(databaseService.get(ID.getName()).asText())
                        .name(databaseService.get(NAME.getName()).asText())
                        .description(databaseService.get(DESCRIPTION.getName()).asText())
                        .fullyQualifiedName(databaseService.get(FULLY_QUALIFIED_NAME.getName()).asText())
                        .updatedAt(Long.valueOf(databaseService.get(UPDATED_AT.getName()).toString()))
                        .updatedBy(databaseService.get(UPDATED_BY.getName()).asText())
                        .serviceType(databaseService.get(SERVICE_TYPE.getName()).asText())
                        .connection(objectMapper.readValue(databaseService.get(CONNECTION.getName()).get(CONFIG.getName()).toString(), DatabaseConnectionInfo.class))
                        .password(databaseService.get(PASSWORD.getName()) == null ? null : databaseService.get(PASSWORD.getName()).asText())
                        .deleted(databaseService.get(DELETED.getName()).asBoolean())
                        .build();

                this.databaseServices.put(
                        databaseService.get(ID.getName()).toString().replace("\"", ""),
                        value
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setStorageServiceList(JsonNode storageServices) {
        List<JsonNode> data = new ArrayList<>();

        if (storageServices != null && storageServices.isArray()) {  // 배열인지 확인
            for (JsonNode node : storageServices) {
                data.add(node);
            }
        }

        for (JsonNode storageService : data) {
            try {
                this.storageServices.put(
                        storageService.get(ID.getName()).asText(),
                        GetObjectStorageResponseDto.builder()
                                .id(storageService.get(ID.getName()).asText())
                                .name(storageService.get(NAME.getName()).asText())
                                .description(storageService.get(DESCRIPTION.getName()).asText())
                                .fullyQualifiedName(storageService.get(NAME.getName()).asText())
                                .updatedAt(Long.valueOf(storageService.get(UPDATED_AT.getName()).asText()))
                                .updatedBy(storageService.get(UPDATED_BY.getName()).asText())
                                .serviceType(storageService.get(SERVICE_TYPE.getName()).asText())
                                .deleted(storageService.get(DELETED.getName()).asBoolean())
                                .connection(objectMapper.readValue(storageService.get(CONNECTION.getName()).get(CONFIG.getName()).toString(), ObjectStorageConnectionInfo.class))
                                .build()
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * service 를 인메모리에 캐싱
     */
    @Override
    public List<JsonNode> getServiceListFromFabricServer() {
        JsonNode databaseServices = openMetadataService.getDatabaseServices();
        JsonNode storageServices = openMetadataService.getStorageServices();

        List<JsonNode> currentServices = new ArrayList<>();

        // merge list
        databaseServices.forEach(currentServices::add);
        storageServices.forEach(currentServices::add);

        // service 저장
        setDatabaseServices(databaseServices);
        setStorageServiceList(storageServices);

        // model 개수 저장
        setServiceModels(currentServices);

        return currentServices;
    }

    @Override
    public void setServiceModels(final List<JsonNode> currentServices) {
        // table 개수 세기
        // om의 model 개수 세기
        for (JsonNode currentService : currentServices) {
            final String serviceType = currentService.get(SERVICE_TYPE.getName()).asText();
            JsonNode models = null;

            // RDB 데이터베이스일 경우
            if (isDatabaseService(serviceType)) {
                models = openMetadataService.getTableModels(
                        currentService.get(
                                FULLY_QUALIFIED_NAME.getName()
                        ).asText()
                );
            } else {
                models = openMetadataService.getStorageModels(
                        currentService.get(
                                NAME.getName()
                        ).asText()
                );
            }

            final Long total = Long.valueOf(models.get("total").asText());

            serviceModels.put(
                    currentService.get(ID.getName()).asText(),
                    ModelInfoVo.builder()
                            .total(total)
                            .updatedAt(UnixTimeUtil.getCurrentMillis())
                            .build()
            );
        }
    }

    @Override
    public DatabaseConnectionRequest getDatabaseConnectionRequest(String serviceId) {
        final GetDatabasesResponseDto serviceInfo = databaseServices.get(serviceId);
        final HostPortVo hostPortVo = convertStringToHostPortVo(serviceInfo);
        String databaseName;

        if (serviceInfo.getServiceType().equals(ORACLE.getName())) {
            if (serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema() != null) {
                databaseName = serviceInfo.getConnection().getOracleConnectionType().getDatabaseSchema();
            } else {
                databaseName = serviceInfo.getConnection().getOracleConnectionType().getOracleServiceName();
            }
        } else {
            databaseName = serviceInfo.getConnection().getDatabaseName() == null ?
                    serviceInfo.getConnection().getDatabase() : serviceInfo.getConnection().getDatabaseName();
        }

        return DatabaseConnectionRequest.builder()
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

    private HostPortVo convertStringToHostPortVo(GetDatabasesResponseDto serviceInfo) {
        String host;
        int port;

        if (serviceInfo.getConnection().getHostPort().contains(":")) {
            String[] parts = serviceInfo.getConnection().getHostPort().split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            host = serviceInfo.getConnection().getHostPort();
            port = DatabasePort.getPortFromHost(serviceInfo.getServiceType());
        }

        // 영어 포함
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

    public Map<String, String> checkTableChange() {
        // table 변경 정보 in-memory 로 들고 있기
        // 없으면 DB 에서 가져오기
        // data 바꼈는지 체크
        if (tableAuditInfos.isEmpty()) {
            // table 변경 이력 가져오기
            // postgres, oracle, mariadb / mysql
        } else {

        }
        return null;
    }
}