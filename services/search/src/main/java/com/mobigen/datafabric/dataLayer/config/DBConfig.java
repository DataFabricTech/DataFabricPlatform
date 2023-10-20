package com.mobigen.datafabric.dataLayer.config;

import lombok.Getter;
import org.apache.commons.configuration.Configuration;


@Getter
public class DBConfig {
    private final Configuration config;
    private final String tableName;
    private final String url;
    private final String username;
    private final String password;


    public DBConfig(Configuration config) {
        this.config = config;
        this.tableName = config.getString("datasource.table_name");
        this.url = config.getString("datasource.url");
        this.username = config.getString("datasource.username");
        this.password = config.getString("datasource.password");
    }
}
