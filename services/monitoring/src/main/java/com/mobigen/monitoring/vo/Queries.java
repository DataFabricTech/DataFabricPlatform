package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.service.query.QueryLoader;
import lombok.Getter;

@Getter
public enum Queries {
    RDB_GET_DATABASES("sql/mysql/get-database-from-rdb.sql"),
    ORACLE_GET_DATABASES("sql/oracle/get-database-from-oracle.sql"),
    GET_TABLES("sql/get-tables.sql"),
    GET_TABLES_FROM_ORACLE("sql/oracle/get-all-tables-from-oracle.sql"),
    POSTGRES_GET_DATABASES("sql/postgres/get-database-from-postgres.sql"),
    GET_ALL_TABLE_ROWS("sql/get-all-table-rows.sql"),
    GET_ALL_TABLE_ROWS_FROM_ORACLE("sql/oracle/get-all-table-rows-from-oracle.sql"),
    GET_SCHEMA("sql/get-schema.sql");

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return QueryLoader.loadQuery(query);
    }
}