package com.mobigen.datafabric.relationship.configurations.dolphin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class DolphinConfiguration {
    private DolphinStorage storage;
}
