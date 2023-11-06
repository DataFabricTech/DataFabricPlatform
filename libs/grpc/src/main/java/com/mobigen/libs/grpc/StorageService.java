package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.StorageServiceGrpc;
import com.mobigen.datafabric.share.protobuf.Utilities;
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
    public void overview(Empty request, StreamObserver<StorageOuterClass.ResStorageOverview> responseObserver) {
        responseObserver.onNext(callBack.overview());
        responseObserver.onCompleted();
    }

    @Override
    public void search(StorageOuterClass.ReqStorageSearch request, StreamObserver<StorageOuterClass.ResStorages> responseObserver) {
        responseObserver.onNext(callBack.search(request));
        responseObserver.onCompleted();
    }

    @Override
    public void status(Utilities.ReqId request, StreamObserver<StorageOuterClass.ResStorage> responseObserver) {
        responseObserver.onNext(callBack.status(request));
        responseObserver.onCompleted();
    }

    @MethodMonitor
    @Override
    public void default_(Utilities.ReqId request, StreamObserver<StorageOuterClass.ResStorage> responseObserver) {
        responseObserver.onNext(callBack.default_(request));
        responseObserver.onCompleted();
    }

    @Override
    public void advanced(Utilities.ReqId request, StreamObserver<StorageOuterClass.ResStorage> responseObserver) {
        responseObserver.onNext(callBack.advanced(request));
        responseObserver.onCompleted();
    }

    @Override
    public void browse(StorageOuterClass.ReqStorageBrowse request, StreamObserver<StorageOuterClass.ResStorageBrowse> responseObserver) {
        responseObserver.onNext(callBack.browse(request));
        responseObserver.onCompleted();
    }

    @Override
    public void browseDefault(StorageOuterClass.ReqStorageBrowse request, StreamObserver<StorageOuterClass.ResStorageBrowseDefault> responseObserver) {
        responseObserver.onNext(callBack.browseDefault());
        responseObserver.onCompleted();
    }

    @Override
    public void connectTest(StorageOuterClass.ConnInfo request, StreamObserver<Utilities.CommonResponse> responseObserver) {
        responseObserver.onNext(callBack.connectTest(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addStorage(StorageOuterClass.Storage request, StreamObserver<Utilities.CommonResponse> responseObserver) {
        responseObserver.onNext(callBack.addStorage(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateStorage(StorageOuterClass.Storage request, StreamObserver<Utilities.CommonResponse> responseObserver) {
        responseObserver.onNext(callBack.updateStorage());
        responseObserver.onCompleted();
    }

    @Override
    public void connectedData(Utilities.ReqId request, StreamObserver<StorageOuterClass.ResConnectedData> responseObserver) {
        responseObserver.onNext(callBack.connectedData());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteStorage(Utilities.ReqId request, StreamObserver<Utilities.CommonResponse> responseObserver) {
        responseObserver.onNext(callBack.deleteStorage(request));
        responseObserver.onCompleted();
    }
//    @Override
//    @MethodMonitor
//    public void storageType(StorageTypeRequest request, StreamObserver<StorageTypeResponse> responseObserver) {
//        responseObserver.onNext(callBack.storageType(request));
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    @MethodMonitor
//    public void adaptor(AdaptorRequest request, StreamObserver<AdaptorResponse> responseObserver) {
//        responseObserver.onNext(callBack.adaptor(request));
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    @MethodMonitor
//    public void info(InfoRequest request, StreamObserver<InfoResponse> responseObserver) {
//        responseObserver.onNext(callBack.info(request));
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    @MethodMonitor
//    public void connectTest(ConnectTestRequest request, StreamObserver<CommonResponse> responseObserver) {
//        responseObserver.onNext(callBack.connectTest(request));
//        responseObserver.onCompleted();
//    }
}