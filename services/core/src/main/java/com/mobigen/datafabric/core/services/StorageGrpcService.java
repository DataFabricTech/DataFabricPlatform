package com.mobigen.datafabric.core.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.mobigen.datafabric.core.util.Converter;
import com.mobigen.datafabric.core.util.QueuePublisher;
import com.mobigen.datafabric.dataLayer.service.jpaService.StorageService;
import com.mobigen.datafabric.share.interfaces.*;
import dto.enums.StatusType;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * This class is a grpc protocol for using Storage
 *
 * @author Kosb
 * @version 0.0.1
 */
@Slf4j
@GrpcService
public class StorageGrpcService extends StorageServiceGrpc.StorageServiceImplBase {
    private final StorageService storageService;

    public StorageGrpcService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void addStorage(Storage request, StreamObserver<CommonResponse> responseObserver) {
        var storage = convertDtoStorage(request);
        CommonResponse commonResponse;

        try {
            storageService.save(storage);
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
    public void getStorage(ReqId request, StreamObserver<ResGetStorage> responseObserver) {
        ResGetStorage commonResponse;
        try {
            var storageAdaptorSchema = storageService.findById(UUID.fromString(request.getId()));

            if (storageAdaptorSchema.isPresent()) {
                var res = storageAdaptorSchema.get();

                // todo
                commonResponse = ResGetStorage.newBuilder()
                        .setCode("success")
                        .setStorage(convertGrpcStorage(res))
                        .build();
            } else {
                // todo
                commonResponse = ResGetStorage.newBuilder()
                        .setCode("success")
                        .build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetStorage.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getStorages(ReqGetStorages request, StreamObserver<ResGetStorages> responseObserver) {
        ResGetStorages commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var storageAdaptorSchemas = switch (request.getStorageTypeCase()) {
                case NAME -> storageService.findByName(request.getName(), jpaPageable);
                case CREATED_BY -> storageService.findByCreatedBy(UUID.fromString(request.getCreatedBy()), jpaPageable);
                case MODIFIED_BY ->
                        storageService.findByModifiedBy(UUID.fromString(request.getModifiedBy()), jpaPageable);
                case STATUS -> storageService.findByStatus(converter.convert(request.getStatus()), jpaPageable);
                case STORAGETYPE_NOT_SET -> null;
            };

            if (storageAdaptorSchemas == null || storageAdaptorSchemas.isEmpty()) {
                // todo
                commonResponse = ResGetStorages.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetStorages.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetStorages.Data.newBuilder()
                                        .addAllStorage(convertGrpcStorageList(storageAdaptorSchemas))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetStorages.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void getAllStorages(ReqGetStorages request, StreamObserver<ResGetStorages> responseObserver) {
        ResGetStorages commonResponse;
        try {
            var converter = new Converter();
            var jpaPageable = converter.convertPageable(request.getPageable());

            var storages = storageService.findAll(jpaPageable);
            if (storages == null || storages.isEmpty()) {
                // todo
                commonResponse = ResGetStorages.newBuilder()
                        .setCode("success")
                        .build();
            } else {
                commonResponse = ResGetStorages.newBuilder()
                        .setCode("success")
                        .setData(
                                ResGetStorages.Data.newBuilder()
                                        .addAllStorage(convertGrpcStorageList(storages))
                                        .build()
                        ).build();
            }
        } catch (IllegalArgumentException | NoSuchElementException | DataAccessException e) {
            // todo
            log.error("error");
            commonResponse = ResGetStorages.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(commonResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStorage(Storage request, StreamObserver<CommonResponse> responseObserver) {
        var storage = convertDtoStorage(request);
        CommonResponse commonResponse;
        try {
            storageService.update(storage);
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
    public void deleteStorage(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        try {
            storageService.deleteById(UUID.fromString(request.getId()));
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
    public void connectTest(ReqId request, StreamObserver<ResConnectTest> responseObserver) {
        ResConnectTest resConnectTest;
        var converter = new Converter();
        try {
            var status = mockConnectTest(UUID.fromString(request.getId()));
            resConnectTest = ResConnectTest.newBuilder()
                    .setCode("success")
                    .setStatus(converter.convert(status))
                    .build();
        } catch (Exception e) {
            resConnectTest = ResConnectTest.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(resConnectTest);
        responseObserver.onCompleted();
    }

    @Override
    public void getDataListWithId(ReqId request, StreamObserver<ResDataList> responseObserver) {
        /**
         * todo 저장소에 있는 Table 혹은 file들을 가져오는 명령어
         */
        // 이건 그냥 예시일 뿐이다.
        ResDataList resDataList;
        try {
            JsonObject dataList = mockExtractionGetDataList(UUID.fromString(request.getId()));
            resDataList = ResDataList.newBuilder()
                    .setCode("success")
                    .setDataList(convertGrpcDataList(dataList))
                    .build();
        } catch (Exception e) {
            resDataList = ResDataList.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(resDataList);
        responseObserver.onCompleted();
    }

    @Override
    public void getDataListWithStorage(Storage request, StreamObserver<ResDataList> responseObserver) {
        ResDataList resDataList;
        try {
            JsonObject dataList = mockExtractionGetDataList(convertDtoStorage(request));
            resDataList = ResDataList.newBuilder()
                    .setCode("success")
                    .setDataList(convertGrpcDataList(dataList))
                    .build();
        } catch (Exception e) {
            resDataList = ResDataList.newBuilder()
                    .setCode("fail")
                    .setErrMsg(e.getMessage())
                    .build();
        }

        responseObserver.onNext(resDataList);
        responseObserver.onCompleted();
    }

    @Override
    public void extractStorageMetadata(ReqId request, StreamObserver<CommonResponse> responseObserver) {
        CommonResponse commonResponse;
        var queuePublisher = new QueuePublisher();
        var client = queuePublisher.getClient();
        try {
            client.publish(queuePublisher.getQueueId(),
                    "",
                    queuePublisher.makeBody(
                            "storageMetadataExtract",
                            request.getId(),
                            "extraction"));
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

    // todo line

    @Override
    public void overview(Empty request, StreamObserver<ResStorageOverview> responseObserver) {
        super.overview(request, responseObserver);
    }

    private dto.Storage convertDtoStorage(Storage storage) {
        var converter = new Converter();
        if (storage != null)
            // todo null check
            return dto.Storage.builder()
                    .storageId(UUID.fromString(storage.getStorageId()))
                    .adaptorId(UUID.fromString(storage.getAdaptorId()))
                    .name(storage.getName())
                    .description(storage.getDescription())
                    .createdBy(UUID.fromString(storage.getCreatedBy()))
                    .createdAt(converter.convert(storage.getCreatedAt()))
                    .modifiedBy(UUID.fromString(storage.getCreatedBy()))
                    .modifiedAt(converter.convert(storage.getModifiedAt()))
                    .status(converter.convert(storage.getStatus()))
                    .lastSyncAt(converter.convert(storage.getLastSyncAt()))
                    .lastMonitoringAt(converter.convert(storage.getLastMonitoringAt()))
                    .syncEnable(storage.getSyncEnable())
                    .syncTime(storage.getSyncTime())
                    .monitoringEnable(storage.getMonitoringEnable())
                    .monitoringPeriod(storage.getMonitoringPeriod())
                    .monitoringFailThreshold(storage.getMonitoringFailThreshold())
                    .dataAutoAdds(convertDtoDataAutoAddList(storage.getStorageId(), storage.getDataAutoAddList()))
                    .storageMetadata(convertDtoStorageMetadataList(storage.getStorageId(), storage.getStorageMetaList()))
                    .storageTags(convertDtoStorageTagList(storage.getStorageId(), storage.getStorageTagList()))
                    .storageConnInfos(convertDtoStorageConnInfoList(storage.getStorageId(), storage.getStorageConnInfoList()))
                    .build();

        return null;
    }

    private Storage convertGrpcStorage(dto.Storage storage) {
        var converter = new Converter();
        if (storage != null)
            return Storage.newBuilder()
                    .setStorageId(storage.getStorageId().toString())
                    .setAdaptorId(storage.getAdaptorId().toString())
                    .setName(storage.getName())
                    .setDescription(storage.getDescription())
                    .setCreatedBy(storage.getCreatedBy().toString())
                    .setCreatedAt(converter.convert(storage.getCreatedAt()))
                    .setModifiedBy(storage.getModifiedBy().toString())
                    .setModifiedAt(converter.convert(storage.getModifiedAt()))
                    .setStatus(converter.convert(storage.getStatus()))
                    .setLastSyncAt(converter.convert(storage.getLastSyncAt()))
                    .setLastMonitoringAt(converter.convert(storage.getLastMonitoringAt()))
                    .setSyncEnable(storage.isSyncEnable())
                    .setSyncTime(storage.getSyncTime())
                    .setMonitoringEnable(storage.isMonitoringEnable())
                    .setMonitoringPeriod(storage.getMonitoringPeriod())
                    .setMonitoringFailThreshold(storage.getMonitoringFailThreshold())
                    .addAllDataAutoAdd(convertGrpcDataAutoAddList(storage.getDataAutoAdds()))
                    .addAllStorageMeta(convertGrpcStorageMetadataList(storage.getStorageMetadata()))
                    .addAllStorageTag(convertGrpcStorageTagList(storage.getStorageTags()))
                    .addAllStorageConnInfo(convertGrpcStorageConnInfoList(storage.getStorageConnInfos()))
                    .build();
        return null;
    }

    private ArrayList<dto.DataAutoAdd> convertDtoDataAutoAddList(String storageId, List<DataAutoAdd> dataAutoAddList) {
        var list = new ArrayList<dto.DataAutoAdd>();
        for (var dataAutoAdd : dataAutoAddList) {
            list.add(convertDtoDataAutoAdd(storageId, dataAutoAdd));
        }
        return list;
    }

    private ArrayList<DataAutoAdd> convertGrpcDataAutoAddList(List<dto.DataAutoAdd> dataAutoAddList) {
        var list = new ArrayList<DataAutoAdd>();
        for (var dataAutoAdd : dataAutoAddList) {
            list.add(convertGrpcDataAutoAdd(dataAutoAdd));
        }
        return list;
    }

    private dto.DataAutoAdd convertDtoDataAutoAdd(String storageId, DataAutoAdd dataAutoAdd) {
        var converter = new Converter();
        return dto.DataAutoAdd.builder()
                .storageId(UUID.fromString(storageId))
                .num(dataAutoAdd.getNum())
                .regex(dataAutoAdd.getRegex())
                .formatType(converter.convert(dataAutoAdd.getFormatType()))
                .build();
    }

    private DataAutoAdd convertGrpcDataAutoAdd(dto.DataAutoAdd dataAutoAdd) {
        var converter = new Converter();
        return DataAutoAdd.newBuilder()
                .setNum(dataAutoAdd.getNum())
                .setRegex(dataAutoAdd.getRegex())
                .setFormatType(converter.convert(dataAutoAdd.getFormatType()))
                .build();
    }

    private ArrayList<dto.StorageMetadata> convertDtoStorageMetadataList(
            String storageId,
            List<StorageMetadata> storageMetadataList) {
        var list = new ArrayList<dto.StorageMetadata>();
        for (var storageMetadata : storageMetadataList) {
            list.add(convertDtoStorageMetadata(storageId, storageMetadata));
        }
        return list;
    }

    private ArrayList<StorageMetadata> convertGrpcStorageMetadataList(List<dto.StorageMetadata> storageMetadataList) {
        var list = new ArrayList<StorageMetadata>();
        for (var storageMetadata : storageMetadataList) {
            list.add(convertGrpcStorageMetadata(storageMetadata));
        }
        return list;
    }

    private dto.StorageMetadata convertDtoStorageMetadata(String storageId, StorageMetadata storageMetadata) {
        return dto.StorageMetadata.builder()
                .storageId(UUID.fromString(storageId))
                .metadataId(UUID.fromString(storageMetadata.getMetadataId()))
                .metadataValue(storageMetadata.getMetadataValue())
                .build();
    }

    private StorageMetadata convertGrpcStorageMetadata(dto.StorageMetadata storageMetadata) {
        return StorageMetadata.newBuilder()
                .setMetadataId(storageMetadata.getMetadataId().toString())
                .setMetadataValue(storageMetadata.getMetadataValue())
                .build();
    }

    private ArrayList<dto.StorageTag> convertDtoStorageTagList(String storageId, List<StorageTag> storageTagList) {
        var list = new ArrayList<dto.StorageTag>();
        for (var storageTag : storageTagList) {
            list.add(convertDtoStorageTag(storageId, storageTag));
        }
        return list;
    }

    private ArrayList<StorageTag> convertGrpcStorageTagList(List<dto.StorageTag> storageTagList) {
        var list = new ArrayList<StorageTag>();
        for (var storageTag : storageTagList) {
            list.add(convertGrpcStorageTag(storageTag));
        }
        return list;
    }

    private dto.StorageTag convertDtoStorageTag(String storageId, StorageTag storageTag) {
        return dto.StorageTag.builder()
                .storageId(UUID.fromString(storageId))
                .tagId(UUID.fromString(storageTag.getTagId()))
                .build();
    }

    private StorageTag convertGrpcStorageTag(dto.StorageTag storageTag) {
        return StorageTag.newBuilder()
                .setTagId(storageTag.getTagId().toString())
                .build();
    }

    private ArrayList<dto.StorageConnInfo> convertDtoStorageConnInfoList(
            String storageId,
            List<StorageConnInfo> storageConnInfoList) {
        var list = new ArrayList<dto.StorageConnInfo>();
        for (var storageConnInfo : storageConnInfoList) {
            list.add(convertDtoStorageConnInfo(storageId, storageConnInfo));
        }
        return list;
    }

    private ArrayList<StorageConnInfo> convertGrpcStorageConnInfoList(List<dto.StorageConnInfo> storageConnInfoList) {
        var list = new ArrayList<StorageConnInfo>();
        for (var storageConnInfo : storageConnInfoList) {
            list.add(convertGrpcStorageConnInfo(storageConnInfo));
        }
        return list;
    }

    private dto.StorageConnInfo convertDtoStorageConnInfo(String storageId, StorageConnInfo storageConnInfo) {
        return dto.StorageConnInfo.builder()
                .storageId(UUID.fromString(storageId))
                .type(storageConnInfo.getType())
                .storageConnKey(storageConnInfo.getStorageConnKey())
                .storageConnValue(storageConnInfo.getStorageConnValue())
                .isOption(storageConnInfo.getIsOption())
                .build();
    }

    private StorageConnInfo convertGrpcStorageConnInfo(dto.StorageConnInfo storageConnInfo) {
        return StorageConnInfo.newBuilder()
                .setType(storageConnInfo.getType())
                .setStorageConnKey(storageConnInfo.getStorageConnKey())
                .setStorageConnValue(storageConnInfo.getStorageConnValue())
                .setIsOption(storageConnInfo.isOption())
                .build();
    }

    private List<Storage> convertGrpcStorageList(List<dto.Storage> storages) {
        var list = new ArrayList<Storage>();
        for (var storage : storages) {
            list.add(convertGrpcStorage(storage));
        }

        return list;
    }

    private FormatType convertGrpcFormatType(String formatType)  {
        return switch (formatType.toLowerCase()) {
            case "csv" -> FormatType.CSV;
            case "excel" -> FormatType.EXCEL;
            case "word" -> FormatType.WORD;
            case "table" -> FormatType.TABLE;
            case "view" -> FormatType.VIEW;
            case "index" -> FormatType.INDEX;
            case "txt" -> FormatType.TXT;
            case "hwp" -> FormatType.HWP;
            case "db" -> FormatType.DB;
            case "directory" -> FormatType.DIRECTORY;
            case "file" -> FormatType.FILE;
            default -> null;
        };
    }

    private ResDataList.DataList convertGrpcDataList(JsonObject jsonObject) {
        var builder = ResDataList.DataList.newBuilder();
        var nameElement = jsonObject.get("name");
        if (nameElement != null) {
            builder.setName(nameElement.getAsString());
        }

        var formatType = jsonObject.get("format_type");
        if (formatType != null) {
            // todo 이거 너무 마음에 안든다. 더 좋은 방법 찾아보자.
            builder.setFormatType(convertGrpcFormatType(formatType.getAsString()));
        }

        var hasChildren = jsonObject.get("has_children");
        if (hasChildren != null) {
            builder.setHasChildren(hasChildren.getAsBoolean());
        }

        var dataListArray = jsonObject.getAsJsonArray("children");
        if (dataListArray != null) {
            for (var element : dataListArray) {
                var childJsonObject = element.getAsJsonObject();
                builder.addDataList(convertGrpcDataList(childJsonObject));
            }
        }

        return builder.build();
    }

    /**
     * todo 현재의 함수는 extraction의 interface를 사용한 mock 함수입니다.
     */
    private StatusType mockConnectTest(UUID storageId) {
        return StatusType.CONNECTED;
    }

    private JsonObject mockExtractionGetDataList(UUID storageId) {
        // storage의 id를 통하여 dataList 추출
        var jsonObject = new JsonObject();
        jsonObject.addProperty("name", "mock");
        jsonObject.addProperty("format_type", FormatType.DB.toString());
        jsonObject.addProperty("has_children", true);

        var childObject = new JsonObject();
        childObject.addProperty("name", "mockChild");
        childObject.addProperty("format_type", FormatType.TABLE.toString());
        childObject.addProperty("has_children", false);
        var jsonArray = new JsonArray();
        jsonArray.add(childObject);

        jsonObject.add("children", jsonArray);

        return jsonObject;
    }

    private JsonObject mockExtractionGetDataList(dto.Storage storage) {
        // 저장될 storage를 통하여 dataList 추출
        var jsonObject = new JsonObject();
        jsonObject.addProperty("name", "mock");
        jsonObject.addProperty("format_type", FormatType.DB.toString());
        jsonObject.addProperty("has_children", true);

        var childObject = new JsonObject();
        childObject.addProperty("name", "mockChild");
        childObject.addProperty("format_type", FormatType.TABLE.toString());
        childObject.addProperty("has_children", false);
        var jsonArray = new JsonArray();
        jsonArray.add(childObject);

        jsonObject.add("children", jsonArray);

        return jsonObject;
    }
}
