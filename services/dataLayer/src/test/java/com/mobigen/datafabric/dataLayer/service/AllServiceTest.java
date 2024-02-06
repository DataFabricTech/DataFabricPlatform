package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.datafabric.dataLayer.service.jpaService.*;
import dto.*;
import dto.enums.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class AllServiceTest {
    @Autowired
    private ModelService modelService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private StorageMetadataSchemaService storageMetadataSchemaService;
    @Autowired
    private ModelMetadataSchemaService modelMetadataSchemaService;
    @Autowired
    private StorageAdaptorConnInfoSchemaService storageAdaptorConnInfoSchemaService;
    @Autowired
    private StorageAdaptorSchemaService storageAdaptorSchemaService;
    @Autowired
    private TagService tagService;
    @Autowired
    private DataTypeSchemaService dataTypeSchemaService;
    @Autowired
    private DataTypeOptionSchemaService dataTypeOptionSchemaService;

    @DisplayName("All insert test")
    @Test
    void allInsertTest() {
        var adaptorId = UUID.randomUUID();
        var storageId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var metadataId = UUID.randomUUID();
        var tagId = UUID.randomUUID();
        var modelId = UUID.randomUUID();
        var childModelId = UUID.randomUUID();
        var feedId = UUID.randomUUID();
        var parentFeedId = UUID.randomUUID();
        var resolvedId = UUID.randomUUID();
        var createdAt = LocalDateTime.now();

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
                .dataTypeOptionSchemaValue("example_data_type_option_value")
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

        assertDoesNotThrow(() -> {
            storageAdaptorSchemaService.save(storageAdaptorSchema);
            storageAdaptorConnInfoSchemaService.save(storageAdaptorConnInfoSchema);
            storageMetadataSchemaService.save(storageMetadataSchema);
            // --
            tagService.save(tag);
            // --
            storage.getDataAutoAdds().add(dataAutoAdd);
            storage.getStorageMetadatas().add(storageMetadata);

            storage.getStorageTags().add(storageTag);

            storage.getStorageConnInfo().add(storageConnInfo);

            storageService.save(storage);

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            dataTypeSchemaService.save(dataTypeSchema);
            dataTypeOptionSchemaService.save(dataTypeOptionSchema);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            modelMetadataSchemaService.save(modelMetadataSchema);
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

            modelService.save(model);
            modelService.save(childModel);
        });
    }

    @DisplayName("update relation things")
    @Test
    @Transactional
    void relationUpdate() {
        final LocalDateTime createdAt = LocalDateTime.now();
        final UUID adaptorId = UUID.randomUUID();
        final UUID storageId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
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

            var storages = new ArrayList<Storage>();
            storages.add(storage);
            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.MARIADB)
                    .enable(false)
                    .storage(storages)
                    .build();

            storageAdaptorSchemaService.save(storageAdaptorSchema);
        });

        assertDoesNotThrow(() -> {
            var found = storageAdaptorSchemaService.findById(adaptorId).get();
            var target = found.getStorage().stream().filter(storage -> storage.generateKey().equals(storageId))
                    .findFirst().orElse(null);

            var update = target.toBuilder().name("updated name").build();

            found.getStorage().set(found.getStorage().indexOf(target), update);

            storageAdaptorSchemaService.update(found);
        });

        assertDoesNotThrow(() -> {
            var found = storageAdaptorSchemaService.findById(adaptorId).get();

            assertEquals(1, found.getStorage().size());
            assertEquals("updated name", found.getStorage().get(0).getName());
        });
    }
}
