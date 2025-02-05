package com.mobigen.datafabric.relationship.configurations.fabric;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class FabricConfiguration {
    private FabricStorage storage;
}
