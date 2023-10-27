package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;

public interface AdaptorServiceCallBack {
    AdaptorOuterClass.ResSupportedStorageType getStorageType(Empty request);

    AdaptorOuterClass.ResAdaptors getAdaptors(AdaptorOuterClass.ReqAdaptors request);
}
