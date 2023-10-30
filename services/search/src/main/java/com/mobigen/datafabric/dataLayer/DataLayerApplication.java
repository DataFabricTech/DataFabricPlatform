package com.mobigen.datafabric.dataLayer;

import com.mobigen.datafabric.dataLayer.config.AppConfig;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.libs.grpc.DataLayerCallBack;
import com.mobigen.libs.grpc.DataLayerService;
import com.mobigen.libs.grpc.GRPCServer;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch._types.OpenSearchException;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class DataLayerApplication {
    public static void main(String[] args) {
        log.debug("Data Layer Main Start");
        GRPCServer server = new GRPCServer(9360, 10);
        var appConfig = new AppConfig();
        DataLayerCallBack cb;
        try {
            cb = new DataLayerServiceImpl(appConfig.openSearchService(), appConfig.rdbmsService(), appConfig.dbConfig(), appConfig.openSearchConfig());
        } catch (SQLException | ClassNotFoundException e) {
            // todo connect error
            throw new RuntimeException(e);
        }
        DataLayerService service = new DataLayerService(cb);


        try {
            server.addService(service);

            // Init Index
            appConfig.openSearchService().createIndex();

            server.start();
        } catch (OpenSearchException e) {
            throw new RuntimeException(e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}