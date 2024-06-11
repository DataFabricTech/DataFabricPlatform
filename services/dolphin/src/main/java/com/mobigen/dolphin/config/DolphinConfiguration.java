package com.mobigen.dolphin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import static com.mobigen.dolphin.util.Functions.convertKeywordName;

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
    private OpenMetadataConfig openMetadata;

    @Getter
    @Setter
    public static class Model {
        private String prefix;
        private String catalog;
        private ModelSchema schema;
        private String dbSchema;
        private String fileSchema;
        private Character specialChar = '"';

        public String getCatalog() {
            return convertKeywordName(catalog);
        }

        public String getDBSchema() {
            return convertKeywordName(dbSchema);
        }
        public String getFileSchema() {
            return convertKeywordName(fileSchema);
        }
    }

    @Getter
    @Setter
    public static class ModelSchema {
        private String db;
        private String file;
    }

    @Getter
    @Setter
    public static class OpenMetadataConfig {
        private String fernetKey;
        private String apiUrl;
        private String botToken;
    }
}
