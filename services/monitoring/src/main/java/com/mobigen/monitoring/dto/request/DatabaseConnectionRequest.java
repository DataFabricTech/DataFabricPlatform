package com.mobigen.monitoring.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConnectionRequest {
    private String dbType;  // "postgresql", "mysql", "mariadb"
    private String host;  // ex) "localhost"
    private int port;  // ex) 5432, 3306
    private String databaseName;  // ex) "mydb"
    private String username;  // ex) "root"
    private String password;  // ex) "mypassword"
}
