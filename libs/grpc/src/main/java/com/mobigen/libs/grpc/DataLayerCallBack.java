package com.mobigen.libs.grpc;


import com.mobigen.datafabric.share.protobuf.DataLayer.*;

public interface DataLayerCallBack {
    ResExecute execute(ReqExecute sql);

    ResBatchExecute executeBatch(ReqBatchExecute sql);
}
