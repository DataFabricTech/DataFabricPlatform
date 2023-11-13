package com.mobigen.datafabric.dataLayer.config;

import lombok.Getter;
import org.apache.commons.configuration.Configuration;

import java.util.List;


@Getter
public class DBConfig {
    private final Configuration config;
    private final List<String> dataModel;
    private final List<String> storage;
    private final String dataTag;
    private final String url;
    private final String username;
    private final String password;


    public DBConfig(Configuration config) {
        this.config = config;
        this.dataModel = config.getList("datasource.table_name.data_model");
        this.storage = config.getList("datasource.table_name.storage");
        this.dataTag = config.getString("datasource.table_name.data_tag");
        this.url = config.getString("datasource.url");
        this.username = config.getString("datasource.username");
        this.password = config.getString("datasource.password");
    }
}
