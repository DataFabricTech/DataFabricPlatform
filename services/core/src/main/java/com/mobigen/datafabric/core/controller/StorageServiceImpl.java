package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.StorageTypeService;
import com.mobigen.libs.grpc.*;
import lombok.extern.slf4j.Slf4j;

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
                    .addModels(new StorageTypeService().getStorageTypeModel(request.getModel().getId()))
                    .build();
        } else {
            return StorageTypeResponse.newBuilder()
                    .addModels(StorageTypeModel.newBuilder().setName("a").build())
                    .build();
        }
    }
}
