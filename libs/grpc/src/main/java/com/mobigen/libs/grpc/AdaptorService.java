package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;
import com.mobigen.datafabric.share.protobuf.AdaptorServiceGrpc;
import com.mobigen.libs.grpc.aop.MethodMonitor;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC AdaptorService 구현부
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public final class AdaptorService extends AdaptorServiceGrpc.AdaptorServiceImplBase {

    AdaptorServiceCallBack callBack;

    public AdaptorService(AdaptorServiceCallBack callBack) {
        log.debug("Init AdaptorService");
        this.callBack = callBack;
    }

    @Override
    @MethodMonitor
    public void getStorageType(Empty request, StreamObserver<AdaptorOuterClass.ResSupportedStorageType> responseObserver) {
        responseObserver.onNext(callBack.getStorageType(request));
        responseObserver.onCompleted();
    }

    @Override
    @MethodMonitor
    public void getAdaptors(AdaptorOuterClass.ReqAdaptors request, StreamObserver<AdaptorOuterClass.ResAdaptors> responseObserver) {
        responseObserver.onNext(callBack.getAdaptors(request));
        responseObserver.onCompleted();
    }
}