package com.mobigen.libs.grpc;

public interface StorageServiceCallBack {
    OverviewResponse overview();

    StorageTypeResponse storageType(StorageTypeRequest request);
}
