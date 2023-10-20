package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataLayerService extends DataLayerGRPCServiceGrpc.DataLayerGRPCServiceImplBase {
    DataLayerCallBack callBack;

    public DataLayerService(DataLayerCallBack callBack) {
        log.debug("Init DataLayer Service");
        this.callBack = callBack;
    }

    @Override
    public void receiver(QueryRequestMessage request, StreamObserver<QueryResponseMessage> responseObserver) {
        log.debug(">> DataLayer:Receiver");
        responseObserver.onNext( callBack.query(request.getQuery()));
        log.debug("<< DataLayer:Receiver");
        responseObserver.onCompleted();
    }

    @Override
    public void search(SearchRequestMessage request, StreamObserver<SearchResponseMessage> responseObserver) {
        super.search(request, responseObserver);
    }

    @Override
    public void recentSearch(RecentSearchesRequestMessage request, StreamObserver<RecentSearchesResponseMessage> responseObserver) {
        super.recentSearch(request, responseObserver);
    }

    @Override
    public void healthCheck(Empty request, StreamObserver<HealthCheckResponseMessage> responseObserver) {
        super.healthCheck(request, responseObserver);
    }
}
