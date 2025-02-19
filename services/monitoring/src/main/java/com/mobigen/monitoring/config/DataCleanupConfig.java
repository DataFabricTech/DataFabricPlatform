package com.mobigen.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data-cleanup-condition")
public class DataCleanupConfig {
    private int retentionDays = 90;
    private int maximumRows = 30;
}
