package com.mobigen.libs.grpc;

import com.mobigen.datafabric.share.protobuf.Portal.*;
import com.mobigen.datafabric.share.protobuf.PortalServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalService extends PortalServiceGrpc.PortalServiceImplBase {

    PortalCallBack callBack;
    public PortalService(PortalCallBack callBack) {
        log.debug("Init Portal Service");
        this.callBack = callBack;
    }
    @Override
    public void search(ReqSearch request, StreamObserver<ResSearch> responseObserver) {
        responseObserver.onNext(callBack.search(request));
        responseObserver.onCompleted();
    }

    @Override
    public void recentSearches(com.google.protobuf.Empty request, StreamObserver<ResRecentSearches> responseObserver) {
        responseObserver.onNext(callBack.recentSearches(request));
        responseObserver.onCompleted();
    }
}
