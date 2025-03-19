package com.mobigen.vdap.server.tags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
import com.mobigen.vdap.server.repositories.TagUsageRepository;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Autowired
    private ClassificationRepository classificationRepository;
    @Autowired
    private EntityExtensionRepository entityExtensionRepository;
    @Autowired
    private TagUsageRepository tagUsageRepository;

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
        tagUsageRepository.deleteAll();
        entityExtensionRepository.deleteAll();
        tagRepository.deleteAll();
        classificationRepository.deleteAll();
    }

    @Test
    @Order(1)
    void create() throws Exception {
        Classification classification = createClassification("tag-test-classification",
                "classification for tag", "for tag unittest");

        // 정상 케이스
        String name = "tag-create-test-01";
        String displayName = "tag-create-test-01-displayName";
        String desc = "tag-create-test-01-desc";
        Tag tag = createTag(classification, name, displayName, desc);

        Assertions.assertNotNull(tag.getId());
        Assertions.assertEquals(name, tag.getName());
        Assertions.assertEquals(displayName, tag.getDisplayName());
        Assertions.assertEquals(desc, tag.getDescription());
        Assertions.assertNotNull(tag.getUpdatedAt());
        Assertions.assertNotNull(tag.getUpdatedBy());
        Assertions.assertNotNull(tag.getHref());

        // Duplicate
        CreateTag request = new CreateTag()
                .withClassification(classification.getId())
                .withName(name)
                .withDisplayName(displayName)
                .withDescription(desc)
                .withProvider(ProviderType.USER);
        MvcResult result = mockMvc.perform(post("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(request)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
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

        // create tag 2
        name = "tag-create-test-02";
        displayName = "tag-create-test-02-displayName";
        desc = "tag-create-test-02-desc";
        tag = createTag(classification, name, displayName, desc);

        tag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertNotNull(tag.getId());
        Assertions.assertEquals(name, tag.getName());
        Assertions.assertEquals(displayName, tag.getDisplayName());
        Assertions.assertEquals(desc, tag.getDescription());
        Assertions.assertNotNull(tag.getUpdatedAt());
        Assertions.assertNotNull(tag.getUpdatedBy());
        Assertions.assertNotNull(tag.getHref());
    }

    @Test
    @Order(2)
    void update() throws Exception {
        Classification classification = createClassification(
                "tag-update-test-classification",
                "classification for tag",
                "for tag unittest");
        String name = "tag-update-test-01";
        String displayName = "tag-update-test-01-displayName";
        String desc = "tag-update-test-01-desc";
        Tag tag = createTag(classification, name, displayName, desc);

        // Name Update
        String updatedName = "tag-update-test-01-updated";
        CreateTag updateTag = new CreateTag()
                .withClassification(classification.getId())
                .withName(updatedName)
                .withDisplayName(displayName)
                .withDescription(desc)
                .withProvider(ProviderType.USER);
        MvcResult result = mockMvc.perform(post("/v1/tags/" + tag.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateTag)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Tag resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertEquals(updatedName, resTag.getName());
        Assertions.assertEquals(displayName, resTag.getDisplayName());
        Assertions.assertEquals(desc, resTag.getDescription());
        Assertions.assertNotNull(resTag.getChangeDescription());

        // version list
        result = mockMvc.perform(get("/v1/tags/" + tag.getId() + "/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        EntityHistory entityHistory = JsonUtils.convertValue(node.get("data"), EntityHistory.class);
        Assertions.assertEquals("tag", entityHistory.getEntityType());
        Assertions.assertEquals(2, entityHistory.getVersions().size());
        Tag oldTag = JsonUtils.readValue((String) entityHistory.getVersions().getLast(), Tag.class);
        Assertions.assertEquals(name, oldTag.getName());
        Assertions.assertEquals(displayName, oldTag.getDisplayName());
        Assertions.assertEquals(desc, oldTag.getDescription());

        // get version
        result = mockMvc.perform(get("/v1/tags/" + tag.getId() + "/versions/" + resTag.getVersion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Tag latestTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertEquals(updatedName, latestTag.getName());
        Assertions.assertEquals(displayName, latestTag.getDisplayName());
        Assertions.assertEquals(desc, latestTag.getDescription());
        Assertions.assertNotNull(latestTag.getChangeDescription());

        // update displayName
        String updatedDisplayName = "tag-update-test-01-updated-displayName";
        CreateTag updateTag2 = new CreateTag()
                .withClassification(classification.getId())
                .withName(updatedName)
                .withDisplayName(updatedDisplayName)
                .withDescription(desc)
                .withProvider(ProviderType.USER);
        result = mockMvc.perform(post("/v1/tags/" + tag.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.pojoToJson(updateTag2)))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertEquals(updatedName, resTag.getName());
        Assertions.assertEquals(updatedDisplayName, resTag.getDisplayName());
        Assertions.assertEquals(desc, resTag.getDescription());
        Assertions.assertNotNull(resTag.getChangeDescription());

        // delete tag and check extension data
        result = mockMvc.perform(post("/v1/tags/" + tag.getId() + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        // Extension(version history) 삭제 확인
        String versionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.TAG);
        List<EntityExtension> extensions  =
                entityExtensionRepository.findByIdAndExtensionStartingWith(tag.getId().toString(),
                        versionPrefix + ".", Sort.by(Sort.Order.asc("extension")));
        Assertions.assertEquals(0, extensions.size());
    }

    @Test
    @Order(3)
    void getAndList() throws Exception {
        Classification classification = createClassification(
                "tag-get-test-classification",
                "classification for tag",
                "for tag unittest");
        String name = "tag-get-test-01";
        String displayName = "tag-get-test-01-displayName";
        String desc = "tag-get-test-01-desc";
        Tag tag = createTag(classification, name, displayName, desc);

        // get
        MvcResult result = mockMvc.perform(get("/v1/tags/" + tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Tag resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertEquals(tag.getId(), resTag.getId());
        Assertions.assertEquals(name, resTag.getName());
        Assertions.assertEquals(displayName, resTag.getDisplayName());
        Assertions.assertEquals(desc, resTag.getDescription());
        Assertions.assertNotNull(resTag.getUpdatedAt());
        Assertions.assertNotNull(resTag.getUpdatedBy());
        Assertions.assertNotNull(resTag.getHref());

        // add tag 2
        name = "tag-get-test-02";
        displayName = "tag-get-test-02-displayName";
        desc = "tag-get-test-02-desc";
        Tag tag2 = createTag(classification, name, displayName, desc);

        // get
        result = mockMvc.perform(get("/v1/tags/" + tag2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resTag = JsonUtils.convertValue(node.get("data"), Tag.class);
        Assertions.assertEquals(tag2.getId(), resTag.getId());
        Assertions.assertEquals(name, resTag.getName());
        Assertions.assertEquals(displayName, resTag.getDisplayName());
        Assertions.assertEquals(desc, resTag.getDescription());
        Assertions.assertNotNull(resTag.getUpdatedAt());
        Assertions.assertNotNull(resTag.getUpdatedBy());
        Assertions.assertNotNull(resTag.getHref());

        // not found
        result = mockMvc.perform(get("/v1/tags/" + Utilities.generateUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        // add tag 3
        name = "tag-get-test-03";
        displayName = "tag-get-test-03-displayName";
        desc = "tag-get-test-03-desc";
        Tag tag3 = createTag(classification, name, displayName, desc);

        // list
        result = mockMvc.perform(get("/v1/tags")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        PageModel<Tag> pageResponse = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Tag>>() {
                });
        Assertions.assertEquals(0, pageResponse.getPage());
        Assertions.assertEquals(2, pageResponse.getSize());
        Assertions.assertEquals(2, pageResponse.getTotalPages());
        Assertions.assertEquals(3, pageResponse.getTotalElements());
        Assertions.assertNotNull(pageResponse.getContents());

        List<Tag> tagList = pageResponse.getContents();
        Assertions.assertEquals(2, tagList.size());
        // for loop tagList and check name, displayName, description
        for (Tag t : tagList) {
            if (t.getId().equals(tag.getId())) {
                Assertions.assertEquals(tag.getId(), t.getId());
                Assertions.assertEquals(tag.getName(), t.getName());
                Assertions.assertEquals(tag.getDisplayName(), t.getDisplayName());
                Assertions.assertEquals(tag.getDescription(), t.getDescription());
            } else if (t.getId().equals(tag2.getId())) {
                Assertions.assertEquals(tag2.getId(), t.getId());
                Assertions.assertEquals(tag2.getName(), t.getName());
                Assertions.assertEquals(tag2.getDisplayName(), t.getDisplayName());
                Assertions.assertEquals(tag2.getDescription(), t.getDescription());
            }
        }
    }

    @Test
    @Order(4)
    void tagAndClassificationRelationship() throws Exception {
        // create classification
        Classification classification = createClassification(
                "tag-get-test-classification",
                "classification for tag",
                "for tag unittest");

        // create tag
        Tag tag = createTag(classification, "tag-get-test-01",
                "tag-get-test-01-displayName",
                "tag-get-test-01-desc");

        // get classification with terms
        MvcResult result = mockMvc.perform(get("/v1/classifications/" + classification.getId())
                        .param("fields", "termCount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // result check
        JsonNode node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        Classification resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(classification.getId(), resClassification.getId());
        Assertions.assertEquals(1, resClassification.getTermCount());

        // get classification with terms
        result = mockMvc.perform(get("/v1/classifications/" + classification.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // result check
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(classification.getId(), resClassification.getId());
        Assertions.assertNull(resClassification.getTermCount());
        Assertions.assertNull(resClassification.getUsageCount());

        // get list and check term count
        result = mockMvc.perform(get("/v1/classifications")
                        .param("page", "0")
                        .param("size", "2")
                        .param("fields", "termCount")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        PageModel<Classification> classificationPage = JsonUtils.convertValue(node.get("data"),
                new TypeReference<PageModel<Classification>>() {
                });
        Assertions.assertNotNull(classificationPage.getContents());
        List<Classification> classificationList = classificationPage.getContents();
        // for loop classificationList and check terms count and tag info
        for (Classification c : classificationList) {
            if (c.getId().equals(classification.getId())) {
                Assertions.assertEquals(1, c.getTermCount());
            }
        }

        // Delete tag
        result = mockMvc.perform(post("/v1/tags/" + tag.getId() + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        // Check classification term count
        result = mockMvc.perform(get("/v1/classifications/" + classification.getId())
                        .param("fields", "termCount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());
        resClassification = JsonUtils.convertValue(node.get("data"), Classification.class);
        Assertions.assertEquals(0, resClassification.getTermCount());

        // Recreate tag
        tag = createTag(classification, "tag-get-test-01",
                "tag-get-test-01-displayName",
                "tag-get-test-01-desc");

        // TODO : update tag and add relationship(usage)

        // Delete Classification
        result = mockMvc.perform(post("/v1/classifications/" + classification.getId() + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Success", node.get("code").asText());

        // Get Tag And Fail
        result = mockMvc.perform(get("/v1/tags/" + tag.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        node = JsonUtils.readTree(result.getResponse().getContentAsString());
        Assertions.assertEquals("Error", node.get("code").asText());

        // TODO : Check Tag Extension And Tag Usage
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
        Assertions.assertEquals("Success", node.get("code").asText());
        return JsonUtils.convertValue(node.get("data"), Classification.class);
    }

    public Tag createTag(Classification classification, String name, String displayName, String description) throws Exception {
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