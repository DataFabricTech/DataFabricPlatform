package com.mobigen.vdap.server.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.filter.FilteringGeneratorDelegate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.github.fge.jsonpatch.JsonPatch;
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
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import com.mobigen.vdap.server.entity.StorageServiceEntity;
import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.extensions.ExtensionRepository;
import com.mobigen.vdap.server.extensions.ExtensionService;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.relationship.RelationshipRepository;
import com.mobigen.vdap.server.relationship.RelationshipService;
import com.mobigen.vdap.server.relationship.TagUsageRepository;
import com.mobigen.vdap.server.relationship.TagUsageService;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.ClassificationRepository;
import com.mobigen.vdap.server.tags.TagRepository;
import com.mobigen.vdap.server.users.KeyCloakUtil;
import com.mobigen.vdap.server.users.UserService;
import com.mobigen.vdap.server.util.EntityUtil;
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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

import static com.mobigen.vdap.server.util.EntityUtil.compareTagLabel;
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
    private ExtensionRepository extensionRepository;
    @Autowired
    private ExtensionService extensionService;
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
        extensionRepository.deleteAll();
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

//        Assertions.assertNull(service.getPipelines());
//        Assertions.assertNull(service.getTestConnectionResult());

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
            if (relationship.getFromId().equals(admin.getId().toString())) {
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
            if (!tag001.getId().toString().equals(tagUsage.getTagId()) && !tag002.getId().toString().equals(tagUsage.getTagId())) {
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
        List<TagLabel> tags = new ArrayList<>();

        Classification classification001 = createClassification("test_classification_001", "test_classification_001", "test classification", false);
        Tag tag001 = createTag(classification001, "test_tag_003", "test_tag_003", "test tag 003");
        tags.add(convertTagToTagLabel(tag001));
        Tag tag002 = createTag(classification001, "test_tag_004", "test_tag_004", "test tag 004");
        tags.add(convertTagToTagLabel(tag002));
        Classification classification002 = createClassification("test_classification", "test_classification", "test classification", false);
        Tag tag003 = createTag(classification002, "test_tag_001", "test_tag_001", "test tag 001");
        tags.add(convertTagToTagLabel(tag003));
        Tag tag004 = createTag(classification002, "test_tag_002", "test_tag_002", "test tag 002");
        tags.add(convertTagToTagLabel(tag004));

        // name list
        ArrayList<String> nameList = new ArrayList<>();

        // 1. MySQL
        List<String> owners001 = List.of("admin", "jblim");
        StorageService mysql = createStorageService("mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags.subList(0, 1), owners001);
        nameList.add("mysql");
        chkStorageService(mysql, "mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags.subList(0, 1), owners001);
        // 2. Postgres
        StorageService postgres = createStorageService("postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, tags.subList(1, 3), null);
        nameList.add("postgres");
        chkStorageService(postgres, "postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, tags.subList(1, 3), null);
        // 3. MariaDB
        StorageService mariadb = createStorageService("mariadb", "mariadb_display", "mariadb description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, null, null);
        nameList.add("mariadb");
        chkStorageService(mariadb, "mariadb", "mariadb_display", "mariadb description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, null, null);
        // 4. MinIO
        List<String> owners004 = List.of("admin");
        StorageService minio = createStorageService("minio", "minio_display", "minio description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, List.of(tags.get(3)), owners004);
        nameList.add("minio");
        chkStorageService(minio, "minio", "minio_display", "minio description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, List.of(tags.get(3)), owners004);
        // 5. MongoDB
        List<String> owners005 = List.of("admin", "jblim");
        StorageService mongo = createStorageService("mongo", "mongo_display", "mongo description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, null);
        nameList.add("mongo");
        chkStorageService(mongo, "mongo", "mongo_display", "mongo description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, owners005);
        // 6. OracleDB
        List<String> owners006 = List.of("jblim");
        StorageService oracle = createStorageService("oracle", "oracle_display", "oracle description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, tags.subList(1, 3), owners006);
        nameList.add("oracle");
        chkStorageService(oracle, "oracle", "oracle_display", "oracle description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, tags.subList(1, 3), owners006);

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
        nameList.sort(String.CASE_INSENSITIVE_ORDER);

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
        for (int i = 0; i < contents.size(); i++) {
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
        for (int i = 0; i < contents.size(); i++) {
            StorageService storageService = contents.get(i);
            Assertions.assertTrue(storageService.getTags() == null || storageService.getTags().isEmpty());
            Assertions.assertEquals(nameList.get(i + 2), storageService.getName());
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
                           List<TagLabel> tags, List<String> owners) {
        Assertions.assertEquals(name, storageService.getName());
        Assertions.assertEquals(displayName, storageService.getDisplayName());
        Assertions.assertEquals(description, storageService.getDescription());
        Assertions.assertEquals(kindOfService, storageService.getKindOfService());
        Assertions.assertEquals(serviceType, storageService.getServiceType());

        if (tags != null) {
            List<TagLabel> mutableTags = new ArrayList<>(tags);
            mutableTags.sort(compareTagLabel);
            List<TagLabel> destTags = storageService.getTags().stream().map(
                            tag -> new TagLabel()
                                    .withSource(tag.getSource())
                                    .withParentId(tag.getParentId())
                                    .withId(tag.getId())
                                    .withName(tag.getName())
                                    .withDisplayName(tag.getDisplayName())
                                    .withDescription(tag.getDescription()))
                    .sorted(compareTagLabel)
                    .toList();
            JsonPatch path = JsonUtils.getJsonPatch(mutableTags, destTags);
            log.info("Tag Compare Path: {}", path);
            Assertions.assertEquals("[]", path.toString());
        }
        if (owners != null) {
            for (EntityReference owner : storageService.getOwners()) {
                Assertions.assertTrue(owners.contains(owner.getName()));
            }
        }
    }

    @Test
    @Order(5)
    void updateTest() throws Exception {
        List<TagLabel> tags = new ArrayList<>();

        Classification classification = createClassification("test_classification_001", "test_classification_001", "test classification", false);
        Tag tag = createTag(classification, "test_tag_003", "test_tag_003", "test tag 003");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_004", "test_tag_004", "test tag 004");
        tags.add(convertTagToTagLabel(tag));

        classification = createClassification("test_classification", "test_classification", "test classification", false);
        tag = createTag(classification, "test_tag_001", "test_tag_001", "test tag 001");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_002", "test_tag_002", "test tag 002");
        tags.add(convertTagToTagLabel(tag));


        // 1. MySQL
        mysqlUpdateTest(tags);
        // 2. Postgres
        postgresUpdateTest(tags);
        // 3. MariaDB
        mariadbUpdateTest(tags);
        // 4. MinIO
        minioUpdateTest(tags);
        // 5. MongoDB
        mongoUpdateTest(tags);
        // 6. OracleDB
        oracleUpdateTest(tags);
        // 7. Mssql
        mssqlUpdateTest(tags);
        // 8. Hive
        hiveUpdateTest(tags);
    }

    private void mssqlUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        String name = "mssql";
        String displayName = "mssql_display";
        String description = "mssql_description";
        List<String> owners = List.of("jblim");
        StorageService storageService = createStorageService(name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Mssql, List.of(tags.get(3), tags.get(2)), owners);
        chkStorageService(storageService, name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Mssql, List.of(tags.get(3), tags.get(2)), owners);
        history.add(storageService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(storageService);
        history.add(updatedService);

        // Update Test : Connection
        MssqlConnection connection = new MssqlConnection();
        connection.setHostPort("localhost_modify:1433");
        connection.setUsername("username_modify");
        connection.setPassword("password_modify");
        connection.setDatabase("database_modify");

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(3)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("admin"));

        // ...
    }

    private void hiveUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        String name = "hive";
        String displayName = "hive_display";
        String description = "hive_description";
        List<String> owners = List.of("jblim");
        StorageService storageService = createStorageService(name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Hive, List.of(tags.get(3), tags.get(1)), owners);
        chkStorageService(storageService, name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Hive, List.of(tags.get(3), tags.get(1)), owners);
        history.add(storageService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(storageService);
        history.add(updatedService);

        // Update Test : Connection
        HiveConnection connection = new HiveConnection();
        connection.setHostPort("modify:10000");
        connection.setUsername("username_modify");
        connection.setPassword("password_modify");
        connection.setAuth(HiveConnection.Auth.BASIC);
        connection.setDatabaseName("database_modify");
        connection.setKerberosServiceName("hive_modify");
        connection.setMetastoreConnection(
                new PostgresConnection()
                        .withHostPort("localhost_modify:5432")
                        .withUsername("username_modify")
                        .withAuthType(new BasicAuth().withPassword("password_modify"))
                        .withDatabase("metastore_modify"));

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(3)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("admin"));

        // ...

    }

    private void oracleUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        String name = "oracle";
        String displayName = "oracle_display";
        String description = "oracle_description";
        List<String> owners = List.of("admin", "jblim");
        StorageService storageService = createStorageService(name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, List.of(tags.get(3), tags.get(1)), owners);
        chkStorageService(storageService, name, displayName, description,
                ServiceType.DATABASE, StorageService.StorageServiceType.Oracle, List.of(tags.get(3), tags.get(1)), owners);
        history.add(storageService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(storageService);
        history.add(updatedService);

        // Update Test : Connection
        OracleConnection connection = new OracleConnection();
        connection.setHostPort("modify:1521");
        connection.setUsername("username_modify");
        connection.setPassword("password_modify");
        connection.setOracleConnectionType(new OracleConnectionType().withAdditionalProperty("oracleServiceName", "FREE_Modify"));
        connection.setInstantClientDirectory("/instantclient");

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(3)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("jblim"));

        // ...

    }

    private void mongoUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        StorageService storageService = createStorageService("mongo", "mongo_display", "mongo_description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, null);
        chkStorageService(storageService, "mongo", "mongo_display", "mongo_description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MongoDB, null, null);
        history.add(storageService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(storageService);
        history.add(updatedService);

        // Update Test : Connection
        MongoDBConnection connection = JsonUtils.convertValue(storageService.getConnection().getConfig(), MongoDBConnection.class);
        connection.withHostPort("modify:27017");
        connection.withUsername("username_modify");
        connection.withPassword("password_modify");
        connection.withDatabaseName("database_modify");
        connection.setSslMode(SSLMode.DISABLE);

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(3)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("jblim"));

        // ...
    }

    private void minioUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        List<String> owners = List.of("admin", "jblim");
        StorageService minioService = createStorageService("minio", "minio_display", "minio_description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, tags.subList(1, 3), owners);
        chkStorageService(minioService, "minio", "minio_display", "minio_description",
                ServiceType.STORAGE, StorageService.StorageServiceType.MinIO, tags.subList(1, 3), owners);
        history.add(minioService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(minioService);
        history.add(updatedService);

        // Update Test : Connection
        MinIOConnection connection = JsonUtils.convertValue(minioService.getConnection().getConfig(), MinIOConnection.class);
        MinIOCredentials credentials = new MinIOCredentials();
        credentials.withEndPointURL(URI.create("localhost:9000"));
        credentials.withAccessKeyId("username");
        credentials.withSecretAccessKey("password");
        credentials.withRegion("ap-northeast-2");
        connection.setMinioConfig(credentials);

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, null);
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, null);

        // ...
    }

    void mysqlUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        List<String> owners = List.of("admin", "jblim");
        StorageService mysqlService = createStorageService("mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags.subList(1, 3), owners);
        chkStorageService(mysqlService, "mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags.subList(1, 3), owners);
        history.add(mysqlService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(mysqlService);
        history.add(updatedService);

        // Update Test : Connection
        MysqlConnection connection = JsonUtils.convertValue(mysqlService.getConnection().getConfig(), MysqlConnection.class);
        connection.setHostPort("localhost:3307");
        connection.setUsername("username_modify");
        connection.setAuthType(new basicAuth().withPassword("password_modify"));
        connection.setDatabaseName("test_modify");

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(1)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("admin"));

        // ...
    }

    void postgresUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        StorageService postgres = createStorageService("postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, List.of(tags.getFirst()), null);
        chkStorageService(postgres, "postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, List.of(tags.getFirst()), null);
        history.add(postgres);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(postgres);
        history.add(updatedService);

        // Update Test : Connection
        PostgresConnection connection = JsonUtils.convertValue(postgres.getConnection().getConfig(), PostgresConnection.class);
        connection.setHostPort("localhost:3307");
        connection.setUsername("username_modify");
        connection.setAuthType(new basicAuth().withPassword("password_modify"));
        connection.setDatabase("test_modify");

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(0), tags.get(1)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("jblim"));
    }

    void mariadbUpdateTest(List<TagLabel> tags) throws Exception {
        List<StorageService> history = new ArrayList<>();
        StorageService mariadbService = createStorageService("mariadb", "mariadb_display", "mariadb_description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, List.of(tags.get(2), tags.get(3)), null);
        chkStorageService(mariadbService, "mariadb", "mariadb_display", "mariadb_description",
                ServiceType.DATABASE, StorageService.StorageServiceType.MariaDB, List.of(tags.get(2), tags.get(3)), null);
        history.add(mariadbService);

        // Update Test, name, displayName, description
        StorageService updatedService = checkDefaultUpdate(mariadbService);
        history.add(updatedService);

        // Update Test : Connection
        MariaDBConnection connection = JsonUtils.convertValue(mariadbService.getConnection().getConfig(), MariaDBConnection.class);
        connection.setHostPort("localhost:3307");
        connection.setUsername("username_modify");
        connection.setPassword("password_modify");
        connection.setDatabaseName("test_modify");

        updatedService = checkConnectionUpdate(history, connection);
        history.add(updatedService);

        // Update Tag
        updatedService = checkUpdateTags(history, List.of(tags.get(1), tags.get(0)));
        history.add(updatedService);

        // Update Owner
        checkUpdateOwners(history, List.of("jblim", "admin"));
    }


    StorageService checkDefaultUpdate(StorageService original) throws Exception {
        CreateStorageService updateRequest = convertFromStorageService(original);
        updateRequest.setName(original.getName() + "_modify");
        updateRequest.setDisplayName(original.getDisplayName() + "_modify");
        updateRequest.setDescription(original.getDescription() + "_modify");

        MvcResult result = mockMvc.perform(post("/v1/services/" + original.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService updatedService = JsonUtils.convertValue(node.get("data"), StorageService.class);
        Assertions.assertEquals(updateRequest.getName(), updatedService.getName());
        Assertions.assertEquals(updateRequest.getDisplayName(), updatedService.getDisplayName());
        Assertions.assertEquals(updateRequest.getDescription(), updatedService.getDescription());
        Assertions.assertEquals(EntityUtil.nextVersion(original.getVersion()), updatedService.getVersion());
        Assertions.assertNotNull(updatedService.getChangeDescription());
        List<EntityExtension> extensions = extensionService.getExtensions(
                original.getId().toString(),
                EntityUtil.getVersionExtensionPrefix(Entity.STORAGE_SERVICE) + ".");
        Assertions.assertEquals(1, extensions.size());
        StorageService service_v01 = JsonUtils.readValue(extensions.getFirst().getJson(), StorageService.class);
        Assertions.assertEquals(original.getName(), service_v01.getName());
        Assertions.assertEquals(original.getDisplayName(), service_v01.getDisplayName());
        Assertions.assertEquals(original.getDescription(), service_v01.getDescription());

        return updatedService;
    }

    StorageService checkConnectionUpdate(List<StorageService> history, Object connection) throws Exception {
        StorageService original = history.getLast();
        CreateStorageService updateRequest = convertFromStorageService(original);
        updateRequest.setConnection(new StorageConnection().withConfig(connection));

        MvcResult result = mockMvc.perform(post("/v1/services/" + original.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService updatedService = JsonUtils.convertValue(node.get("data"), StorageService.class);
        Assertions.assertEquals(updateRequest.getName(), updatedService.getName());
        Assertions.assertEquals(updateRequest.getDisplayName(), updatedService.getDisplayName());
        Assertions.assertEquals(updateRequest.getDescription(), updatedService.getDescription());
        Assertions.assertEquals(EntityUtil.nextVersion(original.getVersion()), updatedService.getVersion());
        Assertions.assertNotNull(updatedService.getChangeDescription());

        // Check Connection With Decrypt And JsonDiff
        compareConnection(connection, updatedService.getConnection().getConfig(),
                original.getServiceType().toString(), original.getName(), original.getKindOfService());

        // 버전 히스토리 저장 확인
        List<EntityExtension> extensions = extensionService.getExtensions(
                original.getId().toString(),
                EntityUtil.getVersionExtensionPrefix(Entity.STORAGE_SERVICE) + ".");
        Assertions.assertEquals(2, extensions.size());
        StorageService history_v01 = JsonUtils.readValue(extensions.getLast().getJson(), StorageService.class);
        StorageService original_v01 = history.getFirst();
        Assertions.assertEquals(original_v01.getName(), history_v01.getName());
        Assertions.assertEquals(original_v01.getDisplayName(), history_v01.getDisplayName());
        Assertions.assertEquals(original_v01.getDescription(), history_v01.getDescription());

        StorageService history_v02 = JsonUtils.readValue(extensions.getFirst().getJson(), StorageService.class);
        compareConnection(history.getLast().getConnection().getConfig(), history_v02.getConnection().getConfig(),
                original.getServiceType().toString(), original.getName(), original.getKindOfService());
        return updatedService;
    }

    StorageService checkUpdateTags(List<StorageService> history, List<TagLabel> tags) throws Exception {
        StorageService original = history.getLast();
        CreateStorageService updateRequest = convertFromStorageService(original);
        updateRequest.setTags(tags);

        MvcResult result = mockMvc.perform(post("/v1/services/" + original.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService resService = JsonUtils.convertValue(node.get("data"), StorageService.class);

        // Check Tags
        if( tags != null ) {
            Assertions.assertEquals(updateRequest.getTags().size(), resService.getTags().size());
            List<TagLabel> mutableTags = new ArrayList<>(tags);
            mutableTags.sort(compareTagLabel);
            List<TagLabel> destTags = resService.getTags().stream().map(
                            tag -> new TagLabel()
                                    .withSource(tag.getSource())
                                    .withParentId(tag.getParentId())
                                    .withId(tag.getId())
                                    .withName(tag.getName())
                                    .withDisplayName(tag.getDisplayName())
                                    .withDescription(tag.getDescription()))
                    .sorted(compareTagLabel)
                    .toList();
            JsonPatch path = JsonUtils.getJsonPatch(mutableTags, destTags);
            Assertions.assertEquals("[]", path.toString());
        } else {
            Assertions.assertTrue(resService.getTags() == null || resService.getTags().isEmpty());
        }
        return resService;
    }

    void compareConnection(Object expected, Object actual, String serviceType, String serviceName, ServiceType kindof) throws Exception {
        Object expectedConnection = null;
        try {
            expectedConnection = new SecretsManager().decryptServiceConnectionConfig(
                    expected, serviceType, serviceName, kindof);
        } catch (Exception e) {
            log.error("SecretsManager decrypt error: {}", e.getMessage());
            // 에러 발생 시 이미 복호화된 상태일 것 이다.
            expectedConnection = expected;
        }
        Object actualConnection = null;
        try {
            actualConnection = new SecretsManager().decryptServiceConnectionConfig(
                    actual, serviceType, serviceName, kindof);
        } catch (Exception e) {
            log.error("SecretsManager decrypt error: {}", e.getMessage());
            // 에러 발생 시 이미 복호화된 상태일 것 이다.
            actualConnection = expected;
        }
        JsonPatch patch = JsonUtils.getJsonPatch(expectedConnection, actualConnection);
        Assertions.assertEquals("[]", patch.toString());
    }

    CreateStorageService convertFromStorageService(StorageService storageService) {
        SecretsManager secret = new SecretsManager();
        CreateStorageService create = new CreateStorageService();
        create.setName(storageService.getName());
        create.setDisplayName(storageService.getDisplayName());
        create.setDescription(storageService.getDescription());
        create.setKindOfService(storageService.getKindOfService());
        create.setServiceType(storageService.getServiceType());
        create.setConnection(
                new StorageConnection().withConfig(
                        secret.decryptServiceConnectionConfig(
                                storageService.getConnection().getConfig(),
                                storageService.getServiceType().value(),
                                storageService.getName(),
                                storageService.getKindOfService())));
        create.setTags(storageService.getTags());
        create.setOwners(storageService.getOwners());
        return create;
    }

    StorageService createStorageService(String name, String displayName, String description,
                                        ServiceType kind, StorageService.StorageServiceType storageServiceType,
                                        List<TagLabel> tags, List<String> owners) throws Exception {
        CreateStorageService create = new CreateStorageService();
        create.setName(name);
        create.setDisplayName(displayName);
        create.setDescription(description);
        create.setKindOfService(kind);
        create.setServiceType(storageServiceType);

        switch (storageServiceType) {
            case StorageService.StorageServiceType.MariaDB -> {
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
            case StorageService.StorageServiceType.MinIO -> {
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
            case StorageService.StorageServiceType.MongoDB -> {
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
        // Set Tags
        create.setTags(tags);
        // Set Owners
        create.setOwners(getOwners(owners));
        // Send Request
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

    private StorageService checkUpdateOwners(List<StorageService> history, List<String> owners) throws Exception {
        StorageService original = history.getLast();
        CreateStorageService updateRequest = convertFromStorageService(original);
        updateRequest.setOwners(getOwners(owners));
        MvcResult result = mockMvc.perform(post("/v1/services/" + original.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService updatedService = JsonUtils.convertValue(node.get("data"), StorageService.class);
        if( owners != null ) {
            Assertions.assertEquals(updateRequest.getOwners().size(), updatedService.getOwners().size());
            for (EntityReference owner : updatedService.getOwners()) {
                Assertions.assertTrue(owners.contains(owner.getName()));
            }
        } else {
            Assertions.assertTrue(updatedService.getOwners() == null || updatedService.getOwners().isEmpty());
        }
        return updatedService;
    }

    @Test
    @Order(6)
    void DeleteTest() throws Exception {
        List<TagLabel> tags = new ArrayList<>();

        Classification classification = createClassification("test_classification_001", "test_classification_001", "test classification", false);
        Tag tag = createTag(classification, "test_tag_003", "test_tag_003", "test tag 003");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_004", "test_tag_004", "test tag 004");
        tags.add(convertTagToTagLabel(tag));

        classification = createClassification("test_classification", "test_classification", "test classification", false);
        tag = createTag(classification, "test_tag_001", "test_tag_001", "test tag 001");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_002", "test_tag_002", "test tag 002");
        tags.add(convertTagToTagLabel(tag));

        // 1. MySQL
        List<String> owners = List.of("admin", "jblim");
        StorageService mysql = createStorageService("mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, tags.subList(0, 1), owners);
        // 2. Postgres
        StorageService postgres = createStorageService("postgres", "postgres_display", "postgres description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Postgres, tags.subList(1, 3), null);


        // DeleteByID
        // Delete Parameter
        // @RequestParam(name = "hardDelete", defaultValue = "false")
        // @RequestParam(name = "recursive", defaultValue = "false")

        // Soft Delete
        MvcResult result = mockMvc.perform(post("/v1/services/" + mysql.getId() + "/delete")
                        .param("hardDelete", "false")
                        .param("recursive", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Assertions.assertEquals("success", node.get("data").asText());

        // Deleted Field Check
        storageServiceRepository.findById(mysql.getId().toString()).ifPresent( entity -> {
            Assertions.assertEquals(true, entity.getDeleted());
        });

        // Check Owner Relationship
        List<RelationshipEntity> relationships = relationshipService.getRelationships(
                null, mysql.getId(), null, Entity.STORAGE_SERVICE, Relationship.OWNS, Include.DELETED);
        Assertions.assertEquals(2, relationships.size());
        for (RelationshipEntity relationship : relationships) {
            Assertions.assertTrue(relationship.getDeleted());
        }

        // Check TagUsage
        List<TagUsageEntity> tagUsages = tagUsageService.getTagUsages(
                null, null, null, Entity.STORAGE_SERVICE, mysql.getId().toString());
        Assertions.assertEquals(0, tagUsages.size());

        // TODO : DataModel(MultiModel) 에 대한 개발 후 Children 에 대한 부분도 테스트 필요

        // Hard Delete
        result = mockMvc.perform(post("/v1/services/" + mysql.getId() + "/delete")
                        .param("hardDelete", "true")
                        .param("recursive", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Assertions.assertEquals("success", node.get("data").asText());

        // Deleted Field Check
        Assertions.assertNull(storageServiceRepository.findById(mysql.getId().toString()).orElse(null));

        // Check Owner Relationship
        relationships = relationshipService.getRelationships(
                null, mysql.getId(), null, Entity.STORAGE_SERVICE, Relationship.OWNS, Include.ALL);
        Assertions.assertEquals(0, relationships.size());

        // Check TagUsage
        tagUsages = tagUsageService.getTagUsages(
                null, null, null, Entity.STORAGE_SERVICE, mysql.getId().toString());
        Assertions.assertEquals(0, tagUsages.size());

        // TODO : DataModel(MultiModel) 에 대한 개발 후 Children 에 대한 부분도 테스트 필요

        // DeleteByName
        // Delete Parameter
        // @RequestParam(name = "hardDelete", defaultValue = "false")
        // @RequestParam(name = "recursive", defaultValue = "false")

        // Soft Delete
        result = mockMvc.perform(post("/v1/services/name/" + postgres.getName() + "/delete")
                        .param("hardDelete", "false")
                        .param("recursive", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Assertions.assertEquals("success", node.get("data").asText());

        // Deleted Field Check
        storageServiceRepository.findById(postgres.getId().toString()).ifPresent( entity -> {
            Assertions.assertEquals(true, entity.getDeleted());
        });

        // Check Owner Relationship
        relationships = relationshipService.getRelationships(
                null, postgres.getId(), null, Entity.STORAGE_SERVICE, Relationship.OWNS, Include.DELETED);
        Assertions.assertEquals(0, relationships.size());

        // Check TagUsage
        tagUsages = tagUsageService.getTagUsages(
                null, null, null, Entity.STORAGE_SERVICE, postgres.getId().toString());
        Assertions.assertEquals(0, tagUsages.size());

        // TODO : DataModel(MultiModel) 에 대한 개발 후 Children 에 대한 부분도 테스트 필요

        // Hard Delete
        result = mockMvc.perform(post("/v1/services/name/" + postgres.getName() + "/delete")
                        .param("hardDelete", "true")
                        .param("recursive", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Assertions.assertEquals("success", node.get("data").asText());

        // Deleted Field Check
        Assertions.assertNull(storageServiceRepository.findById(postgres.getId().toString()).orElse(null));

        // Check Owner Relationship
        relationships = relationshipService.getRelationships(
                null, postgres.getId(), null, Entity.STORAGE_SERVICE, Relationship.OWNS, Include.ALL);
        Assertions.assertEquals(0, relationships.size());

        // Check TagUsage
        tagUsages = tagUsageService.getTagUsages(
                null, null, null, Entity.STORAGE_SERVICE, postgres.getId().toString());
        Assertions.assertEquals(0, tagUsages.size());

        // TODO : DataModel(MultiModel) 에 대한 개발 후 Children 에 대한 부분도 테스트 필요
    }

    @Test
    @Order(7)
    void patchTest() throws Exception {
        List<TagLabel> tags = new ArrayList<>();

        Classification classification = createClassification("test_classification_001", "test_classification_001", "test classification", false);
        Tag tag = createTag(classification, "test_tag_003", "test_tag_003", "test tag 003");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_004", "test_tag_004", "test tag 004");
        tags.add(convertTagToTagLabel(tag));

        classification = createClassification("test_classification", "test_classification", "test classification", false);
        tag = createTag(classification, "test_tag_001", "test_tag_001", "test tag 001");
        tags.add(convertTagToTagLabel(tag));
        tag = createTag(classification, "test_tag_002", "test_tag_002", "test tag 002");
        tags.add(convertTagToTagLabel(tag));

        // 1. MySQL
        List<String> owners = List.of("admin", "jblim");
        StorageService mysql = createStorageService("mysql", "mysql_display", "mysql description",
                ServiceType.DATABASE, StorageService.StorageServiceType.Mysql, List.of(tags.get(1), tags.get(2)), owners);

        StorageService updatedService = JsonUtils.deepCopy(mysql, StorageService.class);
        updatedService.setName("mysql_modify");
        updatedService.setDisplayName("mysql_display_modify");
        updatedService.setDescription("mysql description_modify");
        updatedService.setTags(List.of(tags.get(0), tags.get(1)));
        updatedService.setOwners(getOwners(List.of("jblim")));

        JsonPatch patch = JsonUtils.getJsonPatch(mysql, updatedService);
        String strPatchJson = JsonUtils.getObjectMapper().writeValueAsString(patch);

//        log.info("Patch: {}", strPatchJson);
        log.info("original: {}", JsonUtils.getObjectMapper().writeValueAsString(mysql));
        log.info("Patch: {}", strPatchJson);
        MvcResult result = mockMvc.perform(post("/v1/services/" + mysql.getId() + "/patch")
                        .contentType(jakarta.ws.rs.core.MediaType.APPLICATION_JSON_PATCH_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(strPatchJson))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        StorageService resService = JsonUtils.convertValue(node.get("data"), StorageService.class);
        Assertions.assertEquals(updatedService.getName(), resService.getName());
        Assertions.assertEquals(updatedService.getDisplayName(), resService.getDisplayName());
        Assertions.assertEquals(updatedService.getDescription(), resService.getDescription());
        Assertions.assertEquals(EntityUtil.nextVersion(mysql.getVersion()), resService.getVersion());
        Assertions.assertNotNull(resService.getChangeDescription());
        // Tag
        Assertions.assertEquals(updatedService.getTags().size(), resService.getTags().size());
        List<TagLabel> mutableTags = new ArrayList<>(updatedService.getTags());
        mutableTags.sort(compareTagLabel);
        List<TagLabel> destTags = resService.getTags().stream().map(
                        tagLabel -> new TagLabel()
                                .withSource(tagLabel.getSource())
                                .withParentId(tagLabel.getParentId())
                                .withId(tagLabel.getId())
                                .withName(tagLabel.getName())
                                .withDisplayName(tagLabel.getDisplayName())
                                .withDescription(tagLabel.getDescription()))
                .sorted(compareTagLabel)
                .toList();
        JsonPatch path = JsonUtils.getJsonPatch(mutableTags, destTags);
        Assertions.assertEquals("[]", path.toString());
        // Owner
        Assertions.assertEquals(updatedService.getOwners().size(), resService.getOwners().size());
        for (EntityReference owner : resService.getOwners()) {
            Assertions.assertTrue(updatedService.getOwners().stream()
                    .anyMatch(entityReference -> entityReference.getName().equals(owner.getName())));
        }
    }

    List<EntityReference> getOwners(List<String> owners) {
        if (owners != null && !owners.isEmpty()) {
            List<EntityReference> ownersList = new ArrayList<>();
            for (String owner : owners) {
                EntityReference entityReference = new EntityReference();
                entityReference.setId(UUID.fromString(keyCloakUtil.getUserList().get(owner)));
                entityReference.setType(Entity.USER);
                entityReference.setName(owner);
                ownersList.add(entityReference);
            }
            return ownersList;
        }
        return null;
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

    private TagLabel convertTagToTagLabel(Tag tag) {
        return new TagLabel()
                .withSource(TagLabel.TagSource.CLASSIFICATION)
                .withParentId(tag.getClassification().getId())
                .withId(tag.getId())
                .withName(tag.getName())
                .withDisplayName(tag.getDisplayName())
                .withDescription(tag.getDescription());
    }

}