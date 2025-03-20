package com.mobigen.vdap.server.tags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
import com.mobigen.vdap.server.util.JsonUtils;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClassificationRepository repository;
    @Autowired
    private EntityExtensionRepository entityExtensionRepository;

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withReuse(true)
            .withUsername("root")
            .withPassword("password")
            .withDatabaseName("open_vdap");

    @BeforeAll
    static void beforeAll() {
        // Flyway 마이그레이션 실행
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .load();
        flyway.migrate();
        log.info("flyway migration finish");
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        mysql.start();
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        repository.deleteAll();
        entityExtensionRepository.deleteAll();
    }

    @Test
    @Order(1)
    void create() throws Exception {
        CreateClassification request = new CreateClassification()
                .withName("test")
                .withDescription("classification-test-01")
                .withDisplayName("category-test-01")
                .withProvider(ProviderType.USER)
                .withMutuallyExclusive(false);
        MvcResult result = mockMvc.perform(post("/v1/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        Classification classification = JsonUtils.convertValue(node.get("data"), Classification.class);

        Assertions.assertNotNull(classification.getId());
        Assertions.assertEquals(request.getName(), classification.getName());
        Assertions.assertEquals(request.getDescription(), classification.getDescription());
        Assertions.assertEquals(request.getDisplayName(), classification.getDisplayName());
        Assertions.assertNotNull(classification.getUpdatedAt());
        Assertions.assertNotNull(classification.getHref());
        Assertions.assertNotNull(classification.getUpdatedBy());

        // 동일한 이름의 데이터 생성 시도 : 실패
        result = mockMvc.perform(post("/v1/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Error", code);
        String errMsg = node.get("errorMsg").asText();
        Assertions.assertNotNull(errMsg);

        request = new CreateClassification()
                .withName("test2")
                .withDescription("classification-test-02")
                .withDisplayName("category-test-02")
                .withProvider(ProviderType.SYSTEM)
                .withMutuallyExclusive(true);
        result = mockMvc.perform(post("/v1/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        classification = JsonUtils.convertValue(node.get("data"), Classification.class);

        Assertions.assertNotNull(classification.getId());
        Assertions.assertEquals(request.getName(), classification.getName());
        // 기본적으로 0.1 이 설정되어 있음.
        Assertions.assertEquals(0.1, classification.getVersion());
        Assertions.assertEquals(request.getDescription(), classification.getDescription());
        Assertions.assertEquals(request.getDisplayName(), classification.getDisplayName());
        Assertions.assertNotNull(classification.getUpdatedAt());
        Assertions.assertNotNull(classification.getHref());
        Assertions.assertNotNull(classification.getUpdatedBy());
    }

    @Test
    @Order(2)
    void getTest() throws Exception {
        // Set Data
        createClassification("get-test", "get-test-display-name", "get-test-desc");

        // Get By Name Not Error
        MvcResult result = mockMvc.perform(get("/v1/classifications/name/error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Error", code);
        Assertions.assertNotNull(node.get("errorMsg"));

        // Get By Id Not Found
        result = mockMvc.perform(get("/v1/classifications/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Error", code);
        Assertions.assertNotNull(node.get("errorMsg"));

        // Get By Name
        result = mockMvc.perform(get("/v1/classifications/name/get-test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        Classification resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);

        Assertions.assertNotNull(resClassification.getId());
        Assertions.assertEquals("get-test", resClassification.getName());
        Assertions.assertEquals("get-test-display-name", resClassification.getDisplayName());
        Assertions.assertEquals("get-test-desc", resClassification.getDescription());
        Assertions.assertNotNull(resClassification.getUpdatedAt());
        Assertions.assertNotNull(resClassification.getUpdatedBy());
        Assertions.assertNotNull(resClassification.getHref());

        // Get By Id
        result = mockMvc.perform(get("/v1/classifications/" + resClassification.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);

        Assertions.assertNotNull(resClassification.getId());
        Assertions.assertEquals("get-test", resClassification.getName());
        Assertions.assertEquals("get-test-display-name", resClassification.getDisplayName());
        Assertions.assertEquals("get-test-desc", resClassification.getDescription());
        Assertions.assertNotNull(resClassification.getUpdatedAt());
        Assertions.assertNotNull(resClassification.getHref());
        Assertions.assertNotNull(resClassification.getUpdatedBy());
    }

    @Test
    @Order(2)
    void updateTest() throws Exception {
        Classification orignal = createClassification("update-test", "update-test-displayname", "update-test-desc");
        CreateClassification updateData = new CreateClassification()
                .withName(orignal.getName())
                .withDisplayName(orignal.getName())
                .withDescription(orignal.getName());
        // 1. name
        updateData = updateData.withName("name-update");
        MvcResult result = mockMvc.perform(post("/v1/classifications/" + orignal.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        Classification resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(resClassification.getId());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(updateData.getName(), resClassification.getName());
        Assertions.assertEquals(updateData.getDescription(), resClassification.getDescription());
        Assertions.assertEquals(updateData.getDisplayName(), resClassification.getDisplayName());

        // version list
        result = mockMvc.perform(get("/v1/classifications/" + resClassification.getId() + "/versions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        EntityHistory history = JsonUtils.convertValue(node.get("data"), EntityHistory.class);
        Assertions.assertNotNull(history);
        Assertions.assertEquals(Entity.CLASSIFICATION, history.getEntityType());
        Assertions.assertEquals(2, history.getVersions().size());
        Classification old = JsonUtils.readValue((String) history.getVersions().getLast(), Classification.class);
        Assertions.assertEquals("update-test", old.getName());
        Assertions.assertEquals("update-test-displayname", old.getDisplayName());
        Assertions.assertEquals("update-test-desc", old.getDescription());

        result = mockMvc.perform(get("/v1/classifications/" + resClassification.getId() + "/versions/" + old.getVersion())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        Classification oldVersion = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(oldVersion);
        Assertions.assertEquals("update-test", old.getName());
        Assertions.assertEquals("update-test-displayname", old.getDisplayName());
        Assertions.assertEquals("update-test-desc", old.getDescription());

        result = mockMvc.perform(get("/v1/classifications/" + resClassification.getId() + "/versions/" + resClassification.getVersion())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        Classification latestVersion = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(oldVersion);
        Assertions.assertEquals("name-update", latestVersion.getName());
        Assertions.assertEquals("update-test-displayname", old.getDisplayName());
        Assertions.assertEquals("update-test-desc", old.getDescription());

        // 1.1. Get
        result = mockMvc.perform(get("/v1/classifications/name/" + updateData.getName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        // 2. description
        updateData.withDescription("description update");
        result = mockMvc.perform(post("/v1/classifications/" + resClassification.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(updateData.getName(), resClassification.getName());
        Assertions.assertEquals(updateData.getDescription(), resClassification.getDescription());

        // 3. displayName
        updateData.withDisplayName("displayname update");
        result = mockMvc.perform(post("/v1/classifications/" + resClassification.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateData)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(updateData.getName(), resClassification.getName());
        Assertions.assertEquals(updateData.getDisplayName(), resClassification.getDisplayName());
        Assertions.assertEquals(updateData.getDescription(), resClassification.getDescription());

        // 3.1. Get
        result = mockMvc.perform(get("/v1/classifications/name/" + updateData.getName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(updateData.getName(), resClassification.getName());
        Assertions.assertEquals(updateData.getDisplayName(), resClassification.getDisplayName());
        Assertions.assertEquals(updateData.getDescription(), resClassification.getDescription());

    }

    @Test
    @Order(3)
    void delete() throws Exception {
        // Create Data
        Classification data01 = createClassification("delete-test-01", "delete-test-01-displayname", "delete-test-01-desc");
        Classification data02 = createClassification("delete-test-02", "delete-test-02-displayname", "delete-test-02-desc");

        // Delete By Id - Data 01
        MvcResult result = mockMvc.perform(post("/v1/classifications/" + data01.getId() + "/delete")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());


        // Delete By Id - Data 02
        result = mockMvc.perform(post("/v1/classifications/name/" + data02.getName() + "/delete")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        // Delete Error
        result = mockMvc.perform(post("/v1/classifications/name/" + data02.getName() + "/delete")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        // Get List
        result = mockMvc.perform(get("/v1/classifications")
                        .param("page", "0")
                        .param("size", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        PageModel<Classification> pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {
                });
        Assertions.assertNotNull(pageResponse);
        Assertions.assertEquals(0, pageResponse.getTotalElements());
    }

    @Test
    @Order(4)
    void list() throws Exception {
        int currentSize = 4;
        List<Classification> data = new ArrayList<>();
        data.add(createClassification("list-test-01", "list-test-01-displayname", "list-test-desc-01"));
        data.add(createClassification("list-test-02", "list-test-02-displayname", "list-test-desc-02"));
        data.add(createClassification("list-test-03", "list-test-03-displayname", "list-test-desc-03"));
        data.add(createClassification("list-test-04", "list-test-04-displayname", "list-test-desc-04"));

        MvcResult result = mockMvc.perform(get("/v1/classifications")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        PageModel<Classification> pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {
                });
        Assertions.assertEquals(0, pageResponse.getPage());
        Assertions.assertEquals(2, pageResponse.getSize());
        Assertions.assertEquals(2, pageResponse.getTotalPages());
        Assertions.assertEquals(currentSize, pageResponse.getTotalElements());
        Assertions.assertNotNull(pageResponse.getContents());

        List<Classification> classificationList = pageResponse.getContents();

        // next page
        result = mockMvc.perform(get("/v1/classifications")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {
                });
        Assertions.assertEquals(1, pageResponse.getPage());
        Assertions.assertEquals(2, pageResponse.getSize());
        Assertions.assertEquals(2, pageResponse.getTotalPages());
        Assertions.assertEquals(currentSize, pageResponse.getTotalElements());
        Assertions.assertNotNull(pageResponse.getContents());
    }

    public Classification createClassification(String name, String displayName, String description) throws Exception {
        CreateClassification createData = new CreateClassification()
                .withName(name)
                .withDisplayName(displayName)
                .withDescription(description)
                .withProvider(ProviderType.USER)
                .withMutuallyExclusive(false);
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
}