package com.mobigen.vdap.server.tags;

import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

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
        tagRepository.deleteAll();
    }

    @Test
    @Order(1)
    void create() throws Exception {
        Classification classification = createClassification("tag-test-classification",
                "classification for tag", "for tag unittest");

        // 정상 케이스
        CreateTag request = new CreateTag()
                .withClassification(classification.getId())
                .withName("tag-create-test-01")
                .withDisplayName("tag-create-test-01-displayName")
                .withDescription("tag-create-test-01-desc")
                .withProvider(ProviderType.USER);
        MvcResult result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        Tag resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertNotNull(resTag.getId());
        Assertions.assertEquals(request.getName(), resTag.getName());
        Assertions.assertEquals(request.getDescription(), resTag.getDescription());
        Assertions.assertEquals(request.getDisplayName(), resTag.getDisplayName());
        Assertions.assertNotNull(resTag.getUpdatedAt());
        Assertions.assertNotNull(resTag.getUpdatedBy());
        Assertions.assertNotNull(resTag.getHref());

        // Duplicate
        result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        // classification not found
        CreateTag errRequest = new CreateTag()
                .withClassification(Utilities.generateUUID())
                .withName("tag-create-test-02")
                .withDisplayName("tag-create-test-02-displayName")
                .withDescription("tag-create-test-02-desc")
                .withProvider(ProviderType.USER);
        result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(errRequest)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        request = new CreateTag()
                .withClassification(classification.getId())
                .withName("tag-create-test-02")
                .withDisplayName("tag-create-test-02-displayName")
                .withDescription("tag-create-test-02-desc")
                .withProvider(ProviderType.USER);
        result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertNotNull(resTag.getId());
        Assertions.assertEquals(request.getName(), resTag.getName());
        Assertions.assertEquals(request.getDescription(), resTag.getDescription());
        Assertions.assertEquals(request.getDisplayName(), resTag.getDisplayName());
        Assertions.assertNotNull(resTag.getUpdatedAt());
        Assertions.assertNotNull(resTag.getUpdatedBy());
        Assertions.assertNotNull(resTag.getHref());
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