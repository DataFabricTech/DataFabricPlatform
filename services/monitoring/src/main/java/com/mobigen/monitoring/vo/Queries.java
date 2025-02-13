package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.service.query.QueryLoader;
import lombok.Getter;

@Getter
public enum Queries {
    RDB_GET_DATABASES("sql/get-database-from-rdb.sql"),
    GET_TABLES("sql/get-tables.sql"),
    POSTGRES_GET_DATABASES("sql/get-database-from-postgres.sql"),
    GET_ALL_TABLE_ROWS("sql/get-all-table-rows.sql");

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return QueryLoader.loadQuery(query);
    }
}