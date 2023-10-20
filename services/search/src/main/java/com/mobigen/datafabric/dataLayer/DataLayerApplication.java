package com.mobigen.datafabric.dataLayer;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.datafabric.dataLayer.service.OpenSearchService;
import com.mobigen.libs.grpc.DataLayerCallBack;
import com.mobigen.libs.grpc.DataLayerService;
import com.mobigen.libs.grpc.GRPCServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DataLayerApplication {
    public static void main(String[] args) {
        log.debug("Data Layer Main Start");
        GRPCServer server = new GRPCServer(9360, 10);
        var appConfig = new AppConfig();
        DataLayerCallBack cb = new DataLayerServiceImpl(appConfig.openSearchService(), appConfig.rdbmsService());
        DataLayerService service = new DataLayerService(cb);


        try {
            server.addService(service);

            // Init Index
            appConfig.openSearchRepository().createIndex();

            server.start();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}