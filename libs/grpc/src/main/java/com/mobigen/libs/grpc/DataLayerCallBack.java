package com.mobigen.libs.grpc;


public interface DataLayerCallBack {
    QueryResponseMessage query(String query);

    SearchResponseMessage search(String input, DataModel detailSearch, Filter filterSearch, String userId);

    RecentSearchesResponseMessage recentSearch(String userId);

    HealthCheckResponseMessage healthCheck();
}
