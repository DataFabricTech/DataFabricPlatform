package com.mobigen.datafabric.core;

import com.mobigen.datafabric.core.controller.StorageServiceImpl;
import com.mobigen.libs.grpc.GRPCServer;
import com.mobigen.libs.grpc.StorageService;
import com.mobigen.libs.grpc.StorageServiceCallBack;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        log.error("Hello World!");
        GRPCServer server = new GRPCServer(9360, 10);
        try {
            initService(server);
            server.start();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initService(GRPCServer server) throws IOException {
        StorageServiceCallBack cb = new StorageServiceImpl();
        StorageService service = new StorageService(cb);
        server.addService(service);
    }
}
