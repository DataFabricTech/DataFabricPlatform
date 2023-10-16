package com.mobigen.datafabric.core.controller;

import com.mobigen.libs.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import com.mobigen.libs.grpc.Storage.*;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
class StorageServiceImplTest {

    private InProcessServer server;
    private ManagedChannel channel;
    private StorageServiceGrpc.StorageServiceBlockingStub blockingStub;
    private StorageServiceGrpc.StorageServiceStub asyncStub;
    private final UUID testId = UUID.randomUUID();


    @BeforeEach
    public void beforeEachTest() throws InstantiationException, IllegalAccessException, IOException, InterruptedException {
        server = new InProcessServer();
        StorageServiceCallBack cb = new StorageServiceImpl();
        StorageService service = new StorageService( cb );
        server.addService(service);
        server.start();
        channel = InProcessChannelBuilder
                .forName("test")
                .directExecutor()
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        blockingStub = StorageServiceGrpc.newBlockingStub(channel);
        asyncStub = StorageServiceGrpc.newStub(channel);
    }

    @AfterEach
    public void afterEachTest() throws InterruptedException {
        channel.shutdownNow();
        server.stop();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @org.junit.jupiter.api.Test
    void overview() {
    }

    @org.junit.jupiter.api.Test
    public void getTypeByIdTest() throws InterruptedException {
        try {
            var res = blockingStub.storageType(StorageTypeRequest.newBuilder().build());
            var expect = StorageTypeModel.newBuilder()
                    .setName("MySQL")
                    .setId(String.valueOf(testId))
                    .build();
            System.out.println(res);

        } finally {
            shutdown();
        }
    }
}