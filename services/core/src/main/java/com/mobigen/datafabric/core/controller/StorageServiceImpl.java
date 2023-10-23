package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.services.storage.AdaptorService;
import com.mobigen.datafabric.core.services.storage.InfoService;
import com.mobigen.datafabric.core.services.storage.StorageTypeService;
import com.mobigen.libs.grpc.Method;
import com.mobigen.libs.grpc.Storage.*;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

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
                    .addAllModels(new StorageTypeService().getStorageTypeModels())
                    .build();
        } else {
            throw new RuntimeException("Not supported method");
        }
    }

    @Override
    public AdaptorResponse adaptor(AdaptorRequest request) {
        var service = new AdaptorService();
        if (request.getMethod().equals(Method.get)) {
            return AdaptorResponse.newBuilder()
                    .addModels(service.getAdaptor(request.getModel().getId()))
                    .build();
        } else if (request.getMethod().equals(Method.list)) {
            return AdaptorResponse.newBuilder()
                    .addAllModels(service.getAdaptors(request.getModel().getStorageType()))
                    .build();
        } else if (request.getMethod().equals(Method.create)) {
            try {
                return AdaptorResponse.newBuilder().addModels(service.createAdaptor(request.getModel())).build();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (request.getMethod().equals(Method.update)) {
            return AdaptorResponse.newBuilder().addModels(service.updateAdaptor(request.getModel())).build();
        } else {
            return null;

        }
    }

    @Override
    public InfoResponse info(InfoRequest request) {
        var service = new InfoService();
        if (request.getMethod().equals(Method.list)) {
            return InfoResponse.newBuilder()
                    .addAllModels(service.getInfos())
                    .build();
        } else if (request.getMethod().equals(Method.create)) {
            try {
                return InfoResponse.newBuilder()
                        .addModels(service.createInfo(request.getModel()))
                        .build();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (request.getMethod().equals(Method.update)) {
            return InfoResponse.newBuilder()
//                    .addModels(service.updateAdaptor(request.getModel()))
                    .build();
        } else {
            return null;

        }
    }
}
