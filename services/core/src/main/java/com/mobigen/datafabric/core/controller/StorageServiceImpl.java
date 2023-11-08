package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.DataStorageService;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * gRPC 의 request 를 받아 response 를 생성하는 콜백 클래스의 구현부
 * Storage 관련 서비스 제공
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class StorageServiceImpl implements StorageServiceCallBack {
    DataStorageService dataStorageService = new DataStorageService();

    @Override
    public StorageOuterClass.ResStorageOverview overview() {
        return StorageOuterClass.ResStorageOverview.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorageOverview.Data.newBuilder()
                        .addAllStorageTypeCount(List.of(
                                StorageOuterClass.StorageTypeCount.newBuilder()
                                        .setCount(4)
                                        .setStorageType("postgresql")
                                        .build()
                        ))
                        .addAllStorageStatusCount(List.of(
                                StorageOuterClass.StorageStatusCount.newBuilder()
                                        .setCount(5)
                                        .setStatus(1)
                                        .build()
                        ))
                        .addAllStorageStatistics(List.of(
                                StorageOuterClass.StorageStatistics.newBuilder()
                                        .build()
                        ))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorages search(StorageOuterClass.ReqStorageSearch request) {
        var filters = request.getFilter();
        var sorts = request.getSortsList();
        return StorageOuterClass.ResStorages.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorages.Data.newBuilder()
                        .addAllStorages(dataStorageService.search(filters, sorts))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage status(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .setStorage(dataStorageService.status(request.getId()))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage default_(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .setStorage(dataStorageService.default_(request.getId()))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorage advanced(Utilities.ReqId request) {
        return StorageOuterClass.ResStorage.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorage.Data.newBuilder()
                        .setStorage(dataStorageService.advanced(request.getId()))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorageBrowse browse(StorageOuterClass.ReqStorageBrowse request) {
        return StorageOuterClass.ResStorageBrowse.newBuilder()
                .setCode("OK")
                .setData(StorageOuterClass.ResStorageBrowse.Data.newBuilder()
                        .setStorageBrowse(dataStorageService.browse(
                                request.getId(),
                                request.getPath(),
                                request.getDepth(),
                                request.getName()
                        ))
                        .build())
                .build();
    }

    @Override
    public StorageOuterClass.ResStorageBrowseDefault browseDefault() {
        return null;
    }

    @Override
    public Utilities.CommonResponse connectTest(StorageOuterClass.ConnInfo request) {
        var result = dataStorageService.connectTest(
                request.getAdaptorId(),
                request.getBasicOptionsList(),
                request.getAdvancedOptionsList(),
                request.getUrlFormat()
        );
        if (result.getLeft()) {
            return Utilities.CommonResponse.newBuilder()
                    .setCode("OK")
                    .build();
        } else {
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(result.getRight())
                    .build();
        }
    }

    @Override
    public Utilities.CommonResponse addStorage(StorageOuterClass.Storage request) {
        try {
            dataStorageService.addStorage(request);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("OK")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }

    @Override
    public Utilities.CommonResponse updateStorage() {
        return null;
    }

    @Override
    public StorageOuterClass.ResConnectedData connectedData() {
        return null;
    }

    @Override
    public Utilities.CommonResponse deleteStorage(Utilities.ReqId request) {
        try {
            dataStorageService.deleteStorage(request.getId());
            return Utilities.CommonResponse.newBuilder()
                    .setCode("OK")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Utilities.CommonResponse.newBuilder()
                    .setCode("FAIL")
                    .setErrMsg(e.getMessage())
                    .build();
        }
    }
}
