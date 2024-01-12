package com.mobigen.datafabric.extraction.PostgreSQL;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PostgreSQLJDBCInfo {
    String JDBC_DRIVER = "org.postgresql.Driver";
    String url = "jdbc:postgresql://192.168.107.19:5433/postgres";
    String username = "postgres";
    String password = "ahqlwps12#$";

    List<String> tableNameList = Arrays.asList("address", "last_modify_test");
}