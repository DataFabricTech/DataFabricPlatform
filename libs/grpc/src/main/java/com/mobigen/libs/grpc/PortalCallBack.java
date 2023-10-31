package com.mobigen.libs.grpc;

import com.google.protobuf.Empty;
import com.mobigen.datafabric.share.protobuf.Portal.*;

public interface PortalCallBack {

    ResSearch search(ReqSearch request);
    ResRecentSearches recentSearches(Empty request);
}
