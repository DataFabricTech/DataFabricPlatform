package com.mobigen.datafabric.dataLayer.config;

import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.datafabric.dataLayer.repository.RDBMSRepository;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.datafabric.dataLayer.service.OpenSearchService;
import com.mobigen.datafabric.dataLayer.service.RDBMSService;
import com.mobigen.libs.configuration.Config;
import org.apache.commons.configuration.Configuration;
import org.opensearch.client.opensearch._types.OpenSearchException;

import java.sql.SQLException;

public class AppConfig {
    public AppConfig() {
    }

    public DataLayerServiceImpl dataLayerServiceImpl() throws SQLException, ClassNotFoundException {
        return new DataLayerServiceImpl(openSearchService(), rdbmsService(), dbConfig());
    }


    public OpenSearchService openSearchService() {
        return new OpenSearchService(openSearchRepository(), dbConfig());
    }

    public RDBMSService rdbmsService() throws SQLException, ClassNotFoundException {
        return new RDBMSService(rdbmsRepository());
    }

    public OpenSearchRepository openSearchRepository() throws OpenSearchException, NullPointerException {
        return new OpenSearchRepository(openSearchConfig());
    }

    public OpenSearchConfig openSearchConfig() {
        return new OpenSearchConfig(config());
    }

    public RDBMSRepository rdbmsRepository() throws SQLException, ClassNotFoundException {
        return new RDBMSRepository(dbConfig());
    }

    public DBConfig dbConfig() {
        return new DBConfig(config());
    }


    public Configuration config() {
        return new Config().getConfig();
    }
}
