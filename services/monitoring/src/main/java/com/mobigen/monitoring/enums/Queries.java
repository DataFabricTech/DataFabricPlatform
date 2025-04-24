package com.mobigen.monitoring.enums;

import com.mobigen.monitoring.service.query.QueryLoader;
import lombok.Getter;

@Getter
public enum Queries {
    RDB_GET_DATABASES("sql/mariadb/get-database-from-rdb.sql"),
    ORACLE_GET_DATABASES("sql/oracle/get-database-from-oracle.sql"),
    POSTGRES_GET_DATABASES("sql/postgres/get-database-from-postgres.sql"),

    GET_TABLES("sql/get-tables.sql"),
    GET_TABLES_FROM_ORACLE("sql/oracle/get-all-tables-from-oracle.sql"),

    GET_ALL_TABLE_ROWS("sql/get-all-table-rows.sql"),
    GET_ALL_TABLE_ROWS_FROM_ORACLE("sql/oracle/get-all-table-rows-from-oracle.sql"),

    GET_SCHEMA("sql/get-schema.sql"),

    GET_MYSQL_CPU_SPENT_TIME("sql/mysql/get-cpu-spent-time.sql"),
    GET_MARIADB_CPU_SPENT_TIME("sql/mariadb/get-cpu-spent-time.sql"),
    GET_ORACLE_CPU_SPENT_TIME("sql/oracle/get-cpu-spent-time.sql"),
    GET_POSTGRES_CPU_SPENT_TIME("sql/postgres/get-cpu-spent-time.sql"),

    GET_MYSQL_DISK_USAGE("sql/mariadb/get-disk-usage.sql"),
    GET_ORACLE_DISK_USAGE("sql/oracle/get-disk-usage.sql"),
    GET_POSTGRES_DISK_USAGE("sql/postgres/get-disk-usage.sql"),

    GET_MYSQL_REQUEST_RATE_QUERY("sql/mysql/get-request-rate-query.sql"),
    GET_MARIADB_REQUEST_RATE_QUERY("sql/mariadb/get-request-rate-query.sql"),
    GET_ORACLE_REQUEST_RATE_QUERY("sql/oracle/get-request-rate-query.sql"),
    GET_POSTGRES_REQUEST_RATE_QUERY("sql/postgres/get-request-rate-query.sql"),

    GET_MYSQL_MEMORY_USAGE("sql/mysql/get-memory-usage.sql"),
    GET_MARIADB_MEMORY_USAGE("sql/mariadb/get-memory-usage.sql"),
    GET_ORACLE_MEMORY_USAGE("sql/oracle/get-memory-usage.sql"),
    GET_POSTGRES_MEMORY_USAGE("sql/postgres/get-memory-usage.sql"),

    GET_MYSQL_SLOW_QUERY("sql/mariadb/get-slow-query.sql"),
    GET_ORACLE_SLOW_QUERY("sql/oracle/get-slow-query.sql"),
    GET_POSTGRES_SLOW_QUERY("sql/postgres/get-slow-query.sql"),

    GET_VIEW("sql/get-views.sql"),
    get_ORACLE_VIEW("sql/oracle/get-view-schema-from-oracle.sql"),
    ;

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return QueryLoader.loadQuery(query);
    }
}