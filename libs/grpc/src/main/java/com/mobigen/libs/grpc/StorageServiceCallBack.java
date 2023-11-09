package com.mobigen.libs.grpc;


import com.mobigen.datafabric.share.protobuf.StorageOuterClass;
import com.mobigen.datafabric.share.protobuf.Utilities;

public interface StorageServiceCallBack {
    StorageOuterClass.ResStorageOverview overview();

    StorageOuterClass.ResStorages search(StorageOuterClass.ReqStorageSearch request);

    StorageOuterClass.ResStorage status(Utilities.ReqId request);

    StorageOuterClass.ResStorage default_(Utilities.ReqId request);

    StorageOuterClass.ResStorage advanced(Utilities.ReqId request);

    StorageOuterClass.ResStorageBrowse browse(StorageOuterClass.ReqStorageBrowse request);

    StorageOuterClass.ResStorageBrowseDefault browseDefault();

    Utilities.CommonResponse connectTest(StorageOuterClass.ConnInfo request);

    Utilities.CommonResponse addStorage(StorageOuterClass.Storage request);

    Utilities.CommonResponse updateStorage(StorageOuterClass.Storage request);

    StorageOuterClass.ResConnectedData connectedData();

    Utilities.CommonResponse deleteStorage(Utilities.ReqId request);
//
//    StorageTypeResponse storageType(StorageTypeRequest request);
//
//    AdaptorResponse adaptor(AdaptorRequest request);
//
//    InfoResponse info(InfoRequest request);
//
//    CommonResponse connectTest(ConnectTestRequest request);
}
