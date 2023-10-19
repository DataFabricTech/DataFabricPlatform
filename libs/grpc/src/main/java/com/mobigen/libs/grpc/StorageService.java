package com.mobigen.libs.grpc;

import com.mobigen.libs.grpc.Storage.*;
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
    public void overview(Empty request, StreamObserver<OverviewResponse> responseObserver) {

        log.debug(">> Storage:Overview");
        responseObserver.onNext(callBack.overview());
        log.debug("<< Storage:Overview");
        responseObserver.onCompleted();
    }

    @Override
    public void storageType(StorageTypeRequest request, StreamObserver<StorageTypeResponse> responseObserver) {
        log.debug(">> Storage:storageType");
        responseObserver.onNext(callBack.storageType(request));
        log.debug("<< Storage:storageType");
        responseObserver.onCompleted();
    }

    @Override
    public void adaptor(AdaptorRequest request, StreamObserver<AdaptorResponse> responseObserver) {
        log.debug(">> Storage:adaptor");
        responseObserver.onNext(callBack.adaptor(request));
        log.debug("<< Storage:adaptor");
        responseObserver.onCompleted();
    }

    @Override
    public void info(InfoRequest request, StreamObserver<InfoResponse> responseObserver) {
        log.debug(">> Storage:adaptor");
        responseObserver.onNext(callBack.info(request));
        log.debug("<< Storage:adaptor");
        responseObserver.onCompleted();
    }
}