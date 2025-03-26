package com.mobigen.monitoring.enums;

import com.mobigen.monitoring.service.query.QueryLoader;
import lombok.Getter;

@Getter
public enum Queries {
    RDB_GET_DATABASES("sql/mysql/get-database-from-rdb.sql"),
    ORACLE_GET_DATABASES("sql/oracle/get-database-from-oracle.sql"),
    POSTGRES_GET_DATABASES("sql/postgres/get-database-from-postgres.sql"),

    GET_TABLES("sql/get-tables.sql"),
    GET_TABLES_FROM_ORACLE("sql/oracle/get-all-tables-from-oracle.sql"),

    GET_ALL_TABLE_ROWS("sql/get-all-table-rows.sql"),
    GET_ALL_TABLE_ROWS_FROM_ORACLE("sql/oracle/get-all-table-rows-from-oracle.sql"),

    GET_SCHEMA("sql/get-schema.sql"),

    GET_MYSQL_CPU_SPENT_TIME("sql/mysql/get-cpu-spent-time.sql"),
    GET_ORACLE_CPU_SPENT_TIME("sql/oracle/get-cpu-spent-time.sql"),
    GET_POSTGRES_CPU_SPENT_TIME("sql/postgres/get-cpu-spent-time.sql"),

    GET_MYSQL_DISK_USAGE("sql/mysql/get-disk-usage.sql"),
    GET_ORACLE_DISK_USAGE("sql/oracle/get-disk-usage.sql"),
    GET_POSTGRES_DISK_USAGE("sql/postgres/get-disk-usage.sql"),

    GET_MYSQL_FAILED_REQUEST_QUERY("sql/mysql/get-failed-request-query.sql"),
    GET_ORACLE_FAILED_REQUEST_QUERY("sql/oracle/get-failed-request-query.sql"),
    GET_POSTGRES_FAILED_REQUEST_QUERY("sql/postgres/get-failed-request-query.sql"),

    GET_MYSQL_SUCCESS_REQUEST_QUERY("sql/mysql/get-success-request-query.sql"),
    GET_ORACLE_SUCCESS_REQUEST_QUERY("sql/oracle/get-success-request-query.sql"),
    GET_POSTGRES_SUCCESS_REQUEST_QUERY("sql/postgres/get-success-request-query.sql"),

    GET_MYSQL_MEMORY_USAGE("sql/mysql/get-memory-usage.sql"),
    GET_ORACLE_MEMORY_USAGE("sql/oracle/get-memory-usage.sql"),
    GET_POSTGRES_MEMORY_USAGE("sql/postgres/get-memory-usage.sql"),

    GET_MYSQL_SLOW_QUERY("sql/mysql/get-slow-query.sql"),
    GET_ORACLE_SLOW_QUERY("sql/oracle/get-slow-query.sql"),
    GET_POSTGRES_SLOW_QUERY("sql/postgres/get-slow-query.sql");

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return QueryLoader.loadQuery(query);
    }
}