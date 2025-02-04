package com.mobigen.datafabric.relationship.configurations.fabric;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class FabricStorage {
    private StorageType schema;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
}
