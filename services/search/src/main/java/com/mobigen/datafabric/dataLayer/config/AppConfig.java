package com.mobigen.datafabric.dataLayer.config;

import com.mobigen.datafabric.dataLayer.repository.PortalRepository;
import com.mobigen.datafabric.dataLayer.repository.DataLayerRepository;
import com.mobigen.datafabric.dataLayer.service.DataLayerServiceImpl;
import com.mobigen.datafabric.dataLayer.service.PortalServiceImpl;
import com.mobigen.libs.configuration.Config;
import org.apache.commons.configuration.Configuration;

import java.sql.SQLException;

public class AppConfig {
    public AppConfig() {
    }

    public PortalServiceImpl portalService() {
        return new PortalServiceImpl(portalRepository(), dbConfig());
    }

    public PortalRepository portalRepository() {
        return new PortalRepository(portalConfig());
    }

    public PortalConfig portalConfig() {
        return new PortalConfig(config());
    }

    public DataLayerServiceImpl dataLayerServiceImpl() throws SQLException, ClassNotFoundException {
        return new DataLayerServiceImpl(dataLayerRepository(), portalService(), dbConfig());
    }

    public DataLayerRepository dataLayerRepository() {
        return new DataLayerRepository(dbConfig());
    }

    public DBConfig dbConfig() {
        return new DBConfig(config());
    }

    public Configuration config() {
        return new Config().getConfig();
    }
}
