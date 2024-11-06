package com.mobigen.datafabric.relationship.configurations.dolphin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class DolphinStorage {
    private StorageType schema;
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
}
