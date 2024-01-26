package com.mobigen.datafabric.dataLayer.config;

import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.datafabric.dataLayer.service.PortalServiceImpl;
import com.mobigen.libs.configuration.Configuration;

public class AppConfig {
    public AppConfig() {
    }

    public PortalServiceImpl portalService() {
        return new PortalServiceImpl(portalRepository(), dbConfig(), portalConfig());
    }

    public PortalRepository portalRepository() {
        return new PortalRepository(portalConfig());
    }

    public PortalConfig portalConfig() {
        return new PortalConfig(config());
    }

    public DataLayerServiceImpl dataLayerServiceImpl() {
        return new DataLayerServiceImpl(dataLayerRepository(), portalService());
    }

    public DataLayerRepository dataLayerRepository() {
        return new DataLayerRepository(dbConfig());
    }

    public DBConfig dbConfig() {
        return new DBConfig(config());
    }

    public org.apache.commons.configuration.Configuration config() {
        return new Configuration().getConfig();
    }
}
