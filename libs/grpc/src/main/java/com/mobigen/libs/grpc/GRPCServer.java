package com.mobigen.libs.grpc;

import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GRPCServer {

    private static final Integer DEFAULT_PORT = 9360;

    private final ServerBuilder<?> serverBuilder;
    private Server server;

    // Constructor
    public GRPCServer(int port, int threadPoolSize) {
        // port check
        if ( port < 0 || port > 65535 ) {
            port = DEFAULT_PORT;
        }
        this.serverBuilder = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .executor( Executors.newFixedThreadPool(threadPoolSize) );
        this.serverBuilder.addService( ProtoReflectionService.newInstance() );
        log.debug("Init gRPC Server Builder. Port[ {} ], ThreadPool[ {} ]", port, threadPoolSize);
    }

    // Add Service
    public void addService( BindableService service) throws IOException {
        // service null check
        if ( service == null ) {
            // return parameter error
            throw new IOException("Parameter Service Is Null");
        }
        // debug print server service name and method list
        log.debug("Add gRPC Service. Service Name[ {} ]", service.bindService().getServiceDescriptor().getName());
        service.bindService().getServiceDescriptor().getMethods().forEach( method -> {
            log.debug("Add gRPC Service Method. Service Name[ {} ], Method Name[ {} ]",
                    service.bindService().getServiceDescriptor().getName(), method.getFullMethodName());
        });

        this.serverBuilder.addService( service );
    }

    /**
     * Start gRPC Server
     * gRPC 서버를 시작하는 함수로 내부에서 서버 종료를 감시하고 처리하는 스레드와 서버 종료까지 블록킹되는 기능이 있다.
     * 따라서 이 함수의 실행을 메인 스레드가 아닌 곳에서 실행은 지양해야 한다.
     */
    public void start() throws IOException, InterruptedException {
        // Build Server
        this.server = this.serverBuilder.build();
        // Check Server Service
        if ( this.server.getServices(  ).isEmpty() ) {
            throw new IOException("Server Service Is Empty. Please Add Service");
        }
        // Start Server
        this.server.start();
        log.info("Start gRPC Server. Listening Port [ {} ]", this.server.getPort());
        // Set Shutdown Hook
        addShutdownHook(this.server);    // Configures cleanup
        // Wait For Termination
        this.server.awaitTermination();  // Block until shutdown
    }

    // Stop gRPC Server
    private void stop() throws InterruptedException {
        log.info("Stop gRPC Server. Listening Port [ {} ]", this.server.getPort());
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    // Block Until Shutdown
    private static void addShutdownHook(final Server server) {
        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                // Start graceful shutdown
                server.shutdown();
                try {
                    // Wait for RPCs to complete processing
                    if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                        // That was plenty of time. Let's cancel the remaining RPCs
                        server.shutdownNow();
                        // shutdownNow isn't instantaneous, so give a bit of time to clean resources up
                        // gracefully. Normally this will be well under a second.
                        server.awaitTermination(5, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException ex) {
                    server.shutdownNow();
                }
            }
        });
    }
}
