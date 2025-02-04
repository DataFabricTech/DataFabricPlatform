package com.mobigen.datafabric.relationship.configurations;

import com.mobigen.datafabric.relationship.configurations.dolphin.DolphinConfiguration;
import com.mobigen.datafabric.relationship.configurations.fabric.FabricConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "configuration")
public class Configurations {
    private String defaultPeriod;
    private DolphinConfiguration dolphin;
    private FabricConfiguration fabric;
    private StorageConfiguration storage;
    private TemporarySpace temporarySpace;
    private DataRelationshipServer dataRelationship;
}
