package com.mobigen.vdap.server.tags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.flywaydb.core.Flyway;
import org.hibernate.query.Page;
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

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0")
            .withReuse(true)
            .withUsername("root")
            .withPassword("password")
            .withDatabaseName("open_vdap");

    @BeforeAll
    static void beforeAll() {
        log.warn("before all");

        // Flyway 마이그레이션 실행
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .load();
        flyway.migrate();
        log.info("flyway migration finish");
    }

    @AfterAll
    static void afterAll() {
        log.warn("after all");
        mysql.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.warn("config");
        mysql.start();
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
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

        // id
        Assertions.assertNotNull(classification.getId());
        // name
        Assertions.assertEquals(request.getName(), classification.getName());
        // version
        Assertions.assertNotNull(classification.getVersion());
        // description
        Assertions.assertEquals(request.getDescription(), classification.getDescription());
        // displayName
        Assertions.assertEquals(request.getDisplayName(), classification.getDisplayName());
        // updatedAt
        Assertions.assertNotNull(classification.getUpdatedAt());
        // href
        Assertions.assertNotNull(classification.getHref());
        // updatedBy
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
        Assertions.assertNotNull(classification.getVersion());
        Assertions.assertEquals(request.getDescription(), classification.getDescription());
        Assertions.assertEquals(request.getDisplayName(), classification.getDisplayName());
        Assertions.assertNotNull(classification.getUpdatedAt());
        Assertions.assertNotNull(classification.getHref());
        Assertions.assertNotNull(classification.getUpdatedBy());
    }

    @Test
    @Order(2)
    void getAndUpdate() throws Exception {
        // Set Data
        CreateClassification testClassification = new CreateClassification()
                .withName("test")
                .withDescription("classification-test-01")
                .withDisplayName("category-test-01")
                .withProvider(ProviderType.USER)
                .withMutuallyExclusive(false);
        mockMvc.perform(post("/v1/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(testClassification)))
                .andReturn();

        // Get Classification Error
        MvcResult result = mockMvc.perform(get("/v1/classifications/name/error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Error", code);
        Assertions.assertNotNull(node.get("errorMsg"));

        result = mockMvc.perform(get("/v1/classifications/not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Error", code);
        Assertions.assertNotNull(node.get("errorMsg"));

        // Get By Name
        result = mockMvc.perform(get("/v1/classifications/name/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        Classification resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);

        Assertions.assertNotNull(resClassification.getId());
        Assertions.assertEquals(testClassification.getName(), resClassification.getName());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());
        Assertions.assertEquals(testClassification.getDisplayName(), resClassification.getDisplayName());
        Assertions.assertNotNull(resClassification.getUpdatedAt());
        Assertions.assertNotNull(resClassification.getHref());
        Assertions.assertNotNull(resClassification.getUpdatedBy());

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
        Assertions.assertEquals(testClassification.getName(), resClassification.getName());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());
        Assertions.assertEquals(testClassification.getDisplayName(), resClassification.getDisplayName());
        Assertions.assertNotNull(resClassification.getUpdatedAt());
        Assertions.assertNotNull(resClassification.getHref());
        Assertions.assertNotNull(resClassification.getUpdatedBy());


        // 1. name
        testClassification.withName("test3");
        result = mockMvc.perform(post("/v1/classifications/" + resClassification.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(testClassification)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(resClassification.getId());
        Assertions.assertEquals(testClassification.getName(), resClassification.getName());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());
        Assertions.assertEquals(testClassification.getDisplayName(), resClassification.getDisplayName());


        // 1.1. Get
        result = mockMvc.perform(get("/v1/classifications/name/" + testClassification.getName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);


        // 2. description
        testClassification.withDescription("description change");
        result = mockMvc.perform(post("/v1/classifications/" + resClassification.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(testClassification)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(testClassification.getName(), resClassification.getName());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());

        // 2.1. Get
        result = mockMvc.perform(get("/v1/classifications/name/" + testClassification.getName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);
        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());

        // 3. displayName
        testClassification.withDisplayName("displayname change");
        result = mockMvc.perform(post("/v1/classifications/" + resClassification.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(testClassification)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(testClassification.getName(), resClassification.getName());
        Assertions.assertNotNull(resClassification.getVersion());
        Assertions.assertEquals(testClassification.getDisplayName(), resClassification.getDisplayName());
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());

        // 3.1. Get
        result = mockMvc.perform(get("/v1/classifications/name/" + testClassification.getName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);
        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(testClassification.getDescription(), resClassification.getDescription());
    }

    @Test
    @Order(3)
    void delete() throws Exception {
        // Create Data 01
        CreateClassification testClassification = new CreateClassification()
                        .withName("delete-test-01")
                        .withDescription("classification-test-01")
                        .withProvider(ProviderType.USER)
                        .withMutuallyExclusive(false);
        MvcResult result = mockMvc.perform(post("/v1/classifications").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).content(JsonUtils.pojoToJson(testClassification)))
                        .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        Classification deleteClassification_01 = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(deleteClassification_01.getId());
        
        // Create Data 02
        testClassification = new CreateClassification()
                        .withName("delete-test-02")
                        .withDescription("classification-test-02")
                        .withProvider(ProviderType.USER)
                        .withMutuallyExclusive(false);
        result = mockMvc.perform(post("/v1/classifications").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).content(JsonUtils.pojoToJson(testClassification)))
                        .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        Classification deleteClassification_02 = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertNotNull(deleteClassification_02.getName());
       
        // Delete By Id - Data 01
        result = mockMvc.perform(post("/v1/classifications/" + deleteClassification_01.getId() + "/delete")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

       
        // Delete By Id - Data 02
        result = mockMvc.perform(post("/v1/classifications/name/" + deleteClassification_02.getName() + "/delete")
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        // Get List And All Delete
        // result = mockMvc.perform(get("/v1/classifications")
        //                 .param("page", "0")
        //                 .param("size", "100")
        //                 .accept(MediaType.APPLICATION_JSON))
        //         .andExpect(status().isOk())
        //         .andReturn();

        // JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        // String code = node.get("code").asText();
        // Assertions.assertEquals("Success", code);

        // PageModel<Classification> pageResponse = JsonUtils.convertValue(node.get("data"),
        //         new TypeReference<PageModel<Classification>>() {});
        // Assertions.assertNotNull(pageResponse);
    }

    @Test
    @Order(4)
    void list() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/classifications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        String code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        PageModel<Classification> pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {});
        Assertions.assertNotNull(pageResponse);
        int currentSize = pageResponse.getTotalElements();
        if( currentSize <= 2 ) {
            CreateClassification testClassification = new CreateClassification()
                    .withName("list-test-01")
                    .withDescription("classification-test-01")
                    .withProvider(ProviderType.USER)
                    .withMutuallyExclusive(false);
            mockMvc.perform(post("/v1/classifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(JsonUtils.pojoToJson(testClassification)))
                    .andReturn();
            testClassification = new CreateClassification()
                    .withName("list-test-02")
                    .withDescription("classification-test-01")
                    .withProvider(ProviderType.USER)
                    .withMutuallyExclusive(false);
            mockMvc.perform(post("/v1/classifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(JsonUtils.pojoToJson(testClassification)))
                    .andReturn();
            testClassification = new CreateClassification()
                    .withName("list-test-03")
                    .withDescription("classification-test-01")
                    .withProvider(ProviderType.USER)
                    .withMutuallyExclusive(false);
            mockMvc.perform(post("/v1/classifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(JsonUtils.pojoToJson(testClassification)))
                    .andReturn();
            testClassification = new CreateClassification()
                    .withName("list-test-04")
                    .withDescription("classification-test-01")
                    .withProvider(ProviderType.USER)
                    .withMutuallyExclusive(false);
            mockMvc.perform(post("/v1/classifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(JsonUtils.pojoToJson(testClassification)))
                    .andReturn();
            currentSize += 4;
        }
        result = mockMvc.perform(get("/v1/classifications")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        code = node.get("code").asText();
        Assertions.assertEquals("Success", code);

        pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {});
        Assertions.assertEquals(currentSize, pageResponse.getTotalElements());
        Assertions.assertNotNull(pageResponse.getContents());
    }
}