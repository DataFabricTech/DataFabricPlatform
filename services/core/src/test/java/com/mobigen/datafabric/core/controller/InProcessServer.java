//package com.mobigen.datafabric.core.controller;
//
//import com.mobigen.libs.grpc.GRPCServer;
//import io.grpc.*;
//import io.grpc.inprocess.InProcessServerBuilder;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.util.concurrent.Executors;
//
//
///**
// * InProcessServer that manages startup/shutdown of a service within the same process as the client is running. Used for unit testing purposes.
// *
// * @author be
// */
//@Slf4j
//public class InProcessServer {
//
//    private Server server;
//    private final ServerBuilder<?> serverBuilder;
//
//    public InProcessServer() {
//        this.serverBuilder = InProcessServerBuilder
//                .forName("test")
//                .directExecutor();
//    }
//
//    public void addService(BindableService service) {
//        this.serverBuilder.addService(service);
//    }
//
//    public void start() throws IOException, InterruptedException {
//        // Build Server
//        this.server = this.serverBuilder.build();
//        // Check Server Service
//        if (this.server.getServices().isEmpty()) {
//            throw new IOException("Server Service Is Empty. Please Add Service");
//        }
//        // Start Server
//        this.server.start();
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
//                System.err.println("*** shutting down gRPC server since JVM is shutting down");
//                InProcessServer.this.stop();
//                System.err.println("*** server shut down");
//            }
//        });
//    }
//
//    void stop() {
//        if (server != null) {
//            server.shutdown();
//        }
//    }
//}