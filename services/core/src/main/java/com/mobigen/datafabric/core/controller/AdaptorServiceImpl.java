package com.mobigen.datafabric.core.controller;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.core.services.storage.AdaptorService;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;
import com.mobigen.libs.grpc.AdaptorServiceCallBack;

/**
 * gRPC 의 request 를 받아 response 를 생성하는 콜백 클래스의 구현부
 * Adaptor 관련 서비스 제공
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class AdaptorServiceImpl implements AdaptorServiceCallBack {
    AdaptorService service = new AdaptorService();

    @Override
    public AdaptorOuterClass.ResSupportedStorageType getStorageType(Empty request) {
        AdaptorOuterClass.ResSupportedStorageType resSupportedStorageType;
        var builder = AdaptorOuterClass.ResSupportedStorageType.newBuilder();
        var storageTypes = service.getStorageTypes();
        if (storageTypes == null) {
            resSupportedStorageType = builder
                    .setCode("FAIL")
                    .setErrMsg("null")
                    .build();
        } else {
            resSupportedStorageType = builder
                    .setCode("OK")
                    .setData(
                            AdaptorOuterClass.ResSupportedStorageType.Data.newBuilder()
                                    .addAllSupportedStorageType(storageTypes)
                                    .build()
                    )
                    .build();
        }
        return resSupportedStorageType;
    }

    @Override
    public AdaptorOuterClass.ResAdaptors getAdaptors(AdaptorOuterClass.ReqAdaptors request) {
        AdaptorOuterClass.ResAdaptors resAdaptors;
        var builder = AdaptorOuterClass.ResAdaptors.newBuilder();
        var adaptors = service.getAdaptors();
        if (adaptors == null) {
            resAdaptors = builder
                    .setCode("FAIL")
                    .setErrMsg("null")
                    .build();
        } else {
            resAdaptors = builder
                    .setCode("OK")
                    .setData(
                            AdaptorOuterClass.ResAdaptors.Data.newBuilder()
                                    .addAllAdaptors(adaptors)
                                    .build()
                    )
                    .build();
        }
        return resAdaptors;
    }
}

