package com.mobigen.datafabric.extraction;

import com.mobigen.libs.grpc.GRPCServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ExtractionApplication {
    public static void main(String[] args) {
        log.debug("Extractor Main Start");
        GRPCServer server = new GRPCServer(9360, 10);
        try {
            initService(server);
            server.start();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initService(GRPCServer server) throws IOException {
//        server.addService(new DataLayerService(new DataLayerServiceImpl(appConfig.dataLayerRepository(), appConfig.portalService())));
//        server.addService(new PortalService(new PortalServiceImpl(appConfig.portalRepository(), appConfig.dbConfig(), appConfig.portalConfig())));
    }
}