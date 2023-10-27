package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.share.protobuf.AdaptorOuterClass;

/**
 * 콜백 함수
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public interface AdaptorServiceCallBack {
    AdaptorOuterClass.ResSupportedStorageType getStorageType(Empty request);

    AdaptorOuterClass.ResAdaptors getAdaptors(AdaptorOuterClass.ReqAdaptors request);
}
