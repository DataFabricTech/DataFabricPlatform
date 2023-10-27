package com.mobigen.libs.grpc;

import com.mobigen.libs.grpc.Storage.*;
import com.mobigen.libs.grpc.aop.MethodMonitor;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StorageService extends StorageServiceGrpc.StorageServiceImplBase {

    StorageServiceCallBack callBack;

    public StorageService(StorageServiceCallBack callBack) {
        log.debug("Init Storage Service");
        this.callBack = callBack;
    }

    @Override
    @MethodMonitor
    public void overview(Empty request, StreamObserver<OverviewResponse> responseObserver) {
        responseObserver.onNext(callBack.overview());
        responseObserver.onCompleted();
    }

    @Override
    @MethodMonitor
    public void storageType(StorageTypeRequest request, StreamObserver<StorageTypeResponse> responseObserver) {
        responseObserver.onNext(callBack.storageType(request));
        responseObserver.onCompleted();
    }

    @Override
    @MethodMonitor
    public void adaptor(AdaptorRequest request, StreamObserver<AdaptorResponse> responseObserver) {
        responseObserver.onNext(callBack.adaptor(request));
        responseObserver.onCompleted();
    }

    @Override
    @MethodMonitor
    public void info(InfoRequest request, StreamObserver<InfoResponse> responseObserver) {
        responseObserver.onNext(callBack.info(request));
        responseObserver.onCompleted();
    }

    @Override
    @MethodMonitor
    public void connectTest(ConnectTestRequest request, StreamObserver<CommonResponse> responseObserver) {
        responseObserver.onNext(callBack.connectTest(request));
        responseObserver.onCompleted();
    }
}