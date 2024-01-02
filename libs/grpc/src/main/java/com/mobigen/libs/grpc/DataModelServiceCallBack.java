package com.mobigen.libs.grpc;


import com.mobigen.datafabric.share.protobuf.DataModelOuterClass;
import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;

public interface DataModelServiceCallBack {
    // POST /data/v1/preview - 미리보기
    // rpc Preview(ReqId) returns (DataModelPreview);
    DataModelOuterClass.DataModelPreview preview( Utilities.ReqId request );

    // POST /data/v1/default - 데이터 상세 보기 - 기본 정보
    // rpc Default(ReqId) returns (DataModelDefault);
    DataModelOuterClass.DataModelDefault defaultInfo(Utilities.ReqId request);

    // POST /data/v1/metadata - 사용자 설정 메타 데이터 업데이트
    // rpc UserMetadata(ReqMetaUpdate) returns (CommonResponse);
    Utilities.CommonResponse updateMetadata( DataModelOuterClass.ReqMetaUpdate request );

    //  POST /data/v1/tag - 데이터 태그 업데이트
    // rpc Tag(ReqTagUpdate) returns (CommonResponse);
    Utilities.CommonResponse updateTag ( DataModelOuterClass.ReqTagUpdate request );

    //  POST /data/v1/download-request - 다운로드 요청
    // rpc DownloadRequest(ReqId) returns (CommonResponse);
    Utilities.CommonResponse downloadRequest ( Utilities.ReqId request );

    //  POST /data/v1/comment/add - 데이터 평가와 댓글 추가
    // rpc AddComment(ReqRatingAndComment) returns (CommonResponse);
    Utilities.CommonResponse addComment ( DataModelOuterClass.ReqRatingAndComment request );

    //  POST /data/v1/comment/update - 데이터 평가와 댓글 업데이트
    // rpc UpdateComment(ReqRatingAndComment) returns (CommonResponse);
    Utilities.CommonResponse updateComment ( DataModelOuterClass.ReqRatingAndComment request );

    //  POST /data/v1/comment/delete - 데이터 평가와 댓글 삭제
    // rpc DelComment(ReqRatingAndComment) returns (CommonResponse);
    Utilities.CommonResponse deleteComment ( DataModelOuterClass.ReqRatingAndComment request );

    //  POST /data/v1/all-data/summary -  데이터 모델 조회 축약 버전
    // rpc AllDataSummary(DataModelSearch) returns (ResDataModels);
    DataModelOuterClass.ResDataModels allDataSummary ( DataModelOuterClass.DataModelSearch request );

    //  POST /data/v1/all-data          - 데이터 모델 조회
    // rpc AllData(DataModelSearch) returns (ResDataModels);
    DataModelOuterClass.ResDataModels allData ( DataModelOuterClass.DataModelSearch request );
}
