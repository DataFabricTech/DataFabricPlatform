package com.mobigen.datafabric.dataLayer.config;

import com.mobigen.datafabric.dataLayer.repository.MultiRepository;
import com.mobigen.datafabric.dataLayer.repository.OpenSearchRepository;
import com.mobigen.datafabric.dataLayer.repository.RDBMSRepository;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.datafabric.dataLayer.service.OpenSearchService;
import com.mobigen.datafabric.dataLayer.service.RDBMSService;
import com.mobigen.libs.configuration.Config;
import org.apache.commons.configuration.Configuration;

public class AppConfig {
    public AppConfig() {
    }

    public DataLayerServiceImpl dataLayerService() {
        return new DataLayerServiceImpl(openSearchService(), rdbmsService());
    }

    public OpenSearchService openSearchService() {
        return new OpenSearchService(openSearchRepository());
    }

    public RDBMSService rdbmsService() {
        return new RDBMSService(multiRepository(), rdbmsRepository(), dbConfig());
    }

    public MultiRepository multiRepository() {
        return new MultiRepository(openSearchRepository(), rdbmsRepository());
    }

    public OpenSearchRepository openSearchRepository() {
        return new OpenSearchRepository(openSearchConfig());
    }

    public OpenSearchConfig openSearchConfig() {
        return new OpenSearchConfig(config());
    }

    public RDBMSRepository rdbmsRepository() {
        return new RDBMSRepository(dbConfig());
    }

    public DBConfig dbConfig() {
        return new DBConfig(config());
    }

    public Configuration config() {
        return new Config().getConfig();
    }
}
