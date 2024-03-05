package com.mobigen.datafabric.core.services;

import com.mobigen.datafabric.core.util.Converter;
import com.mobigen.datafabric.dataLayer.service.jpaService.ModelMetadataSchemaService;
import com.mobigen.datafabric.dataLayer.service.jpaService.StorageMetadataSchemaService;
import com.mobigen.datafabric.share.interfaces.*;
import dto.ModelMetadataSchema;
import dto.StorageMetadataSchema;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * This class is a grpc protocol for using MetadataSchema(Model, Storage)
 *
 * @author Kosb
 * @version 0.0.1
 */
@Slf4j
@GrpcService
public class MetadataGrpcService extends MetadataSchemaServiceGrpc.MetadataSchemaServiceImplBase {
    private final ModelMetadataSchemaService modelMetadataSchemaService;
    private final StorageMetadataSchemaService storageMetadataSchemaService;

    public MetadataGrpcService(ModelMetadataSchemaService modelMetadataSchemaService, StorageMetadataSchemaService storageMetadataSchemaService) {
        this.modelMetadataSchemaService = modelMetadataSchemaService;
        this.storageMetadataSchemaService = storageMetadataSchemaService;
    }

    @Override
    public void addStorageMetadataSchema(MetadataSchema request, StreamObserver<CommonResponse> responseObserver) {
        var storageMeta = convertDtoStorageMeta(request);
        CommonResponse commonResponse;

        try {
            storageMetadataSchemaService.save(storageMeta);
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
    public void addModelMetadataSchema(MetadataSchema request, StreamObserver<CommonResponse> responseObserver) {
        var modelMeta = convertDtoModelMeta(request);
        CommonResponse commonResponse;

        try {
            modelMetadataSchemaService.save(modelMeta);
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
    public void addStorageMetadataSchemas(ReqMetadataSchemas request, StreamObserver<CommonResponse> responseObserver) {
        for (MetadataSchema metadataSchema : request.getMetadataSchemaList()) {
            var storageMeta = convertDtoStorageMeta(metadataSchema);
            CommonResponse commonResponse;

            try {
                storageMetadataSchemaService.save(storageMeta);
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
    }

    @Override
    public void addModelMetadataSchemas(ReqMetadataSchemas request, StreamObserver<CommonResponse> responseObserver) {
        for (MetadataSchema metadataSchema : request.getMetadataSchemaList()) {
            var modelMeta = convertDtoModelMeta(metadataSchema);
            CommonResponse commonResponse;

            try {
                modelMetadataSchemaService.save(modelMeta);
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
    }

    @Override
    public void getStorageMetadataSchemas(ReqName request, StreamObserver<ResGetMetas> responseObserver) {
        ResGetMetas commonResponse;
        var converter = new Converter();

        try {
            var metaList = storageMetadataSchemaService.findByName(request.getName(), converter.convertPageable(request.getPageable()));

            if (metaList == null || metaList.isEmpty()) {
                // todo
                commonResponse = ResGetMetas.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetMetas.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetMetas.Data.newBuilder()
                                        .addAllMetadataSchema(this.convertGrpcStorageMetaList(metaList))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            e.printStackTrace();
            commonResponse = ResGetMetas.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getModelMetadataSchemas(ReqName request, StreamObserver<ResGetMetas> responseObserver) {
        ResGetMetas commonResponse;
        var converter = new Converter();

        try {
            var metaList = modelMetadataSchemaService.findByName(request.getName(), converter.convertPageable(request.getPageable()));

            if (metaList == null || metaList.isEmpty()) {
                // todo
                commonResponse = ResGetMetas.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetMetas.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetMetas.Data.newBuilder()
                                        .addAllMetadataSchema(this.convertGrpcModelMetaList(metaList))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            e.printStackTrace();
            commonResponse = ResGetMetas.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStorageMetadataSchema(MetadataSchema request, StreamObserver<CommonResponse> responseObserver) {
        var meta = convertDtoStorageMeta(request);
        CommonResponse commonResponse;
        try {
            storageMetadataSchemaService.update(meta);
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
    public void updateModelMetadataSchema(MetadataSchema request, StreamObserver<CommonResponse> responseObserver) {
        var meta = convertDtoModelMeta(request);
        CommonResponse commonResponse;
        try {
            modelMetadataSchemaService.update(meta);
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
    public void deleteStorageMetadataSchema(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        try {
            storageMetadataSchemaService.deleteById(UUID.fromString(request.getId()));
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
    public void deleteModelMetadataSchema(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        try {
            modelMetadataSchemaService.deleteById(UUID.fromString(request.getId()));
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

    private dto.StorageMetadataSchema convertDtoStorageMeta(MetadataSchema metadataSchema) {
        return dto.StorageMetadataSchema.builder()
                .metadataId(UUID.fromString(metadataSchema.getMetadataId()))
                .name(metadataSchema.getName())
                .description(metadataSchema.getDescription())
                .build();
    }

    private dto.ModelMetadataSchema convertDtoModelMeta(MetadataSchema metadataSchema) {
        return dto.ModelMetadataSchema.builder()
                .metadataId(UUID.fromString(metadataSchema.getMetadataId()))
                .name(metadataSchema.getName())
                .description(metadataSchema.getDescription())
                .build();
    }

    private Iterable<MetadataSchema> convertGrpcModelMetaList(List<ModelMetadataSchema> metaList) {
        var list = new ArrayList<MetadataSchema>();
        for (dto.ModelMetadataSchema meta : metaList) {
            list.add(convertGrpcMeta(meta));
        }

        return list;
    }

    private Iterable<MetadataSchema> convertGrpcStorageMetaList(List<StorageMetadataSchema> metaList) {
        var list = new ArrayList<MetadataSchema>();
        for (dto.StorageMetadataSchema meta : metaList) {
            list.add(convertGrpcMeta(meta));
        }

        return list;
    }

    private MetadataSchema convertGrpcMeta(ModelMetadataSchema meta) {
        return MetadataSchema.newBuilder()
                .setMetadataId(meta.getMetadataId().toString())
                .setName(meta.getName())
                .setDescription(meta.getDescription())
                .build();
    }

    private MetadataSchema convertGrpcMeta(StorageMetadataSchema meta) {
        return MetadataSchema.newBuilder()
                .setMetadataId(meta.getMetadataId().toString())
                .setName(meta.getName())
                .setDescription(meta.getDescription())
                .build();
    }
}
