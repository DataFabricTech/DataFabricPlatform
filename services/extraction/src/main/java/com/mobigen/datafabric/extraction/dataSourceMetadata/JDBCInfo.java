package com.mobigen.datafabric.extraction.dataSourceMetadata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JDBCInfo {
    private String JDBC_DRIVER = "org.postgresql.Driver";
    private String url = "jdbc:postgresql://192.168.107.19:5433/postgres";
    private String username = "postgres";
    private String password = "ahqlwps12#$";
}
