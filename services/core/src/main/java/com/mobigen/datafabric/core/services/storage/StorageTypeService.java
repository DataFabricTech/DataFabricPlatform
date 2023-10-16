package com.mobigen.datafabric.core.services.storage;

import com.mobigen.libs.grpc.StorageTypeModel;

public class StorageTypeService {
    public StorageTypeModel getStorageTypeModel(String id) {
        // select id, name from DataStorageType where id = id;

        return StorageTypeModel.newBuilder().setName("aaaa").build();
    }
}
