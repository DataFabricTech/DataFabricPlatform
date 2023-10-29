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
    public void execute(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
        log.debug(">> DataLayer:execute");
        responseObserver.onNext(callBack.execute(request.getSql()));
        log.debug("<< DataLayer:execute");
        responseObserver.onCompleted();
    }

    @Override
    public void executeBatch(BatchRequest request, StreamObserver<BatchResponse> responseObserver) {
        log.debug(">> DataLayer:Receiver");
        responseObserver.onNext(callBack.executeBatch(request.getSqlList().toArray(new String[0])));
        log.debug("<< DataLayer:Receiver");
        responseObserver.onCompleted();
    }

    @Override
    public void search(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        log.debug(">> DataLayer:Search");
        var input = request.getInput();
        var detailSearch = request.getDetailSearch();
        var filterSearch = request.getFilterSearch();
        var userId = request.getUserId();
        responseObserver.onNext(callBack.search(input, detailSearch, filterSearch, userId));
        log.debug("<< DataLayer:Search");
        responseObserver.onCompleted();
    }

    @Override
    public void recentSearch(RecentSearchesRequest request, StreamObserver<RecentSearchesResponse> responseObserver) {
        log.debug(">> DataLayer:Recent Search");
        responseObserver.onNext(callBack.recentSearch(request.getUserId()));
        log.debug("<< DataLayer:Recent Search");
        responseObserver.onCompleted();
    }

    @Override
    public void healthCheck(Empty request, StreamObserver<HealthCheckResponse> responseObserver) {
        log.debug(">> DataLayer: Health Check");
        responseObserver.onNext(callBack.healthCheck());
        log.debug("<< DataLayer: Health Check");
        responseObserver.onCompleted();
        responseObserver.onCompleted();
        responseObserver.onCompleted();
        responseObserver.onCompleted();
    }
}
