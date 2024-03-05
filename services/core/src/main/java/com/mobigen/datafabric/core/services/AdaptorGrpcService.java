package com.mobigen.datafabric.core.services;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.mobigen.datafabric.core.util.Converter;
import com.mobigen.datafabric.dataLayer.service.jpaService.StorageAdaptorSchemaService;
import com.mobigen.datafabric.share.interfaces.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.JpaSystemException;

import java.util.*;

/**
 * This class is a grpc protocol for using AdaptorSchema
 *
 * @author Kosb
 * @version 0.0.1
 */
@Slf4j
@GrpcService
public class AdaptorGrpcService extends AdaptorServiceGrpc.AdaptorServiceImplBase {
    private final StorageAdaptorSchemaService storageAdaptorSchemaService;

    public AdaptorGrpcService(StorageAdaptorSchemaService storageAdaptorSchemaService) {
        this.storageAdaptorSchemaService = storageAdaptorSchemaService;
    }

    @Override
    public void addAdaptor(StorageAdaptorSchema request, StreamObserver<CommonResponse> responseObserver) {
        var storageAdaptorSchema = convertDtoAdaptor(request);
        CommonResponse commonResponse;

        try {
            storageAdaptorSchemaService.save(storageAdaptorSchema);

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
    public void getAdaptor(ReqId request, StreamObserver<ResGetAdaptor> responseObserver) {
        ResGetAdaptor commonResponse;
        try {
            var storageAdaptorSchema = storageAdaptorSchemaService.findById(UUID.fromString(request.getId()));

            if (storageAdaptorSchema.isPresent()) {
                var res = storageAdaptorSchema.get();

                // todo
                commonResponse = ResGetAdaptor.newBuilder()
                        .setCode("success")
                        .setStorageAdaptorSchema(convertGrpcAdaptor(res))
                        .build();
            } else {
                // todo
                commonResponse = ResGetAdaptor.newBuilder()
                        .setCode("success")
                        .build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetAdaptor.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getAdaptors(ReqGetAdaptors request, StreamObserver<ResGetAdaptors> responseObserver) {
        ResGetAdaptors commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var storageAdaptorSchemaList = switch (request.getStorageAdaptorSchemaTypeCase()) {
                case NAME -> storageAdaptorSchemaService.findByName(request.getName(), jpaPageable);
                case ADAPTOR_TYPE ->
                        storageAdaptorSchemaService.findByAdaptorType(converter(request.getAdaptorType()), jpaPageable);
                case ENABLE -> storageAdaptorSchemaService.findByEnable(request.getEnable(), jpaPageable);
                case STORAGEADAPTORSCHEMATYPE_NOT_SET -> null;
            };

            if (storageAdaptorSchemaList == null || storageAdaptorSchemaList.isEmpty()) {
                // todo
                commonResponse = ResGetAdaptors.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetAdaptors.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetAdaptors.Data.newBuilder()
                                        .addAllStorageAdaptorSchema(convertGrpcAdaptorList(storageAdaptorSchemaList))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            e.printStackTrace();
            commonResponse = ResGetAdaptors.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAdaptors(ReqGetAdaptors request, StreamObserver<ResGetAdaptors> responseObserver) {
        ResGetAdaptors commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var storageAdaptorSchemas = storageAdaptorSchemaService.findAll(jpaPageable);
            if (storageAdaptorSchemas == null || storageAdaptorSchemas.isEmpty()) {
                // todo
                commonResponse = ResGetAdaptors.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetAdaptors.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetAdaptors.Data.newBuilder()
                                        .addAllStorageAdaptorSchema(convertGrpcAdaptorList(storageAdaptorSchemas))
                                        .build()
                        ).build();
            }

        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            e.printStackTrace();
            commonResponse = ResGetAdaptors.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAdaptor(StorageAdaptorSchema request, StreamObserver<CommonResponse> responseObserver) {
        var storageAdaptorSchema = convertDtoAdaptor(request);
        CommonResponse commonResponse;
        try {
            storageAdaptorSchemaService.update(storageAdaptorSchema);
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
    public void deleteAdaptor(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        try {
            storageAdaptorSchemaService.deleteById(UUID.fromString(request.getId()));
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

    // todo

    @Override
    public void overview(Empty request, StreamObserver<ResAdaptorOverview> responseObserver) {
        super.overview(request, responseObserver);
    }

    public dto.StorageAdaptorSchema convertDtoAdaptor(StorageAdaptorSchema storageAdaptorSchema) {
        var adaptorId = UUID.fromString(storageAdaptorSchema.getAdaptorId());
        return dto.StorageAdaptorSchema.builder()
                .adaptorId(adaptorId)
                .name(storageAdaptorSchema.getName())
                .adaptorType(converter(storageAdaptorSchema.getType()))
                .logo(storageAdaptorSchema.getLogo().toByteArray())
                .enable(storageAdaptorSchema.getEnable())
                .storageAdaptorConnInfoSchemas(
                        convertDtoConnInfoList(adaptorId, storageAdaptorSchema.getStorageAdaptorConnInfoSchemaList())
                )
                .build();

    }

    public StorageAdaptorSchema convertGrpcAdaptor(dto.StorageAdaptorSchema storageAdaptorSchema) {
        if (storageAdaptorSchema != null && storageAdaptorSchema.getLogo() != null)
            return StorageAdaptorSchema.newBuilder()
                    .setAdaptorId(storageAdaptorSchema.getAdaptorId().toString())
                    .setName(storageAdaptorSchema.getName())
                    .setType(converter(storageAdaptorSchema.getAdaptorType()))
                    .setLogo(ByteString.copyFrom(storageAdaptorSchema.getLogo()))
                    .setEnable(storageAdaptorSchema.isEnable())
                    .addAllStorageAdaptorConnInfoSchema(
                            convertGrpcConnInfoList(storageAdaptorSchema.getStorageAdaptorConnInfoSchemas()))
                    .build();
        else
            return StorageAdaptorSchema.newBuilder()
                    .setAdaptorId(Objects.requireNonNull(storageAdaptorSchema).getAdaptorId().toString())
                    .setName(storageAdaptorSchema.getName())
                    .setType(converter(storageAdaptorSchema.getAdaptorType()))
                    .setEnable(storageAdaptorSchema.isEnable())
                    .addAllStorageAdaptorConnInfoSchema(
                            convertGrpcConnInfoList(storageAdaptorSchema.getStorageAdaptorConnInfoSchemas()))
                    .build();

    }

    public ArrayList<dto.StorageAdaptorConnInfoSchema> convertDtoConnInfoList(
            UUID adaptorId,
            List<StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemaList) {
        var list = new ArrayList<dto.StorageAdaptorConnInfoSchema>();
        for (var storageAdaptorConnInfoSchema : storageAdaptorConnInfoSchemaList) {
            list.add(convertDtoConnInfo(adaptorId, storageAdaptorConnInfoSchema));
        }

        return list;
    }

    public ArrayList<StorageAdaptorConnInfoSchema> convertGrpcConnInfoList(
            List<dto.StorageAdaptorConnInfoSchema> storageAdaptorConnInfoSchemaList) {
        var list = new ArrayList<StorageAdaptorConnInfoSchema>();
        for (var storageAdaptorConnInfoSchema : storageAdaptorConnInfoSchemaList) {
            list.add(convertGrpcConnInfo(storageAdaptorConnInfoSchema));
        }

        return list;
    }

    public dto.StorageAdaptorConnInfoSchema convertDtoConnInfo(
            UUID adaptorId,
            StorageAdaptorConnInfoSchema storageAdaptorConnInfoSchema) {
        var converter = new Converter();
        return dto.StorageAdaptorConnInfoSchema.builder()
                .adaptorId(adaptorId)
                .type(storageAdaptorConnInfoSchema.getType())
                .adaptorConnSchemaKey(storageAdaptorConnInfoSchema.getAdaptorConnSchemaKey())
                .adaptorConnSchemaValue(storageAdaptorConnInfoSchema.getAdaptorConnSchemaValue())
                .dataType(converter.convert(storageAdaptorConnInfoSchema.getDataType()))
                .defaultValue(storageAdaptorConnInfoSchema.getDefaultValue())
                .description(storageAdaptorConnInfoSchema.getDesc())
                .required(storageAdaptorConnInfoSchema.getRequired())
                .build();
    }

    public StorageAdaptorConnInfoSchema convertGrpcConnInfo(
            dto.StorageAdaptorConnInfoSchema storageAdaptorConnInfoSchema) {
        var converter = new Converter();
        return StorageAdaptorConnInfoSchema.newBuilder()
                .setType(storageAdaptorConnInfoSchema.getType())
                .setAdaptorConnSchemaKey(storageAdaptorConnInfoSchema.getAdaptorConnSchemaKey())
                .setAdaptorConnSchemaValue(storageAdaptorConnInfoSchema.getAdaptorConnSchemaValue())
                .setDataType(converter.convert(storageAdaptorConnInfoSchema.getDataType()))
                .setDefaultValue(storageAdaptorConnInfoSchema.getDefaultValue())
                .setDesc(storageAdaptorConnInfoSchema.getDescription())
                .setRequired(storageAdaptorConnInfoSchema.isRequired())
                .build();
    }

    public ArrayList<StorageAdaptorSchema> convertGrpcAdaptorList(
            List<dto.StorageAdaptorSchema> storageAdaptorSchemas) {
        var list = new ArrayList<StorageAdaptorSchema>();
        for (var storageAdaptorSchema : storageAdaptorSchemas) {
            list.add(convertGrpcAdaptor(storageAdaptorSchema));
        }

        return list;
    }

    public dto.enums.AdaptorType converter(AdaptorType adaptorType) {
        return switch (adaptorType) {
            case MINIO -> dto.enums.AdaptorType.MINIO;
            case POSTGRES -> dto.enums.AdaptorType.POSTGRESQL;
            case MARIADB -> dto.enums.AdaptorType.MARIADB;
            case UNRECOGNIZED -> null;
        };
    }

    public AdaptorType converter(dto.enums.AdaptorType adaptorType) {
        return switch (adaptorType) {
            case MINIO -> AdaptorType.MINIO;
            case MARIADB -> AdaptorType.MARIADB;
            case POSTGRESQL -> AdaptorType.POSTGRES;
            case UNRECOGNIZED -> null;
        };
    }

}

