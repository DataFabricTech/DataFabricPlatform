package com.mobigen.libs.grpc;

import com.mobigen.datafabric.share.protobuf.DataLayer.*;
import com.mobigen.datafabric.share.protobuf.DataLayerGRPCServiceGrpc;
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
    public void execute(ReqExecute request, StreamObserver<ResExecute> responseObserver) {
        responseObserver.onNext(callBack.execute(request));
        responseObserver.onCompleted();
    }

    @Override
    public void executeBatch(ReqBatchExecute request, StreamObserver<ResBatchExecute> responseObserver) {
        responseObserver.onNext(callBack.executeBatch(request));
        responseObserver.onCompleted();
    }
}
