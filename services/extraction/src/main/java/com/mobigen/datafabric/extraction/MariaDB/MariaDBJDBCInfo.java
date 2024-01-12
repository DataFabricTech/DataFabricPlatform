package com.mobigen.datafabric.extraction.MariaDB;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MariaDBJDBCInfo {
    String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    String url = "jdbc:mariadb://192.168.107.19:3306/AMS-TEST";
    String username = "root";
    String password = "ahqlwps12#$";
    String schema = "AMS-TEST";
}