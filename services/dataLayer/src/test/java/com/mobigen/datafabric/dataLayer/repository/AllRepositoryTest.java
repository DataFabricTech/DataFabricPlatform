package com.mobigen.datafabric.dataLayer.repository;

import dto.*;
import dto.compositeKeys.DataTypeOptionSchemaKey;
import dto.compositeKeys.StorageAdaptorConnInfoSchemaKey;
import dto.enums.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

public class AllRepositoryTest {

    @AfterAll
    static void delete() {
        try (
                var modelRepo = new ModelRepository();
                var storageRepo = new StorageRepository();
                var storageMetaSchemaRepo = new StorageMetadataSchemaRepository();
                var modelMetadataSchemaRepo = new ModelMetadataSchemaRepository();
                var storageAdaptorConnInfoSchemaRepo = new StorageAdaptorConnInfoSchemaRepository();
                var storageAdaptorSchemaRepo = new StorageAdaptorSchemaRepository();
                var tagRepo = new TagRepository();
                var dataTypeSchemaRepo = new DataTypeSchemaRepository();
                var dataTypeOptionSchemaRepo = new DataTypeOptionSchemaRepository();
        ) {
            var adaptorId = UUID.fromString("9df7ac96-90fa-481d-b18d-e3ca09ec95eb");
            var storageId = UUID.fromString("f29d3d75-59e8-4ae6-9902-53869db21d8c");
            var metadataId = UUID.fromString("c518ae33-aec9-42d4-b634-56b4871ec73d");
            var tagId = UUID.fromString("13343db2-a252-4b3a-8e7c-e3ee8e74507a");
            var modelId = UUID.fromString("7a481647-8eb6-4466-8d58-90e2137a6202");

            modelRepo.deleteByKey(modelId);
            storageRepo.deleteByKey(storageId);
            storageMetaSchemaRepo.deleteByKey(metadataId);
            modelMetadataSchemaRepo.deleteByKey(metadataId);
            StorageAdaptorConnInfoSchemaKey sacisk = StorageAdaptorConnInfoSchemaKey.builder().adaptorId(adaptorId)
                    .type("example_storage_adaptor_conn_info_schema_type")
                    .key("example_storage_adaptor_conn_info_schema_key")
                    .build();
            storageAdaptorConnInfoSchemaRepo.deleteByKey(sacisk);

            storageAdaptorSchemaRepo.deleteByKey(adaptorId);

            tagRepo.deleteByKey(tagId);
            dataTypeSchemaRepo.deleteByKey(DataType.CSV);
            dataTypeOptionSchemaRepo.deleteByKey(DataTypeOptionSchemaKey.builder()
                    .dataType(DataType.CSV)
                    .key("example_data_type_option_schema")
                    .build());
        } catch (Exception e) {
            System.out.println("========AfterAll Error==========");
            e.printStackTrace();
            System.out.println(e);
            System.out.println("=================");
        }

    }

    @Test
    void AllInsertTest() {
        try (
                var modelRepo = new ModelRepository();
                var storageRepo = new StorageRepository();
                var storageMetaSchemaRepo = new StorageMetadataSchemaRepository();
                var modelMetadataSchemaRepo = new ModelMetadataSchemaRepository();
                var storageAdaptorConnInfoSchemaRepo = new StorageAdaptorConnInfoSchemaRepository();
                var storageAdaptorSchemaRepo = new StorageAdaptorSchemaRepository();
                var tagRepo = new TagRepository();
                var dataTypeSchemaRepo = new DataTypeSchemaRepository();
                var dataTypeOptionSchemaRepo = new DataTypeOptionSchemaRepository();
        ) {
            var adaptorId = UUID.fromString("9df7ac96-90fa-481d-b18d-e3ca09ec95eb");
            var storageId = UUID.fromString("f29d3d75-59e8-4ae6-9902-53869db21d8c");
            var userId = UUID.fromString("94352448-1c3a-4e58-80a0-f1eff25cb34e");
            var metadataId = UUID.fromString("c518ae33-aec9-42d4-b634-56b4871ec73d");
            var tagId = UUID.fromString("13343db2-a252-4b3a-8e7c-e3ee8e74507a");
            var modelId = UUID.fromString("7a481647-8eb6-4466-8d58-90e2137a6202");
            var childModelId = UUID.fromString("3f91ea59-d4a3-4426-a6ee-8582f2a20b0f");
            var feedId = UUID.fromString("3456c04a-2559-4026-9a74-fcdedc18ee81");
            var parentFeedId = UUID.fromString("fdd6462d-3b6b-413b-8024-9cc79a6ad969");
            var resolvedId = UUID.fromString("d2202504-6f42-4bed-b3f0-74e4ff08ea43");
            var createdAt = LocalDateTime.now();

            var storageAdaptorSchema = StorageAdaptorSchema.builder()
                    .adaptorId(adaptorId)
                    .name("example_storage_adaptor_schema_name")
                    .adaptorType(AdaptorType.STORAGE)
                    .enable(false)
                    .build();

            var storageAdaptorConnInfoSchema = StorageAdaptorConnInfoSchema.builder()
                    .adaptorId(adaptorId)
                    .type("example_storage_adaptor_conn_info_schema_type")
                    .key("example_storage_adaptor_conn_info_schema_key")
                    .value("example_storage_adaptor_conn_info_schema_value")
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
                    .value("example_storage_metadata_value")
                    .build();

            var storageTag = StorageTag.builder()
                    .storageId(storageId)
                    .tagId(tagId)
                    .build();

            var storageConnInfo = StorageConnInfo.builder()
                    .storageId(storageId)
                    .type("example_storage_conn_info_type")
                    .key("example_storage_conn_info_key")
                    .value("example_storage_conn_info_value")
                    .isOption(false)
                    .build();

            var dataTypeSchema = DataTypeSchema.builder()
                    .dataType(DataType.CSV)
                    .name("csv")
                    .build();

            var dataTypeOptionSchema = DataTypeOptionSchema.builder()
                    .dataType(DataType.CSV)
                    .key("example_data_type_option_schema")
                    .value("example_data_type_option_value")
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
                    .key("example_data_type_option_key")
                    .value("example_data_type_option_value")
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
                    .value("example_model_metadata_value")
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
                    .value(10)
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
                    .value("example_tag")
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
        } catch (Exception e) {
            System.out.println("============ Occured ");
            e.printStackTrace();
            System.out.println(e);
            System.out.println("============ Occured ");
        }
    }

    @Test
    void t() {
        System.out.println("pass");
    }
}