package com.mobigen.vdap.server.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.api.services.CreateStorageService;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.security.credentials.BasicAuth;
import com.mobigen.vdap.schema.security.credentials.MinIOCredentials;
import com.mobigen.vdap.schema.security.ssl.SSLMode;
import com.mobigen.vdap.schema.services.connections.database.*;
import com.mobigen.vdap.schema.services.connections.database.common.basicAuth;
import com.mobigen.vdap.schema.services.connections.storage.MinIOConnection;
import com.mobigen.vdap.schema.type.*;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.configurations.AuthConfig;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import com.mobigen.vdap.server.entity.StorageServiceEntity;
import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.relationship.RelationshipRepository;
import com.mobigen.vdap.server.relationship.RelationshipService;
import com.mobigen.vdap.server.relationship.TagUsageRepository;
import com.mobigen.vdap.server.relationship.TagUsageService;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.ClassificationRepository;
import com.mobigen.vdap.server.tags.TagRepository;
import com.mobigen.vdap.server.users.KeyCloakUtil;
import com.mobigen.vdap.server.users.UserService;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageServiceRepository storageServiceRepository;

    @Autowired
    private ClassificationRepository classificationRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagUsageRepository tagUsageRepository;
    @Autowired
    private TagUsageService tagUsageService;
    @Autowired
    private RelationshipRepository relationshipRepository;
    @Autowired
    private RelationshipService relationshipService;
    @Autowired
    private EntityExtensionRepository entityExtensionRepository;
    @Autowired
    private UserService userService;

    private static KeyCloakUtil keyCloakUtil;

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withReuse(true)
            .withUsername("root")
            .withPassword("password")
            .withDatabaseName("open_vdap");

    @Container
    public static GenericContainer<?> keycloakContainer = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.0.7"))
            .withExposedPorts(8080)
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "Mobigen.07$")
            .withCommand("start-dev");

    @BeforeAll
    static void beforeAll() {
        // Flyway 마이그레이션 실행
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .load();
        flyway.migrate();
        log.info("flyway migration finish");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // start mysql container
        mysql.start();
        // start Keycloak container
        keycloakContainer.start();
        // wait for Keycloak to be ready
        keycloakContainer.waitingFor(Wait.forHttp("/").forStatusCode(200));

        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Set the Keycloak URL in the registry
        String keycloakUrl = "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080);
        registry.add("auth.url", () -> keycloakUrl);
        registry.add("auth.realm", () -> "ovp");
        registry.add("auth.admin", () -> "admin");
        registry.add("auth.password", () -> "Mobigen.07$");

        // Initialize Keycloak. Create Realm And User For Test
        AuthConfig config = new AuthConfig();
        config.setUrl(keycloakUrl);
        config.setRealm("ovp");
        config.setAdmin("admin");
        config.setPassword("Mobigen.07$");
        keyCloakUtil = new KeyCloakUtil();
        keyCloakUtil.initKeyCloak(config);
    }


    @AfterAll
    static void afterAll() {
        keyCloakUtil.cleanup();
        keycloakContainer.stop();
        mysql.stop();
    }

    @BeforeEach
    void beforeEach() {
        storageServiceRepository.deleteAll();
        classificationRepository.deleteAll();
        tagRepository.deleteAll();
        tagUsageRepository.deleteAll();
        relationshipRepository.deleteAll();
        entityExtensionRepository.deleteAll();
    }

    @Test
    @Order(1)
    void create() throws Exception {
        // classification setup
        Classification classification = createClassification("test_classification", "test_classification", "test classification", false);
        // tag setup
        Tag tag001 = createTag(classification, "test_tag_001", "test_tag_001", "test tag 001");
        Tag tag002 = createTag(classification, "test_tag_002", "test_tag_002", "test tag 002");

        // TODO : Glossary
        // TODO : Glossary Terms

        CreateStorageService create001 = new CreateStorageService();
        create001.setName("mysql_test_001");
        create001.setDisplayName("mysql_test_001");
        create001.setDescription("for mysql test");
        create001.setKindOfService(ServiceType.DATABASE);
        create001.setServiceType(StorageService.StorageServiceType.Mysql);

        MysqlConnection connection = new MysqlConnection();
        connection.setHostPort("localhost:3306");
        connection.setUsername("username");
        connection.setAuthType(new basicAuth().withPassword("password"));
        connection.setDatabaseName("test");
        create001.setConnection(new StorageConnection().withConfig(connection));

        TagLabel tagLabel001 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification.getId())
                .withId(tag001.getId())
                .withName(tag001.getName())
                .withDisplayName(tag001.getDisplayName())
                .withDescription(tag001.getDescription());
        TagLabel tagLabel002 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification.getId())
                .withId(tag002.getId())
                .withName(tag002.getName())
                .withDisplayName(tag002.getDisplayName())
                .withDescription(tag002.getDescription());

        create001.setTags(List.of(tagLabel001, tagLabel002));

        List<EntityReference> owners = new ArrayList<>();
        EntityReference admin = new EntityReference();
        admin.setId(UUID.fromString(keyCloakUtil.getUserList().get("admin")));
        admin.setType(Entity.USER);
        admin.setName("admin");
        owners.add(admin);

        EntityReference jblim = new EntityReference();
        jblim.setId(UUID.fromString(keyCloakUtil.getUserList().get("jblim")));
        jblim.setType(Entity.USER);
        jblim.setName("jblim");
        owners.add(jblim);

        create001.setOwners(owners);

        MvcResult result = mockMvc.perform(post("/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(create001)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        StorageService service = JsonUtils.convertValue(node.get("data"), StorageService.class);

        Assertions.assertNotNull(service.getId());
        Assertions.assertEquals(create001.getName(), service.getName());
        Assertions.assertEquals(create001.getDescription(), service.getDescription());
        Assertions.assertEquals(create001.getDisplayName(), service.getDisplayName());
        Assertions.assertEquals(create001.getKindOfService(), service.getKindOfService());
        Assertions.assertEquals(create001.getServiceType(), service.getServiceType());
        Assertions.assertNotNull(service.getConnection());
        Assertions.assertNotNull(service.getUpdatedAt());
        Assertions.assertNotNull(service.getUpdatedBy());
        Assertions.assertNotNull(service.getHref());

        Assertions.assertNull(service.getPipelines());
        Assertions.assertNull(service.getTestConnectionResult());

        // Storage Check
        StorageServiceEntity entity = storageServiceRepository.findById(service.getId().toString()).orElse(null);
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(service.getId().toString(), entity.getId());
        Assertions.assertEquals(service.getName(), entity.getName());
        Assertions.assertEquals(service.getKindOfService().toString(), entity.getKind());
        Assertions.assertEquals(service.getServiceType().toString(), entity.getServiceType());
        StorageService entityData = JsonUtils.readValue(entity.getJson(), StorageService.class);

        Assertions.assertEquals(service.getName(), entityData.getName());
        Assertions.assertEquals(service.getDescription(), entityData.getDescription());
        Assertions.assertEquals(service.getDisplayName(), entityData.getDisplayName());
        Assertions.assertEquals(service.getKindOfService(), entityData.getKindOfService());
        Assertions.assertEquals(service.getServiceType(), entityData.getServiceType());
        Assertions.assertNotNull(entityData.getConnection());

        // Object to MySqlConnection
        MysqlConnection entityConnection = JsonUtils.convertValue(entityData.getConnection().getConfig(), MysqlConnection.class);
        Assertions.assertEquals(connection.getHostPort(), entityConnection.getHostPort());
        Assertions.assertEquals(connection.getUsername(), entityConnection.getUsername());
        Assertions.assertEquals(connection.getDatabaseName(), entityConnection.getDatabaseName());
        BasicAuth auth = JsonUtils.convertValue(connection.getAuthType(), BasicAuth.class);
        BasicAuth entityAuth = JsonUtils.convertValue(entityConnection.getAuthType(), BasicAuth.class);
        Assertions.assertNotEquals(auth.getPassword(), entityAuth.getPassword());
        Assertions.assertEquals(auth.getPassword(), SecretsManager.decrypt(entityAuth.getPassword()));

        // Relationship check
        List<RelationshipEntity> relationships = relationshipService.getRelationships(null, service.getId(), null, Entity.STORAGE_SERVICE, Relationship.OWNS, Include.NON_DELETED);
        Assertions.assertEquals(2, relationships.size());
        relationships.forEach(relationship -> {
            log.info("relationship: {}", relationship);
            Assertions.assertEquals(service.getId().toString(), relationship.getToId());
            Assertions.assertEquals(Entity.STORAGE_SERVICE, relationship.getToEntity());
            Assertions.assertEquals(Entity.USER, relationship.getFromEntity());
            if( relationship.getFromId().equals(admin.getId().toString())) {
                EntityReference user = userService.getReferenceById(UUID.fromString(relationship.getFromId()));
                Assertions.assertNotNull(user);
                Assertions.assertEquals(admin.getName(), user.getName());
            } else {
                EntityReference user = userService.getReferenceById(UUID.fromString(relationship.getFromId()));
                Assertions.assertNotNull(user);
                Assertions.assertEquals(jblim.getName(), user.getName());
            }
        });

        // TagUsage check
        List<TagUsageEntity> tagUsageEntities = tagUsageService.getTagUsages(null, null, null, Entity.STORAGE_SERVICE, service.getId().toString());
        Assertions.assertEquals(2, tagUsageEntities.size());
        tagUsageEntities.forEach(tagUsage -> {
            if( !tag001.getId().toString().equals(tagUsage.getTagId()) && !tag002.getId().toString().equals(tagUsage.getTagId())) {
                Assertions.fail("tagUsage tagId is not matched");
            }
            Assertions.assertEquals(TagLabel.TagSource.CLASSIFICATION.ordinal(), tagUsage.getSource());
            Assertions.assertEquals(classification.getId().toString(), tagUsage.getSourceId());
        });

        // TODO : Check Glossary
        // TODO : Check Glossary Terms
    }

    @Test
    @Order(2)
    void errCreateTagExclusive() throws Exception {
        Classification classification001 = createClassification("test_classification_001", "test_classification_001", "test classification", true);
        Tag tag001 = createTag(classification001, "test_tag_001", "test_tag_001", "test tag 001");
        Tag tag002 = createTag(classification001, "test_tag_002", "test_tag_002", "test tag 002");

        CreateStorageService create001 = new CreateStorageService();
        create001.setName("mysql_test_001");
        create001.setDisplayName("mysql_test_001");
        create001.setDescription("for mysql test");
        create001.setKindOfService(ServiceType.DATABASE);
        create001.setServiceType(StorageService.StorageServiceType.Mysql);
        MysqlConnection connection = new MysqlConnection();
        connection.setHostPort("localhost:3306");
        connection.setUsername("username");
        connection.setAuthType(new basicAuth().withPassword("password"));
        connection.setDatabaseName("test");
        create001.setConnection(new StorageConnection().withConfig(connection));
        TagLabel tagLabel001 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification001.getId())
                .withId(tag001.getId())
                .withName(tag001.getName())
                .withDisplayName(tag001.getDisplayName())
                .withDescription(tag001.getDescription());
        TagLabel tagLabel002 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification001.getId())
                .withId(tag002.getId())
                .withName(tag002.getName())
                .withDisplayName(tag002.getDisplayName())
                .withDescription(tag002.getDescription());
        create001.setTags(List.of(tagLabel001, tagLabel002));

        MvcResult result = mockMvc.perform(post("/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(create001)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());
        log.info("errCreateTagExclusive: {}", node.get("errorMsg").asText());
    }

    @Test
    @Order(3)
    void errCreateValidTag() throws Exception {
        Classification classification001 = createClassification("test_classification_001", "test_classification_001", "test classification", true);
        Tag tag001 = createTag(classification001, "test_tag_001", "test_tag_001", "test tag 001");

        CreateStorageService create001 = new CreateStorageService();
        create001.setName("mysql_test_001");
        create001.setDisplayName("mysql_test_001");
        create001.setDescription("for mysql test");
        create001.setKindOfService(ServiceType.DATABASE);
        create001.setServiceType(StorageService.StorageServiceType.Mysql);
        MysqlConnection connection = new MysqlConnection();
        connection.setHostPort("localhost:3306");
        connection.setUsername("username");
        connection.setAuthType(new basicAuth().withPassword("password"));
        connection.setDatabaseName("test");
        create001.setConnection(new StorageConnection().withConfig(connection));
        TagLabel tagLabel001 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification001.getId())
                .withId(tag001.getId())
                .withName(tag001.getName())
                .withDisplayName(tag001.getDisplayName())
                .withDescription(tag001.getDescription());
        TagLabel tagLabel002 = new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(classification001.getId())
                .withId(Utilities.generateUUID())
                .withName("error_tag")
                .withDisplayName("error_tag_tag")
                .withDescription("error_tag");
        create001.setTags(List.of(tagLabel001, tagLabel002));

        MvcResult result = mockMvc.perform(post("/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(create001)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());
    }

    @Test
    @Order(4)
    void createAndFind() throws Exception {
        Classification classification001 = createClassification("test_classification_001", "test_classification_001", "test classification", false);
        Tag tag001 = createTag(classification001, "test_tag_003", "test_tag_003", "test tag 003");
        Tag tag002 = createTag(classification001, "test_tag_004", "test_tag_004", "test tag 004");
        Classification classification002 = createClassification("test_classification", "test_classification", "test classification", false);
        Tag tag003 = createTag(classification002, "test_tag_001", "test_tag_001", "test tag 001");
        Tag tag004 = createTag(classification002, "test_tag_002", "test_tag_002", "test tag 002");

        // name list
        ArrayList<String> nameList = new ArrayList<>();

        // 1. MySQL
        Map<Classification, List<Tag>> tags001 = Map.of(
                classification001, List.of(tag001)
        );
        List<String> owners001 = List.of("admin", "jblim");
        StorageService mysql = createStorageService("mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags001, owners001);
        nameList.add("mysql");
        chkStorageService(mysql, "mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags001, owners001);
        // 2. Postgres
        Map<Classification, List<Tag>> tags002 = Map.of(
                classification001, List.of(tag002),
                classification002, List.of(tag003)
        );
        StorageService postgres = createStorageService("postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, tags002, null);
        nameList.add("postgres");
        chkStorageService(postgres, "postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, tags002, null);
        // 3. MariaDB
        StorageService mariadb = createStorageService("mariadb", "mariadb_display", "mariadb description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, null, null);
        nameList.add("mariadb");
        chkStorageService(mariadb, "mariadb", "mariadb_display", "mariadb description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, null, null);
        // 4. MinIO
        Map<Classification, List<Tag>> tags004 = Map.of(
                classification002, List.of(tag004)
        );
        List<String> owners004 = List.of("admin");
        StorageService minio = createStorageService("minio", "minio_display", "minio description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, tags004, owners004);
        nameList.add("minio");
        chkStorageService(minio, "minio", "minio_display", "minio description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, tags004, owners004);
        // 5. MongoDB
        List<String> owners005 = List.of("admin", "jblim");
        StorageService mongo = createStorageService("mongo", "mongo_display", "mongo description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, null);
        nameList.add("mongo");
        chkStorageService(mongo, "mongo", "mongo_display", "mongo description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, owners005);
        // 6. OracleDB
        Map<Classification, List<Tag>> tags006 = Map.of(
                classification001, List.of(tag002),
                classification002, List.of(tag003)
        );
        List<String> owners006 = List.of("jblim");
        StorageService oracle = createStorageService("oracle", "oracle_display", "oracle description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, tags006, owners006);
        nameList.add("oracle");
        chkStorageService(oracle, "oracle", "oracle_display", "oracle description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, tags006, owners006);

        // 7. Mssql
        StorageService mssql = createStorageService("mssql", "mssql_display", "mssql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mssql, null, null);
        nameList.add("mssql");
        chkStorageService(mssql, "mssql", "mssql_display", "mssql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mssql, null, null);

        // 8. Hive
        List<String> owners008 = List.of("jblim");
        StorageService hive = createStorageService("hive", "hive_display", "hive description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Hive, null, owners008);
        nameList.add("hive");
        chkStorageService(hive, "hive", "hive_display", "hive description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Hive, null, owners008);

        // sort
        Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);

        // List Test
        MvcResult result = mockMvc.perform(get("/v1/services")
                        .param("kind_of_service", ServiceType.DATABASE.toString())
                        .param("service_type", StorageService.StorageServiceType.Mysql.toString())
                        .param("fields", "owners,tags")
                        .param("page", "0")
                        .param("size", "10")
                        .param("include", "non-deleted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        PageModel<StorageService> pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<StorageService>>() {
                });
        Assertions.assertEquals(1, pageResponse.getTotalElements());
        Assertions.assertEquals(1, pageResponse.getTotalPages());
        Assertions.assertNotNull(pageResponse.getContents());

        List<StorageService> contents = pageResponse.getContents();
        // Compare with mysql
        StorageService mysqlService = contents.getFirst();
        Assertions.assertEquals(mysql.getId().toString(), mysqlService.getId().toString());
        Assertions.assertEquals(mysql.getName(), mysqlService.getName());
        Assertions.assertEquals(mysql.getDisplayName(), mysqlService.getDisplayName());
        Assertions.assertEquals(mysql.getDescription(), mysqlService.getDescription());
        Assertions.assertEquals(mysql.getKindOfService(), mysqlService.getKindOfService());
        Assertions.assertEquals(mysql.getServiceType(), mysqlService.getServiceType());
        Assertions.assertEquals(mysql.getConnection().getConfig(), mysqlService.getConnection().getConfig());
        Assertions.assertEquals(mysql.getTags().size(), mysqlService.getTags().size());
        Assertions.assertEquals(mysql.getOwners().size(), mysqlService.getOwners().size());

        // Page And Tag Field Check
        result = mockMvc.perform(get("/v1/services")
                        .param("fields", "tags")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<StorageService>>() {
                });
        Assertions.assertEquals(8, pageResponse.getTotalElements());
        Assertions.assertEquals(4, pageResponse.getTotalPages());
        Assertions.assertNotNull(pageResponse.getContents());

        contents = pageResponse.getContents();
        for( int i = 0; i < contents.size(); i++ ) {
            StorageService storageService = contents.get(i);
            Assertions.assertTrue(storageService.getOwners() == null || storageService.getOwners().isEmpty());
            Assertions.assertEquals(nameList.get(i), storageService.getName());
        }

        // owner
        result = mockMvc.perform(get("/v1/services")
                        .param("fields", "owners")
                        .param("page", "1")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<StorageService>>() {
                });
        Assertions.assertEquals(8, pageResponse.getTotalElements());
        Assertions.assertEquals(4, pageResponse.getTotalPages());
        Assertions.assertNotNull(pageResponse.getContents());

        contents = pageResponse.getContents();
        for( int i = 0; i < contents.size(); i++ ) {
            StorageService storageService = contents.get(i);
            Assertions.assertTrue(storageService.getTags() == null || storageService.getTags().isEmpty());
            Assertions.assertEquals(nameList.get(i+2), storageService.getName());
        }

        // getbyname
        result = mockMvc.perform(get("/v1/services/name/mysql")
                        .param("fields", "owners,tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService resMysql = JsonUtils.convertValue(node.get("data"), StorageService.class);
        Assertions.assertEquals(mysql.getId().toString(), resMysql.getId().toString());
        Assertions.assertEquals(mysql.getName(), resMysql.getName());
        Assertions.assertEquals(mysql.getDisplayName(), resMysql.getDisplayName());
        Assertions.assertEquals(mysql.getDescription(), resMysql.getDescription());
        Assertions.assertEquals(mysql.getKindOfService(), resMysql.getKindOfService());
        Assertions.assertEquals(mysql.getServiceType(), resMysql.getServiceType());

        // getbyid
        result = mockMvc.perform(get("/v1/services/" + mysql.getId())
                        .param("fields", "owners,tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resMysql = JsonUtils.convertValue(node.get("data"), StorageService.class);
        Assertions.assertEquals(mysql.getId().toString(), resMysql.getId().toString());
        Assertions.assertEquals(mysql.getName(), resMysql.getName());
        Assertions.assertEquals(mysql.getDisplayName(), resMysql.getDisplayName());
        Assertions.assertEquals(mysql.getDescription(), resMysql.getDescription());
        Assertions.assertEquals(mysql.getKindOfService(), resMysql.getKindOfService());
        Assertions.assertEquals(mysql.getServiceType(), resMysql.getServiceType());

        // not found from getbyname
        result = mockMvc.perform(get("/v1/services/name/error")
                        .param("fields", "owners,tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        // not found from getbyid
        result = mockMvc.perform(get("/v1/services/error")
                        .param("fields", "owners,tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());
    }

    void chkStorageService(StorageService storageService, String name, String displayName, String description,
                           ServiceType kindOfService, StorageService.StorageServiceType serviceType,
                           Map<Classification, List<Tag>> tags, List<String> owners) {
        Assertions.assertEquals(name, storageService.getName());
        Assertions.assertEquals(displayName, storageService.getDisplayName());
        Assertions.assertEquals(description, storageService.getDescription());
        Assertions.assertEquals(kindOfService, storageService.getKindOfService());
        Assertions.assertEquals(serviceType, storageService.getServiceType());

        if( tags != null ) {
            List<String> classificationIds = new ArrayList<>();
            List<String> tagIds = new ArrayList<>();
            List<String> tagNames = new ArrayList<>();
            for (Map.Entry<Classification, List<Tag>> entry : tags.entrySet()) {
                Classification classification = entry.getKey();
                classificationIds.add(classification.getId().toString());
                List<Tag> tagList = entry.getValue();
                for (Tag tag : tagList) {
                    tagIds.add(tag.getId().toString());
                    tagNames.add(tag.getName());
                }
            }
            for( TagLabel tag : storageService.getTags()) {
                Assertions.assertEquals(TagLabel.TagSource.CLASSIFICATION, tag.getSource());
                Assertions.assertTrue(classificationIds.contains(tag.getParentId().toString()));
                Assertions.assertTrue(tagIds.contains(tag.getId().toString()));
                Assertions.assertTrue(tagNames.contains(tag.getName()));
            }
        }
        if( owners != null ) {
            for ( EntityReference owner : storageService.getOwners()) {
                Assertions.assertTrue(owners.contains(owner.getName()));
            }
        }
    }

    StorageService createStorageService(String name, String displayName, String description,
                                          ServiceType kind, StorageService.StorageServiceType storageServiceType, Map<Classification, List<Tag>> tags, List<String> owners) throws Exception {
        CreateStorageService create = new CreateStorageService();
        create.setName(name);
        create.setDisplayName(displayName);
        create.setDescription(description);
        create.setKindOfService(kind);
        create.setServiceType(storageServiceType);

        switch(storageServiceType) {
            case StorageService.StorageServiceType.MariaDB  -> {
                MariaDBConnection connection = new MariaDBConnection();
                connection.setHostPort("localhost:3306");
                connection.setUsername("username");
                connection.setPassword("password");
                connection.setDatabaseName("database");
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.Mysql -> {
                MysqlConnection connection = new MysqlConnection();
                connection.setHostPort("localhost:3306");
                connection.setUsername("username");
                connection.setAuthType(new basicAuth().withPassword("password"));
                connection.setDatabaseName("database");
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.Postgres -> {
                PostgresConnection connection = new PostgresConnection();
                connection.setHostPort("localhost:5432");
                connection.setUsername("username");
                connection.setAuthType(new basicAuth().withPassword("password"));
                connection.setDatabase("database");
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.MinIO-> {
                MinIOConnection connection = new MinIOConnection();
                connection.withMinioConfig(
                        new MinIOCredentials()
                                .withEndPointURL(URI.create("localhost:9000"))
                                .withAccessKeyId("accesskey")
                                .withSecretAccessKey("secretkey")
                                .withRegion("ap-northeast-2"));
                connection.withBucketNames(List.of("bucket"));
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.Oracle -> {
                OracleConnection connection = new OracleConnection();
                connection.setHostPort("localhost:1521");
                connection.setUsername("username");
                connection.setPassword("password");
                connection.setOracleConnectionType(new OracleConnectionType().withAdditionalProperty("oracleServiceName", "FREE"));
                connection.setInstantClientDirectory("/instantclient");
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.Mssql -> {
                MssqlConnection connection = new MssqlConnection();
                connection.setHostPort("localhost:1433");
                connection.setUsername("username");
                connection.setPassword("password");
                connection.setDatabase("database");
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.MongoDB-> {
                MongoDBConnection connection = new MongoDBConnection();
                connection.withHostPort("localhost:27017");
                connection.withUsername("username");
                connection.withPassword("password");
                connection.withDatabaseName("database");
                connection.setSslMode(SSLMode.DISABLE);
                create.setConnection(new StorageConnection().withConfig(connection));
            }
            case StorageService.StorageServiceType.Hive -> {
                HiveConnection connection = new HiveConnection();
                connection.setHostPort("localhost:10000");
                connection.setUsername("username");
                connection.setPassword("password");
                connection.setAuth(HiveConnection.Auth.BASIC);
                connection.setDatabaseName("database");
                connection.setKerberosServiceName("hive");
                connection.setMetastoreConnection(
                new PostgresConnection()
                        .withHostPort("localhost:5432")
                        .withUsername("username")
                        .withAuthType(new BasicAuth().withPassword("password"))
                        .withDatabase("metastore"));
                create.setConnection(new StorageConnection().withConfig(connection));
            }
        }

        if( tags != null ) {
            List<TagLabel> tagLabels = new ArrayList<>();
            for (Map.Entry<Classification, List<Tag>> entry : tags.entrySet()) {
                Classification classification = entry.getKey();
                List<Tag> tagList = entry.getValue();
                for (Tag tag : tagList) {
                    TagLabel tagLabel = new TagLabel()
                            .withSource(TagLabel.TagSource.CLASSIFICATION)
                            .withParentId(classification.getId())
                            .withId(tag.getId())
                            .withName(tag.getName())
                            .withDisplayName(tag.getDisplayName())
                            .withDescription(tag.getDescription());
                    tagLabels.add(tagLabel);
                }
            }
            create.setTags(tagLabels);
        }
        if( owners != null ) {
            List<EntityReference> ownersList = new ArrayList<>();
            for (String owner : owners) {
                EntityReference entityReference = new EntityReference();
                entityReference.setId(UUID.fromString(keyCloakUtil.getUserList().get(owner)));
                entityReference.setType(Entity.USER);
                entityReference.setName(owner);
                ownersList.add(entityReference);
            }
            create.setOwners(ownersList);
        }
        MvcResult result = mockMvc.perform(post("/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(create)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        return JsonUtils.convertValue(node.get("data"), StorageService.class);
    }

    Classification createClassification(String name, String displayName, String description, Boolean exclusive) throws Exception {
        CreateClassification createData = new CreateClassification()
                .withName(name)
                .withDisplayName(displayName)
                .withDescription(description)
                .withProvider(ProviderType.USER)
                .withMutuallyExclusive(exclusive);
        MvcResult result = mockMvc.perform(post("/v1/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(createData)))
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Success", code);
        return JsonUtils.convertValue(node.get("data"), Classification.class);
    }

    Tag createTag(Classification classification, String name, String displayName, String description) throws Exception {
        CreateTag request = new CreateTag()
                .withClassification(classification.getId())
                .withName(name)
                .withDisplayName(displayName)
                .withDescription(description)
                .withProvider(ProviderType.USER);
        MvcResult result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        return JsonUtils.convertValue(node.get("data"), Tag.class);
    }

}