package com.mobigen.datafabric.core.services;

import com.mobigen.libs.grpc.OverviewResponse;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageServiceImpl implements StorageServiceCallBack {

    @Override
    public OverviewResponse overview() {
        return OverviewResponse.newBuilder().setTotalStorage( 10 ).build();
    }
}
