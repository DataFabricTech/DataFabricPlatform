package com.mobigen.datafabric.extraction.MySQL;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySQLJDBCInfo {
    String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    String url = "jdbc:mysql://192.168.107.19:3307/test";
    String username = "root";
    String password = "ahqlwps12#$";
    String schema = "test";
}