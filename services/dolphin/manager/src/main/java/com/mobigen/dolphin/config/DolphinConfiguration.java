package com.mobigen.dolphin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "dolphin")
@Configuration
public class DolphinConfiguration {
    private Model model;

    @Getter
    @Setter
    public static class Model {
        private String prefix;
        private String catalog;
        private String schema;
    }
}
