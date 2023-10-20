package com.mobigen.datafabric.dataLayer.service;

import com.mobigen.libs.grpc.*;

import java.util.Arrays;

public class DataLayerServiceImpl implements DataLayerCallBack {
    private final OpenSearchService openSearchService;
    private final RDBMSService rdbmsService;

    public DataLayerServiceImpl(OpenSearchService openSearchService, RDBMSService rdbmsService) {
        this.openSearchService = openSearchService;
        this.rdbmsService = rdbmsService;
    }

    @Override
    public QueryResponseMessage query(String query) {
        return rdbmsService.query(query);
    }

    @Override
    public SearchResponseMessage search(String input, DataModel detailSearch, Filter filterSearch, String userId) {
        var dataModelIds = openSearchService.search(input, detailSearch, filterSearch, userId);
        // TODO RDBMS search
        return null;
    }

    @Override
    public RecentSearchesResponseMessage recentSearch(String userId) {
        var searches = openSearchService.getRecentSearch(userId);
        if (searches != null) {
            return RecentSearchesResponseMessage.newBuilder()
                    .addAllSearched(Arrays.asList(searches)).build();
        } else {
            return RecentSearchesResponseMessage.newBuilder().build();
        }
    }

    @Override
    public HealthCheckResponseMessage healthCheck() {
        return HealthCheckResponseMessage.newBuilder().setStatus(true).build();
    }
}
