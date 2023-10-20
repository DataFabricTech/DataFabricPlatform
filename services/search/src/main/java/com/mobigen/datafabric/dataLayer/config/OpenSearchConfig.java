package com.mobigen.datafabric.dataLayer.config;

import lombok.Getter;
import org.apache.commons.configuration.Configuration;

@Getter
public class OpenSearchConfig {
    private final Configuration config;
    private final String dataModelIndex;
    private final String recentSearchesIndex;

    public OpenSearchConfig(Configuration config) {
        this.config = config;
        this.dataModelIndex = config.getString("open_search.index.data_model");
        this.recentSearchesIndex = config.getString("open_search.index.recent_searches");
    }
}
