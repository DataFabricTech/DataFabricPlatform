package com.mobigen.datafabric.dataLayer.config;

import lombok.Getter;
import org.apache.commons.configuration.Configuration;

@Getter
public class OpenSearchConfig {
    private final Configuration config;
    private final String dataModelIndex;
    private final String storageIndex;
    private final String recentSearchesIndex;
    private final String host;
    private final int port;

    public OpenSearchConfig(Configuration config) {
        this.config = config;
        this.dataModelIndex = config.getString("open_search.index.data_model").toLowerCase();
        this.storageIndex = config.getString("open_search.index.storage").toLowerCase();
        this.recentSearchesIndex = config.getString("open_search.index.recent_searches").toLowerCase();
        this.host = config.getString("open_search.host");
        this.port = config.getInt("open_search.port");
    }
}
