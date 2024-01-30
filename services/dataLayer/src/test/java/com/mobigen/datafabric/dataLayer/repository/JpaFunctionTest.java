package com.mobigen.datafabric.dataLayer.repository;

import com.mobigen.datafabric.dataLayer.jpa.JpaAgent;
import dto.*;
import dto.enums.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JpaFunctionTest {

    @DisplayName("storage insert test success case")
    @Test
    void storageInsertTest() {
        final LocalDateTime createdAt = LocalDateTime.now();
        final UUID adaptorId = UUID.randomUUID();
        final UUID storageId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var em = new JpaAgent().getEm();
            em.getTransaction().begin();

            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.MARIADB)
                    .enable(false)
                    .build();
            var storage = Storage.builder()
                    .storageId(storageId)
                    .adaptorId(adaptorId)
                    .name("example_storage_name")
                    .description("example_stroage_description")
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(createdAt)
                    .modifiedBy(userId)
                    .status(StatusType.SUCCESS)
                    .lastSyncAt(null)
                    .lastMonitoringAt(null)
                    .syncEnable(true)
                    .syncTime("example_storage_sync_time")
                    .monitoringEnable(false)
                    .monitoringPeriod(1)
                    .monitoringFailThreshold(1)
                    .build();

            StorageAdaptorSchemaRepository storageAdaptorSchemaRepository = new StorageAdaptorSchemaRepository(em);
            StorageRepository storageRepository = new StorageRepository(em);

            storageAdaptorSchemaRepository.insert(storageAdaptorSchema);
            storageRepository.insert(storage);

            em.getTransaction().commit();
            em.close();
        });
    }

    @DisplayName("storage insert without storageAdaptorSchema test fail case")
    @Test
    void storageInsertFailTest() {
        final UUID adaptorId = UUID.randomUUID();
        final UUID storageId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final LocalDateTime createdAt = LocalDateTime.now();
        assertThrows(RollbackException.class, () -> {
            var em = new JpaAgent().getEm();
            em.getTransaction().begin();

            var storage = Storage.builder()
                    .storageId(storageId)
                    .adaptorId(adaptorId)
                    .name("example_storage_name")
                    .description("example_stroage_description")
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(createdAt)
                    .modifiedBy(userId)
                    .status(StatusType.SUCCESS)
                    .lastSyncAt(null)
                    .lastMonitoringAt(null)
                    .syncEnable(true)
                    .syncTime("example_storage_sync_time")
                    .monitoringEnable(false)
                    .monitoringPeriod(1)
                    .monitoringFailThreshold(1)
                    .build();

            StorageRepository storageRepository = new StorageRepository(em);

            storageRepository.insert(storage);

            em.getTransaction().commit();
            em.close();
        });
    }

    @DisplayName("findByKey Success Test")
    @Test
    void findByKey() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var found = storageMetadataSchemaRepository.findByKey(metadataId);
            assertEquals("example_storage_metadata_schema", found.getName());
        });
    }

    @DisplayName("findByKey not exist key fail Test")
    @Test
    void findByKeyWithNotExistKey() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            assertNull(storageMetadataSchemaRepository.findByKey(metadataId));
        });
    }

    @DisplayName("findAll Success Test")
    @Test
    void findAll() {
        final UUID adaptorId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageAdaptorSchemaRepository storageAdaptorSchemaRepository = new StorageAdaptorSchemaRepository(em);

            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.MARIADB)
                    .enable(false)
                    .build();

            storageAdaptorSchemaRepository.insert(storageAdaptorSchema);
            em.getTransaction().commit();
            em.clear();

            var found = storageAdaptorSchemaRepository.findByKey(adaptorId);
            assertEquals("example_storage_adaptor_schema_name", found.getName());
        });
    }

    @DisplayName("findAll Success Test")
    @Test
    void findAllNotExistItems() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            assertNull(storageMetadataSchemaRepository.findByKey(metadataId));
        });
    }

    @DisplayName("findWhere Object Success Test")
    @Test
    void findWhere() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var found = storageMetadataSchemaRepository.findWhere("metadataId", metadataId);

            assertEquals("example_storage_metadata_schema", found.get(0).getName());
        });
    }

    @DisplayName("findWhere String value test success test")
    @Test
    void findStringWhere() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var found = storageMetadataSchemaRepository.findWhere("name", "example_storage_metadata_schema");

            assertEquals("example_storage_metadata_schema", found.get(0).getName());
        });
    }

    @DisplayName("findLike String with wildCard Success test")
    @Test
    void findLikeFullSpelling() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var found = storageMetadataSchemaRepository.findLike("name", "example_storage_metadata_schema");

            assertEquals("example_storage_metadata_schema", found.get(0).getName());
        });
    }

    @DisplayName("findLike String with WildCard Success test")
    @Test
    void findLikeWithWildCard() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var found = storageMetadataSchemaRepository.findLike("name", "%storage%");

            assertEquals("example_storage_metadata_schema", found.get(0).getName());
        });
    }

    @DisplayName("findLike String without WildCard&FullSpelling Success test")
    @Test
    void findLikeWithoutWildCardAndFullSpelling() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            assertEquals(0, storageMetadataSchemaRepository.findLike("name", "storage").size());
        });
    }

    @DisplayName("delete using entity success test")
    @Test
    void deleteByEntity() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var foundEntity = storageMetadataSchemaRepository.findByKey(metadataId);

            storageMetadataSchemaRepository.delete(foundEntity);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            assertNull(storageMetadataSchemaRepository.findByKey(metadataId));
        });
    }

    @DisplayName("delete using key success test")
    @Test
    void deleteByKey() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            storageMetadataSchemaRepository.deleteByKey(metadataId);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            assertNull(storageMetadataSchemaRepository.findByKey(metadataId));
        });
    }

    @DisplayName("delete empty entity sucess test")
    @Test
    void deleteEmptyEntityUsingEntity() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            storageMetadataSchemaRepository.delete(storageMetadataSchema);
        });
    }


    @DisplayName("delete empty key fail test")
    @Test
    void deleteEmptyEntityUsingKey() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            storageMetadataSchemaRepository.deleteByKey(metadataId);
        });
    }

    @DisplayName("update name update success case")
    @Test
    void updateDefault() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();
            var found = storageMetadataSchemaRepository.findByKey(storageMetadataSchema.generateKey());
            found = found.toBuilder().name("updateName").build();

            storageMetadataSchemaRepository.update(found);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            found = storageMetadataSchemaRepository.findByKey(storageMetadataSchema.generateKey());
            assertEquals("updateName", found.getName());
        });
    }

    @DisplayName("update update empty entity fail case")
    @Test
    void updateEmptyEntity() {
        final UUID metadataId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchema = storageMetadataSchema.toBuilder().name("updateName").build();

            storageMetadataSchemaRepository.update(storageMetadataSchema);
        });
    }

    @Test
    void executeJQuery() {
        final UUID metadataId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();

            var str = "select e from StorageMetadataSchema e where e.name = 'example_storage_metadata_schema'";
            var found = storageMetadataSchemaRepository.executeJQuery(str);
            assertEquals("example_storage_metadata_schema", found.get(0).getName());
        });
    }

    @Test
    void AllInsertTest() {
        final LocalDateTime createdAt = LocalDateTime.now();
        final UUID resolvedId = UUID.randomUUID();
        final UUID parentFeedId = UUID.randomUUID();
        final UUID feedId = UUID.randomUUID();
        final UUID tagId = UUID.randomUUID();
        final UUID adaptorId = UUID.randomUUID();
        final UUID storageId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID modelId = UUID.randomUUID();
        final UUID metadataId = UUID.randomUUID();
        final UUID childModelId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var em = new JpaAgent().getEm();

            var modelRepo = new ModelRepository(em);
            var storageRepo = new StorageRepository(em);
            var storageMetaSchemaRepo = new StorageMetadataSchemaRepository(em);
            var modelMetadataSchemaRepo = new ModelMetadataSchemaRepository(em);
            var storageAdaptorConnInfoSchemaRepo = new StorageAdaptorConnInfoSchemaRepository(em);
            var storageAdaptorSchemaRepo = new StorageAdaptorSchemaRepository(em);
            var tagRepo = new TagRepository(em);
            var dataTypeSchemaRepo = new DataTypeSchemaRepository(em);
            var dataTypeOptionSchemaRepo = new DataTypeOptionSchemaRepository(em);

            em.getTransaction().begin();

            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.MARIADB)
                    .enable(false)
                    .build();

            var storageAdaptorConnInfoSchema = StorageAdaptorConnInfoSchema.builder()
                    .adaptorId(adaptorId)
                    .type("example_storage_adaptor_conn_info_schema_type")
                    .adaptorConnSchemaKey("example_storage_adaptor_conn_info_schema_key")
                    .adaptorConnSchemaValue("example_storage_adaptor_conn_info_schema_value")
                    .valueType(ValueType.BOOLEAN)
                    .defaultValue("example_storage_adaptor_conn_info_schema_default_value")
                    .description("example_storage_adaptor_conn_info_schema_description")
                    .required(false)
                    .build();

            var storage = Storage.builder()
                    .storageId(storageId)
                    .adaptorId(adaptorId)
                    .name("example_storage_name")
                    .description("example_stroage_description")
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(createdAt)
                    .modifiedBy(userId)
                    .status(StatusType.SUCCESS)
                    .lastSyncAt(null)
                    .lastMonitoringAt(null)
                    .syncEnable(true)
                    .syncTime("example_storage_sync_time")
                    .monitoringEnable(false)
                    .monitoringPeriod(1)
                    .monitoringFailThreshold(1)
                    .build();

            var dataAutoAdd = DataAutoAdd.builder()
                    .storageId(storageId)
                    .num(1)
                    .regex("example_data_auto_add_regex")
                    .formatType(FormatType.TABLE)
                    .build();

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            var storageMetadata = StorageMetadata.builder()
                    .storageId(storageId)
                    .metadataId(metadataId)
                    .metadataValue("example_storage_metadata_value")
                    .build();

            var storageTag = StorageTag.builder()
                    .storageId(storageId)
                    .tagId(tagId)
                    .build();

            var storageConnInfo = StorageConnInfo.builder()
                    .storageId(storageId)
                    .type("example_storage_conn_info_type")
                    .storageConnKey("example_storage_conn_info_key")
                    .storageConnValue("example_storage_conn_info_value")
                    .isOption(false)
                    .build();

            var dataTypeSchema = DataTypeSchema.builder()
                    .dataType(DataType.CSV)
                    .name("csv")
                    .build();

            var dataTypeOptionSchema = DataTypeOptionSchema.builder()
                    .dataType(DataType.CSV)
                    .dataTypeOptionSchemaKey("example_data_type_option_schema")
                    .dataTypeOptionSchemaKey("example_data_type_option_value")
                    .valueType(ValueType.STRING)
                    .defaultValue("example_data_type_option_default_value")
                    .description("example_data_type_option_schema_description")
                    .build();

            var model = Model.builder()
                    .modelId(modelId)
                    .name("example_model_name")
                    .description("example_model_description")
                    .dataType(DataType.CSV)
                    .storageId(storageId)
                    .status(StatusType.SUCCESS)
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(null)
                    .modifiedBy(null)
                    .syncEnable(false)
                    .syncTime(null)
                    .syncAt(null)
                    .build();

            var dataTypeOption = DataTypeOption.builder()
                    .modelId(modelId)
                    .dataTypeOptionKey("example_data_type_option_key")
                    .dataTypeOptionValue("example_data_type_option_value")
                    .build();

            var childModel = Model.builder()
                    .modelId(childModelId)
                    .name("example_child_model_name")
                    .description("example_child_model_description")
                    .dataType(DataType.CSV)
                    .storageId(storageId)
                    .status(StatusType.SUCCESS)
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(null)
                    .modifiedBy(null)
                    .syncEnable(false)
                    .syncTime(null)
                    .syncAt(null)
                    .build();

            var relation = ModelRelation.builder()
                    .modelId(modelId)
                    .childModelId(childModelId)
                    .build();

            var modelMetadataSchema = ModelMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_model_metadata_schema_name")
                    .description("example_model_metadata_schema_description")
                    .build();

            var modelMetadata = ModelMetadata.builder()
                    .modelId(modelId)
                    .metadataId(metadataId)
                    .metadataValue("example_model_metadata_value")
                    .build();

            var nameColumnMetadata = ColumnMetadata.builder()
                    .modelId(modelId)
                    .num(1)
                    .name("name")
                    .description("example_name_description")
                    .columnType(ColumnType.VARCHAR)
                    .length(2147483647L)
                    .isPK(true)
                    .isFK(false)
                    .nullable(false)
                    .build();

            var addressColumnMetadata = ColumnMetadata.builder()
                    .modelId(modelId)
                    .num(2)
                    .name("address")
                    .description("example_address_description")
                    .columnType(ColumnType.VARCHAR)
                    .length(2147483647L)
                    .isPK(false)
                    .isFK(false)
                    .nullable(true)
                    .build();

            var testColumnMetadata = ColumnMetadata.builder()
                    .modelId(modelId)
                    .num(3)
                    .name("test")
                    .description("example_test_description")
                    .columnType(ColumnType.INT4)
                    .length(2147483647L)
                    .isPK(false)
                    .isFK(false)
                    .nullable(true)
                    .build();

            var tableDataQuality = TableDataQuality.builder()
                    .modelId(modelId)
                    .num(3)
                    .qualityType(QualityType.MIN)
                    .qualityValue(10)
                    .build();

            var dataSample = DataSample.builder()
                    .modelId(modelId)
                    .formatType(FormatType.TABLE)
                    .filePath("exmaple_data_sample_file_path")
                    .build();

            var modelRatingAndComment = ModelRatingAndComment.builder()
                    .modelId(modelId)
                    .userId(userId)
                    .rating(5)
                    .comments("example_model_rating_and_comment")
                    .createdAt(createdAt)
                    .build();

            var modelTag = ModelTag.builder()
                    .modelId(modelId)
                    .tagId(tagId)
                    .build();

            var tag = Tag.builder()
                    .tagId(tagId)
                    .tagValue("example_tag")
                    .build();

            var modelFeedback = ModelFeedback.builder()
                    .modelId(modelId)
                    .feedId(feedId)
                    .parentFeedId(parentFeedId)
                    .userId(userId)
                    .time(createdAt)
                    .title("example_model_feedback_title")
                    .body("example_model_feedback_body")
                    .isResolved(false)
                    .resolvedUserId(resolvedId)
                    .resolvedTime(createdAt)
                    .build();

            storageAdaptorSchemaRepo.insert(storageAdaptorSchema);
            storageAdaptorConnInfoSchemaRepo.insert(storageAdaptorConnInfoSchema);
            storageMetaSchemaRepo.insert(storageMetadataSchema);
            ;
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            tagRepo.insert(tag);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            storage.getDataAutoAdds().add(dataAutoAdd);
            storage.getStorageMetadatas().add(storageMetadata);

            storage.getStorageTags().add(storageTag);

            storage.getStorageConnInfo().add(storageConnInfo);

            storageRepo.insert(storage);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            dataTypeSchemaRepo.insert(dataTypeSchema);
            dataTypeOptionSchemaRepo.insert(dataTypeOptionSchema);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            modelMetadataSchemaRepo.insert(modelMetadataSchema);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            model.getModelRelations().add(relation);
            model.getModelMetadatas().add(modelMetadata);

            testColumnMetadata.getTableDataQualities().add(tableDataQuality);

            model.getColumnMetadatas().add(nameColumnMetadata);
            model.getColumnMetadatas().add(addressColumnMetadata);
            model.getColumnMetadatas().add(testColumnMetadata);

            model.getDataSamples().add(dataSample);

            model.getModelRatingAndComments().add(modelRatingAndComment);

            model.getModelTags().add(modelTag);

            model.getModelFeedbacks().add(modelFeedback);

            model.getDataTypeOptions().add(dataTypeOption);

            modelRepo.insert(model);
            modelRepo.insert(childModel);

            em.getTransaction().commit();
            em.close();
        });
    }

    @Test
    void agentMultiThreadingTest() {
        var jpaAgent = new JpaAgent();
        var em = jpaAgent.getEm();

        var thread = new Thread(() -> {
            var threadJpaConfig = new JpaAgent();
            var threadEm = threadJpaConfig.getEm();

            assertNotEquals(threadEm, em);
        });
        thread.start();
    }

    @DisplayName("multi threading with two same repository success test")
    @Test
    void multiThreadingWithTwoRepository() {
        final UUID metadataId = UUID.randomUUID();
        final UUID multiMetadataId = UUID.randomUUID();
        var firstThread = new Thread(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(metadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.close();
        });

        var secondThread = new Thread(() -> {
            var jpaAgent = new JpaAgent();
            var em = jpaAgent.getEm();
            em.getTransaction().begin();

            StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

            var storageMetadataSchema = StorageMetadataSchema.builder()
                    .metadataId(multiMetadataId)
                    .name("example_storage_metadata_schema")
                    .description("example_storage_metadata_schema_description")
                    .build();

            storageMetadataSchemaRepository.insert(storageMetadataSchema);
            em.getTransaction().commit();
            em.close();
        });

        assertDoesNotThrow(() -> {
            firstThread.start();
            secondThread.start();
        });
    }

    @DisplayName("multi threading with two same repository and same key fail test")
    @Test
    void multiThreadingWithSameKeyRepository() {
        final UUID metadataId = UUID.randomUUID();
        var firstThread = new Thread(() -> {
            try {
                var jpaAgent = new JpaAgent();
                var em = jpaAgent.getEm();
                em.getTransaction().begin();

                StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

                var storageMetadataSchema = StorageMetadataSchema.builder()
                        .metadataId(metadataId)
                        .name("example_storage_metadata_schema")
                        .description("example_storage_metadata_schema_description")
                        .build();

                storageMetadataSchemaRepository.insert(storageMetadataSchema);
                em.getTransaction().commit();
                em.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var secondThread = new Thread(() -> {
            try {
                var jpaAgent = new JpaAgent();
                var em = jpaAgent.getEm();
                em.getTransaction().begin();

                StorageMetadataSchemaRepository storageMetadataSchemaRepository = new StorageMetadataSchemaRepository(em);

                var storageMetadataSchema = StorageMetadataSchema.builder()
                        .metadataId(metadataId)
                        .name("example_storage_metadata_schema")
                        .description("example_storage_metadata_schema_description")
                        .build();

                storageMetadataSchemaRepository.insert(storageMetadataSchema);
                em.getTransaction().commit();
                em.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertThrows(RuntimeException.class, () -> {
            firstThread.run();
            secondThread.run();
        });
    }

    @DisplayName("generate key success test")
    @Test
    void testGenerateKey() {
        final UUID metadataId = UUID.randomUUID();
        var storageMetadataSchema = StorageMetadataSchema.builder()
                .metadataId(metadataId)
                .name("example_storage_metadata_schema")
                .description("example_storage_metadata_schema_description")
                .build();

        assertEquals(metadataId, storageMetadataSchema.generateKey());
    }

    @DisplayName("update relation things")
    @Test
    void relationUpdate() {
        final LocalDateTime createdAt  = LocalDateTime.now();
        final UUID adaptorId = UUID.randomUUID();
        final UUID storageId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final EntityManager em = new JpaAgent().getEm();
        assertDoesNotThrow(() -> {
            em.getTransaction().begin();

            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.MARIADB)
                    .enable(false)
                    .build();
            var storage = Storage.builder()
                    .storageId(storageId)
                    .adaptorId(adaptorId)
                    .name("example_storage_name")
                    .description("example_stroage_description")
                    .createdAt(createdAt)
                    .createdBy(userId)
                    .modifiedAt(createdAt)
                    .modifiedBy(userId)
                    .status(StatusType.SUCCESS)
                    .lastSyncAt(null)
                    .lastMonitoringAt(null)
                    .syncEnable(true)
                    .syncTime("example_storage_sync_time")
                    .monitoringEnable(false)
                    .monitoringPeriod(1)
                    .monitoringFailThreshold(1)
                    .build();

            StorageAdaptorSchemaRepository storageAdaptorSchemaRepository = new StorageAdaptorSchemaRepository(em);
            StorageRepository storageRepository = new StorageRepository(em);

            storageAdaptorSchemaRepository.insert(storageAdaptorSchema);
            storageRepository.insert(storage);

            em.getTransaction().commit();
            em.clear();
        });

        assertDoesNotThrow(() -> {
            em.getTransaction().begin();

            StorageAdaptorSchemaRepository storageAdaptorSchemaRepository = new StorageAdaptorSchemaRepository(em);
            var found = storageAdaptorSchemaRepository.findByKey(adaptorId);
            var target = found.getStorage().stream().filter(storage -> storage.generateKey().equals(storageId))
                    .findFirst().orElse(null);

            var update = target.toBuilder().name("updated name").build();

            found.getStorage().set(found.getStorage().indexOf(target), update);

            storageAdaptorSchemaRepository.update(found);
            em.getTransaction().commit();
            em.clear();
        });

        assertDoesNotThrow(() -> {
            em.getTransaction().begin();

            StorageAdaptorSchemaRepository storageAdaptorSchemaRepository = new StorageAdaptorSchemaRepository(em);
            var found = storageAdaptorSchemaRepository.findByKey(adaptorId);

            assertEquals(1, found.getStorage().size());
            assertEquals("updated name", found.getStorage().get(0).getName());
        });
    }
}