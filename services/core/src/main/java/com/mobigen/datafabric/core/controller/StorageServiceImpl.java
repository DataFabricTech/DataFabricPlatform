package com.mobigen.datafabric.core.controller;

import com.mobigen.datafabric.core.JdbcConnector;
import com.mobigen.datafabric.core.services.storage.direct.AdaptorService;
import com.mobigen.datafabric.core.services.storage.direct.InfoService;
import com.mobigen.datafabric.core.services.storage.direct.StorageTypeService;
import com.mobigen.libs.grpc.Method;
import com.mobigen.libs.grpc.Storage.*;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * gRPC 의 request 를 받아 response 를 생성하는 콜백 클래스의 구현부
 * Storage 관련 서비스 제공
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 * @deprecated
 */
@Slf4j
@Deprecated
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

    @Override
    public CommonResponse connectTest(ConnectTestRequest request) {
        Map<String, String> basic = new HashMap<>();

        for (var op : request.getBasicOptionsList()) {
            var key = op.getKey();
            var type = op.getValueType();
            var value = op.getValue();
            basic.put(key, value);
        }

        Properties addition = new Properties();

        for (var op : request.getAdditionalOptionsList()) {
            var key = op.getKey();
            var value = op.getValue();
            addition.setProperty(key, value);
        }

        var urlFormat = request.getUrlFormat();
        try (var connector = new JdbcConnector(urlFormat, basic)) {
            var conn = connector.connect(addition);
            var cur = conn.cursor();
            cur.execute("select 1");
            var result = cur.getResultSet();
            System.out.println(result);
            result.next();
            var value = result.getString(1);
            if (value.equals("1")) {
                return CommonResponse.newBuilder().setData("success").build();
            } else {
                return CommonResponse.newBuilder().setData("fail").build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
