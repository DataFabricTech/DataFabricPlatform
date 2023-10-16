package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.StorageTypeService;
import com.mobigen.libs.grpc.*;
import lombok.extern.slf4j.Slf4j;
import com.mobigen.libs.grpc.Storage.*;

@Slf4j
public class StorageServiceImpl implements StorageServiceCallBack {

    @Override
    public OverviewResponse overview() {
        return OverviewResponse.newBuilder().setTotalStorage(10).build();
    }

    @Override
    public StorageTypeResponse storageType(StorageTypeRequest request) {
        if (request.getMethod().equals(Method.get)) {
            return StorageTypeResponse.newBuilder()
                    .addAllModels(new StorageTypeService().getStorageTypeModels(request.getModel().getId()))
                    .build();
        } else if (request.getMethod().equals(Method.create)) {
            return null;
        } else {
            return StorageTypeResponse.newBuilder()
                    .addModels(StorageTypeModel.newBuilder().setName("a").build())
                    .build();
        }
    }
}
