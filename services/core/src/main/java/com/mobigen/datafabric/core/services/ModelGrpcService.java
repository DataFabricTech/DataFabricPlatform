package com.mobigen.datafabric.core.services;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.core.util.Converter;
import com.mobigen.datafabric.core.util.QueuePublisher;
import com.mobigen.datafabric.dataLayer.service.jpaService.ModelService;
import com.mobigen.datafabric.share.interfaces.*;
import dto.ModelRelation;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * This class is a grpc protocol for using AdaptorSchema
 *
 * @author Kosb
 * @version 0.0.1
 */
@Slf4j
@GrpcService
public class ModelGrpcService extends ModelServiceGrpc.ModelServiceImplBase {
    private final ModelService modelService;

    public ModelGrpcService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public void addModel(Model request, StreamObserver<CommonResponse> responseObserver) {
        var model = convertDtoModel(request);
        CommonResponse commonResponse;

        try {
            modelService.save(model);
            // todo
            commonResponse = CommonResponse.newBuilder()
                    .setCode("success")
                    .build();
        } catch (IllegalStateException | NullPointerException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = CommonResponse.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getModel(ReqId request, StreamObserver<ResGetModel> responseObserver) {
        ResGetModel commonResponse;
        try {
            var storageModelSchema = modelService.findById(UUID.fromString(request.getId()));

            if (storageModelSchema.isPresent()) {
                var res = storageModelSchema.get();

                // todo
                commonResponse = ResGetModel.newBuilder()
                        .setCode("success")
                        .setModel(convertGrpcModel(res))
                        .build();
            } else {
                // todo
                commonResponse = ResGetModel.newBuilder()
                        .setCode("success")
                        .build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetModel.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getModels(ReqGetModels request, StreamObserver<ResGetModels> responseObserver) {
        ResGetModels commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var modelList = switch (request.getModelTypeCase()) {
                case NAME -> modelService.findByName(request.getName(), jpaPageable);
                case FORMAT_TYPE ->
                        modelService.findByFormatType(converter.convert(request.getFormatType()), jpaPageable);
                case STORAGE_ID -> modelService.findByStorageId(UUID.fromString(request.getStorageId()), jpaPageable);
                case STATUS -> modelService.findByStatus(converter.convert(request.getStatus()), jpaPageable);
                case CREATED_BY -> modelService.findByCreatedBy(UUID.fromString(request.getCreatedBy()), jpaPageable);
                case MODIFIED_BY ->
                        modelService.findByModifiedBy(UUID.fromString(request.getModifiedBy()), jpaPageable);
                case SYNC_ENABLE -> modelService.findBySyncEnable(request.getSyncEnable(), jpaPageable);
                case MODEL_META_DATA_VALUE ->
                        modelService.findByModelMetadataValue(request.getModelMetaDataValue(), jpaPageable);
                case COLUMN_META_DATA_NAME ->
                        modelService.findByColumnMetadataName(request.getColumnMetaDataName(), jpaPageable);
                case TAG_VALUE -> modelService.findByTagValue(request.getTagValue(), jpaPageable);
                case RELATION_PARENT_ID ->
                        modelService.findByRelationParentId(UUID.fromString(request.getRelationParentId()), jpaPageable);
                case RELATION_CHILD_ID ->
                        modelService.findByRelationChildId(UUID.fromString(request.getRelationChildId()), jpaPageable);
                case CREATED_AT_BETWEEN -> modelService.findByCreatedAtBetween(
                        converter.convert(request.getCreatedAtBetween().getFrom()),
                        converter.convert(request.getCreatedAtBetween().getTo()),
                        jpaPageable);
                case MODIFIED_AT_BETWEEN -> modelService.findByModifiedAtBetween(
                        converter.convert(request.getModifiedAtBetween().getFrom()),
                        converter.convert(request.getModifiedAtBetween().getTo()),
                        jpaPageable);
                case CREATED_AT_BEFORE ->
                        modelService.findByCreatedAtBefore(converter.convert(request.getCreatedAtBefore()), jpaPageable);
                case MODIFIED_AT_BEFORE ->
                        modelService.findByModifiedAtBefore(converter.convert(request.getModifiedAtBefore()), jpaPageable);
                case CREATED_AT_AFTER ->
                        modelService.findByCreatedAtAfter(converter.convert(request.getCreatedAtAfter()), jpaPageable);
                case MODIFIED_AT_AFTER ->
                        modelService.findByModifiedAtAfter(converter.convert(request.getModifiedAtAfter()), jpaPageable);
                case MODELTYPE_NOT_SET -> null;
            };

            if (modelList == null || modelList.isEmpty()) {
                // todo
                commonResponse = ResGetModels.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetModels.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetModels.Data.newBuilder()
                                        .addAllModel(convertGrpcModelList(modelList))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            e.printStackTrace();
            commonResponse = ResGetModels.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllModels(ReqGetModels request, StreamObserver<ResGetModels> responseObserver) {
        ResGetModels commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var modelList = modelService.findAll(jpaPageable);
            if (modelList == null || modelList.isEmpty()) {
                // todo
                commonResponse = ResGetModels.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetModels.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetModels.Data.newBuilder()
                                        .addAllModel(convertGrpcModelList(modelList))
                                        .build()
                        ).build();
            }

        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetModels.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateModel(Model request, StreamObserver<CommonResponse> responseObserver) {
        var model = convertDtoModel(request);
        CommonResponse commonResponse;
        try {
            modelService.update(model);
            // todo
            commonResponse = CommonResponse.newBuilder()
                    .setCode("success")
                    .build();
        } catch (NullPointerException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = CommonResponse.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteModel(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        try {
            modelService.deleteById(UUID.fromString(request.getId()));
            // todo
            commonResponse = CommonResponse.newBuilder()
                    .setCode("success")
                    .build();
        } catch (NullPointerException | IllegalArgumentException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = CommonResponse.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void extractModelMetadata(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        var queuePublisher = new QueuePublisher();
        var client = queuePublisher.getClient();
        try {
            client.publish(queuePublisher.getQueueId(),
                    "",
                    queuePublisher.makeBody(
                            "modelMetadataExtract",
                            request.getId(),
                            "extraction"
                    ));
            commonResponse = CommonResponse.newBuilder()
                    .setCode("success")
                    .build();
        } catch (Exception e) {
            commonResponse = CommonResponse.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    private dto.Model convertDtoModel(Model model) {
        var converter = new Converter();
        var modelId = UUID.fromString(model.getModelId());
        return dto.Model.builder()
                .modelId(UUID.fromString(model.getModelId()))
                .name(model.getName())
                .description(model.getDesc())
                .formatType(converter.convert(model.getFormatType()))
                .storageId(UUID.fromString(model.getStorageId()))
                .status(converter.convert(model.getStatus()))
                .createdAt(converter.convert(model.getCreatedAt()))
                .createdBy(UUID.fromString(model.getCreatedBy()))
                .modifiedAt(converter.convert(model.getModifiedAt()))
                .modifiedBy(UUID.fromString(model.getModifiedBy()))
                .syncEnable(model.getSyncEnable())
                .syncTime(model.getSyncTime())
                .syncAt(converter.convert(model.getSyncAt()))
                .modelMetadata(convertDtoModelMetadatalist(modelId, model.getModelMetaDataList()))
                .columnMetadata(convertDtoColumnMetadataList(modelId, model.getColumnMetaDataList()))
                .dataSamples(convertDtoDataSamples(modelId, model.getDataSampleList()))
                .modelRatingAndComments(convertDtoModelRatingAndComments(modelId, model.getModelRatingAndCommentList()))
                .modelTags(convertDtoModelTags(modelId, model.getModelTagList()))
                .modelFeedbacks(convertDtoModelFeedbacks(modelId, model.getModelFeedbackList()))
                .dataFormatOptions(convertDtoFormatOptions(modelId, model.getDataFormatOptionList()))
                .build();
    }

    private List<dto.ModelMetadata> convertDtoModelMetadatalist(UUID modelId, List<ModelMetadata> modelMetaDataList) {
        var list = new ArrayList<dto.ModelMetadata>();
        for (ModelMetadata modelMetadata : modelMetaDataList) {
            list.add(convertDtoModelMetadata(modelId, modelMetadata));
        }

        return list;
    }

    private dto.ModelMetadata convertDtoModelMetadata(UUID modelId, ModelMetadata modelMetadata) {
        return dto.ModelMetadata.builder()
                .modelId(modelId)
                .metadataId(UUID.fromString(modelMetadata.getMetadataId()))
                .metadataValue(modelMetadata.getMetadataValue())
                .build();
    }

    private List<dto.ColumnMetadata> convertDtoColumnMetadataList(UUID modelId, List<ColumnMetadata> columnMetaDataList) {
        var list = new ArrayList<dto.ColumnMetadata>();
        for (ColumnMetadata columnMetadata : columnMetaDataList) {
            list.add(convertDtoColumnMetadata(modelId, columnMetadata));
        }

        return list;
    }

    private dto.ColumnMetadata convertDtoColumnMetadata(UUID modelId, ColumnMetadata columnMetadata) {
        var converter = new Converter();
        return dto.ColumnMetadata.builder()
                .modelId(modelId)
                .num(columnMetadata.getNum())
                .name(columnMetadata.getName())
                .description(columnMetadata.getDescription())
                .dataType(converter.convert(columnMetadata.getDataType()))
                .length(columnMetadata.getLength())
                .isPK(columnMetadata.getIsPk())
                .isFK(columnMetadata.getIsFk())
                .nullable(columnMetadata.getNullable())
                .tableDataQualities(convertDtoTableDataQualities(modelId, columnMetadata.getTableDataQuailtyList()))
                .build();
    }

    private List<dto.TableDataQuality> convertDtoTableDataQualities(
            UUID modelId,
            List<TableDataQuality> tableDataQuailtyList) {
        var list = new ArrayList<dto.TableDataQuality>();
        for (TableDataQuality tableDataQuality : tableDataQuailtyList) {
            list.add(convertDtoTableDataQuality(modelId, tableDataQuality));
        }

        return list;
    }

    private dto.TableDataQuality convertDtoTableDataQuality(UUID modelId, TableDataQuality tableDataQuality) {
        var converter = new Converter();
        return dto.TableDataQuality.builder()
                .modelId(modelId)
                .num(tableDataQuality.getNum())
                .qualityType(converter.convert(tableDataQuality.getQualityType()))
                .qualityValue(tableDataQuality.getQualityValue())
                .build();
    }

    private List<dto.DataSample> convertDtoDataSamples(UUID modelId, List<DataSample> dataSampleList) {
        var list = new ArrayList<dto.DataSample>();
        for (DataSample dataSample : dataSampleList) {
            list.add(convertDtoDataSample(modelId, dataSample));
        }

        return list;
    }

    private dto.DataSample convertDtoDataSample(UUID modelId, DataSample dataSample) {
        var converter = new Converter();
        return dto.DataSample.builder()
                .modelId(modelId)
                .formatType(converter.convert(dataSample.getFormatType()))
                .filePath(dataSample.getFilePath())
                .build();
    }


    private List<dto.ModelRatingAndComment> convertDtoModelRatingAndComments(
            UUID modelId,
            List<ModelRatingAndComment> modelRatingAndCommentList) {
        var list = new ArrayList<dto.ModelRatingAndComment>();
        for (ModelRatingAndComment modelRatingAndComment : modelRatingAndCommentList) {
            list.add(convertDtoModelRatingAndComment(modelId, modelRatingAndComment));
        }

        return list;
    }

    private dto.ModelRatingAndComment convertDtoModelRatingAndComment(
            UUID modelId,
            ModelRatingAndComment modelRatingAndComment) {
        var converter = new Converter();
        return dto.ModelRatingAndComment.builder()
                .modelId(modelId)
                .userId(UUID.fromString(modelRatingAndComment.getUserId()))
                .rating(modelRatingAndComment.getRating())
                .comments(modelRatingAndComment.getComments())
                .createdAt(converter.convert(modelRatingAndComment.getCreatedAt()))
                .build();
    }


    private List<dto.ModelTag> convertDtoModelTags(UUID modelId, List<ModelTag> modelTagList) {
        var list = new ArrayList<dto.ModelTag>();
        for (ModelTag modelTag : modelTagList) {
            list.add(convertDtoModelTag(modelId, modelTag));
        }
        return list;
    }

    private dto.ModelTag convertDtoModelTag(UUID modelId, ModelTag modelTag) {
        return dto.ModelTag.builder()
                .modelId(modelId)
                .tagId(UUID.fromString(modelTag.getTagId()))
                .build();
    }


    private List<dto.ModelFeedback> convertDtoModelFeedbacks(UUID modelId, List<ModelFeedback> modelFeedbackList) {
        var list = new ArrayList<dto.ModelFeedback>();
        for (ModelFeedback modelFeedback : modelFeedbackList) {
            list.add(convertDtoModelFeedback(modelId, modelFeedback));
        }

        return list;
    }

    private dto.ModelFeedback convertDtoModelFeedback(UUID modelId, ModelFeedback modelFeedback) {
        var converter = new Converter();
        return dto.ModelFeedback.builder()
                .modelId(modelId)
                .feedId(UUID.fromString(modelFeedback.getFeedId()))
                .parentFeedId(UUID.fromString(modelFeedback.getParentFeedId()))
                .userId(UUID.fromString(modelFeedback.getUserId()))
                .time(converter.convert(modelFeedback.getTime()))
                .title(modelFeedback.getTitle())
                .body(modelFeedback.getBody())
                .isResolved(modelFeedback.getIsResolved())
                .resolvedUserId(UUID.fromString(modelFeedback.getResolvedUserId()))
                .resolvedTime(converter.convert(modelFeedback.getResolvedTime()))
                .build();
    }


    private List<dto.DataFormatOption> convertDtoFormatOptions(
            UUID modelId,
            List<DataFormatOption> dataFormatOptionList) {
        var list = new ArrayList<dto.DataFormatOption>();
        for (var dataFormatOption : dataFormatOptionList) {
            list.add(convertDtoDataFormatOption(modelId, dataFormatOption));
        }
        return list;
    }

    private dto.DataFormatOption convertDtoDataFormatOption(UUID modelId, DataFormatOption dataFormatOption) {
        return dto.DataFormatOption.builder()
                .modelId(modelId)
                .dataFormatOptionKey(dataFormatOption.getDataFormatOptionKey())
                .dataFormatOptionValue(dataFormatOption.getDataFormatOptionValue())
                .build();
    }

    private Model convertGrpcModel(dto.Model res) {
        var converter = new Converter();
        return Model.newBuilder()
                .setModelId(res.getModelId().toString())
                .setName(res.getName())
                .setDesc(res.getDescription())
                .setFormatType(converter.convert(res.getFormatType()))
                .setStorageId(res.getStorageId().toString())
                .setStatus(converter.convert(res.getStatus()))
                .setCreatedBy(res.getCreatedBy().toString())
                .setCreatedAt(converter.convert(res.getCreatedAt()))
                .setModifiedBy(res.getModifiedBy().toString())
                .setModifiedAt(converter.convert(res.getModifiedAt()))
                .setSyncEnable(res.getSyncEnable())
                .setSyncAt(converter.convert(res.getSyncAt()))
                .addAllDataFormatOption(convertGrpcDataFormatOptionList(res.getDataFormatOptions()))
                .addAllModelMetaData(convertGrpcModelMetaDataList(res.getModelMetadata()))
                .addAllColumnMetaData(convertGrpcColumnMetaDataList(res.getColumnMetadata()))
                .addAllDataSample(convertGrpcDataSampleList(res.getDataSamples()))
                .addAllModelRatingAndComment(convertGrpcModelRatingAndComments(res.getModelRatingAndComments()))
                .addAllModelTag(convertGrpcModelTags(res.getModelTags()))
                .addAllModelFeedback(convertGrpcModelFeedbacks(res.getModelFeedbacks()))
                .addAllRelation(convertGrpcRelations(res.getModelRelations()))
                .build();
    }

    private Iterable<DataFormatOption> convertGrpcDataFormatOptionList(List<dto.DataFormatOption> dataFormatOptions) {
        var list = new ArrayList<DataFormatOption>();
        for (dto.DataFormatOption dataFormatOption : dataFormatOptions) {
            list.add(convertGrpcDataFormatOption(dataFormatOption));
        }

        return list;
    }

    private DataFormatOption convertGrpcDataFormatOption(dto.DataFormatOption dataFormatOption) {
        return DataFormatOption.newBuilder()
                .setDataFormatOptionKey(dataFormatOption.getDataFormatOptionKey())
                .setDataFormatOptionValue(dataFormatOption.getDataFormatOptionValue())
                .build();
    }

    private Iterable<ModelMetadata> convertGrpcModelMetaDataList(List<dto.ModelMetadata> modelMetadataList) {
        var list = new ArrayList<ModelMetadata>();
        for (dto.ModelMetadata modelMetadata : modelMetadataList) {
            list.add(convertGrpcModelMetadata(modelMetadata));
        }

        return list;
    }

    private ModelMetadata convertGrpcModelMetadata(dto.ModelMetadata modelMetadata) {
        return ModelMetadata.newBuilder()
                .setMetadataId(modelMetadata.getMetadataId().toString())
                .setMetadataValue(modelMetadata.getMetadataValue())
                .build();
    }

    private Iterable<ColumnMetadata> convertGrpcColumnMetaDataList(List<dto.ColumnMetadata> columnMetadataList) {
        var list = new ArrayList<ColumnMetadata>();
        for (dto.ColumnMetadata columnMetadata : columnMetadataList) {
            list.add(convertGrpcColumnMetadata(columnMetadata));
        }

        return list;
    }

    private ColumnMetadata convertGrpcColumnMetadata(dto.ColumnMetadata columnMetadata) {
        var converter = new Converter();
        return ColumnMetadata.newBuilder()
                .setNum(columnMetadata.getNum())
                .setName(columnMetadata.getName())
                .setDescription(columnMetadata.getDescription())
                .setDataType(converter.convert(columnMetadata.getDataType()))
                .setLength(columnMetadata.getLength())
                .setIsPk(columnMetadata.isPK())
                .setIsFk(columnMetadata.isFK())
                .setNullable(columnMetadata.isNullable())
                .addAllTableDataQuailty(convertGrpcTableQualities(columnMetadata.getTableDataQualities()))
                .build();
    }

    private Iterable<TableDataQuality> convertGrpcTableQualities(List<dto.TableDataQuality> tableDataQualities) {
        var list = new ArrayList<TableDataQuality>();
        for (dto.TableDataQuality tableDataQuality : tableDataQualities) {
            list.add(convertGrpcTableDataQuality(tableDataQuality));
        }

        return list;
    }

    private TableDataQuality convertGrpcTableDataQuality(dto.TableDataQuality tableDataQuality) {
        var converter = new Converter();
        return TableDataQuality.newBuilder()
                .setNum(tableDataQuality.getNum())
                .setQualityType(converter.convert(tableDataQuality.getQualityType()))
                .setQualityValue(tableDataQuality.getQualityValue())
                .build();
    }

    private Iterable<DataSample> convertGrpcDataSampleList(List<dto.DataSample> dataSamples) {
        var list = new ArrayList<DataSample>();
        for (dto.DataSample dataSample : dataSamples) {
            list.add(convertGrpcDataSample(dataSample));
        }

        return list;
    }

    private DataSample convertGrpcDataSample(dto.DataSample dataSample) {
        var converter = new Converter();
        return DataSample.newBuilder()
                .setFormatType(converter.convert(dataSample.getFormatType()))
                .setFilePath(dataSample.getFilePath())
                .build();
    }

    private Iterable<ModelRatingAndComment> convertGrpcModelRatingAndComments(
            List<dto.ModelRatingAndComment> modelRatingAndComments) {
        var list = new ArrayList<ModelRatingAndComment>();
        for (dto.ModelRatingAndComment modelRatingAndComment : modelRatingAndComments) {
            list.add(convertGrpcModelRatingAndComment(modelRatingAndComment));
        }

        return list;
    }

    private ModelRatingAndComment convertGrpcModelRatingAndComment(dto.ModelRatingAndComment modelRatingAndComment) {
        var converter = new Converter();
        return ModelRatingAndComment.newBuilder()
                .setUserId(modelRatingAndComment.getUserId().toString())
                .setRating(modelRatingAndComment.getRating())
                .setComments(modelRatingAndComment.getComments())
                .setCreatedAt(converter.convert(modelRatingAndComment.getCreatedAt()))
                .build();
    }

    private Iterable<ModelTag> convertGrpcModelTags(List<dto.ModelTag> modelTags) {
        var list = new ArrayList<ModelTag>();
        for (dto.ModelTag modelTag : modelTags) {
            list.add(convertGrpcModelTag(modelTag));
        }

        return list;
    }

    private ModelTag convertGrpcModelTag(dto.ModelTag modelTag) {
        return ModelTag.newBuilder()
                .setTagId(modelTag.getTagId().toString())
                .build();
    }

    private Iterable<ModelFeedback> convertGrpcModelFeedbacks(List<dto.ModelFeedback> modelFeedbacks) {
        var list = new ArrayList<ModelFeedback>();
        for (dto.ModelFeedback modelFeedback : modelFeedbacks) {
            list.add(convertGrpcModelFeedback(modelFeedback));
        }

        return list;
    }

    private ModelFeedback convertGrpcModelFeedback(dto.ModelFeedback modelFeedback) {
        var converter = new Converter();
        return ModelFeedback.newBuilder()
                .setFeedId(modelFeedback.getFeedId().toString())
                .setParentFeedId(modelFeedback.getParentFeedId().toString())
                .setUserId(modelFeedback.getUserId().toString())
                .setTime(converter.convert(modelFeedback.getTime()))
                .setTitle(modelFeedback.getTitle())
                .setBody(modelFeedback.getBody())
                .setIsResolved(modelFeedback.isResolved())
                .setResolvedUserId(modelFeedback.getResolvedUserId().toString())
                .setResolvedTime(converter.convert(modelFeedback.getResolvedTime()))
                .build();
    }

    private Iterable<Relation> convertGrpcRelations(List<ModelRelation> modelRelations) {
        var list = new ArrayList<Relation>();
        for (ModelRelation modelRelation : modelRelations) {
            list.add(convertGrpcRelation(modelRelation));
        }

        return list;
    }

    private Relation convertGrpcRelation(ModelRelation modelRelation) {
        return Relation.newBuilder()
                .setChildId(modelRelation.getChildModelId().toString())
                .build();
    }

    private Iterable<Model> convertGrpcModelList(List<dto.Model> modelList) {
        var list = new ArrayList<Model>();
        for (dto.Model model : modelList) {
            list.add(convertGrpcModel(model));
        }

        return list;
    }
}
