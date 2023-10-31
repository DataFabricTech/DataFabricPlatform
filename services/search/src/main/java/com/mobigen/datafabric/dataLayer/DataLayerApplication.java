package com.mobigen.datafabric.dataLayer;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.libs.grpc.DataLayerService;
import com.mobigen.libs.grpc.GRPCServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DataLayerApplication {
    public static void main(String[] args) {
        log.debug("Data Layer Main Start");
        GRPCServer server = new GRPCServer(9360, 10);
        try {
            initService(server);
            server.start();
        } catch (IOException |InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initService(GRPCServer server) throws IOException {
        var appConfig = new AppConfig();
        server.addService(new DataLayerService(new DataLayerServiceImpl(appConfig.dataLayerRepository(), appConfig.portalService(), appConfig.dbConfig())));
//        server.addService(new PortalService()); todo 여기에 createIndex 박기
    }
}