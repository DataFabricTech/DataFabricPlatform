package com.mobigen.libs.grpc;

import com.mobigen.libs.grpc.DataLayer.*;
import io.grpc.stub.StreamObserver;

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
                                        Cell.newBuilder().setStringValue("a value").build(),
                                        Cell.newBuilder().setInt32Value(13).build()
                                )
                        ).build()
                ))
                .build());
        responseObserver.onCompleted();
    }
}
