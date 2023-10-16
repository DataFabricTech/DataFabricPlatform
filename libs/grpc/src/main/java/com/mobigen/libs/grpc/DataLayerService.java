package com.mobigen.libs.grpc;

import io.grpc.stub.StreamObserver;
import com.mobigen.libs.grpc.DataLayer.*;

import java.util.List;

public class DataLayerService extends DataLayerServiceGrpc.DataLayerServiceImplBase {
    @Override
    public void receiver(QueryGRPCRequestMessage request, StreamObserver<QueryGRPCResponseMessage> responseObserver) {
        responseObserver.onNext(DataLayer.QueryGRPCResponseMessage.newBuilder()
                .addAllColumn(List.of(
                        Column.newBuilder().setColumnName("A").setType("string").build(),
                        Column.newBuilder().setColumnName("B").setType("int32").build()
                ))
                .addAllRows(List.of(
                        Rows.newBuilder().addAllRow(
                                List.of(
                                        Row.newBuilder().setStringValue("a value").build(),
                                        Row.newBuilder().setInt32Value(13).build()
                                )
                        ).build()
                ))
                .build());
        responseObserver.onCompleted();
    }
}
