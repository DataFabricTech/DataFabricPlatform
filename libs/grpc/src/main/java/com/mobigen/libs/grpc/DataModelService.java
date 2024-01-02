package com.mobigen.libs.grpc;

import com.mobigen.datafabric.share.protobuf.DataModelOuterClass;
import com.mobigen.datafabric.share.protobuf.DataModelServiceGrpc;
import com.mobigen.datafabric.share.protobuf.Utilities;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DataModelService extends DataModelServiceGrpc.DataModelServiceImplBase {

    DataModelServiceCallBack callBack;

    public DataModelService( DataModelServiceCallBack callBack) {
        log.debug("Init Data Model Service");
        this.callBack = callBack;
    }

    @Override
    public void preview( Utilities.ReqId request, StreamObserver<DataModelOuterClass.DataModelPreview> responseObserver ) {
        responseObserver.onNext(callBack.preview(request));
        responseObserver.onCompleted();
    }

    @Override
    public void default_( Utilities.ReqId request, StreamObserver<DataModelOuterClass.DataModelDefault> responseObserver ) {
        responseObserver.onNext(callBack.defaultInfo(request));
        responseObserver.onCompleted();
    }

    @Override
    public void userMetadata( DataModelOuterClass.ReqMetaUpdate request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.updateMetadata(request));
        responseObserver.onCompleted();
    }

    @Override
    public void tag( DataModelOuterClass.ReqTagUpdate request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.updateTag(request));
        responseObserver.onCompleted();
    }

    @Override
    public void downloadRequest( Utilities.ReqId request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.downloadRequest(request));
        responseObserver.onCompleted();
    }

    @Override
    public void addComment( DataModelOuterClass.ReqRatingAndComment request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.addComment(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateComment( DataModelOuterClass.ReqRatingAndComment request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.updateComment(request));
        responseObserver.onCompleted();
    }

    @Override
    public void delComment( DataModelOuterClass.ReqRatingAndComment request, StreamObserver<Utilities.CommonResponse> responseObserver ) {
        responseObserver.onNext(callBack.deleteComment(request));
        responseObserver.onCompleted();
    }

    @Override
    public void allDataSummary( DataModelOuterClass.DataModelSearch request, StreamObserver<DataModelOuterClass.ResDataModels> responseObserver ) {
        responseObserver.onNext(callBack.allDataSummary(request));
        responseObserver.onCompleted();
    }

    @Override
    public void allData( DataModelOuterClass.DataModelSearch request, StreamObserver<DataModelOuterClass.ResDataModels> responseObserver ) {
        responseObserver.onNext(callBack.allData(request));
        responseObserver.onCompleted();
    }
}