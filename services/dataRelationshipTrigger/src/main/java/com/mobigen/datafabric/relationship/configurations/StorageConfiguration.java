package com.mobigen.datafabric.relationship.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class StorageConfiguration {
    private String host;
    private int port;
    private String bucket;
    private String region;
    private String username;
    private String password;
    private String prefix;
}
