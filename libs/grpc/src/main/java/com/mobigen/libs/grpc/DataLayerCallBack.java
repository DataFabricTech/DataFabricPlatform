package com.mobigen.libs.grpc;


public interface DataLayerCallBack {
    QueryResponse execute(String sql);

    BatchResponse executeBatch(String[] sql);

    SearchResponse search(String input, DataModel detailSearch, Filter filterSearch, String userId);

    RecentSearchesResponse recentSearch(String userId);

    HealthCheckResponse healthCheck();
}
