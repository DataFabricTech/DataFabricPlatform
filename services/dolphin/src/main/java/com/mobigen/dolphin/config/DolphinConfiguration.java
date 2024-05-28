package com.mobigen.dolphin.config;

import com.mobigen.dolphin.antlr.ModelSqlParser;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.VocabularyImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

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
        private String schema;
        private Character specialChar = '"';

        public String getCatalog() {
            return convertKeywordName(catalog);
        }

        public String getSchema() {
            return convertKeywordName(schema);
        }

        public String convertKeywordName(String name) {
            if (name.startsWith("`")) {
                name = specialChar + name.substring(1, name.length() - 1) + specialChar;
            } else if (Arrays.asList(((VocabularyImpl) ModelSqlParser.VOCABULARY).getSymbolicNames())
                    .contains("K_" + name.toUpperCase())) {
                name = specialChar + name + specialChar;
            }
            return name;
        }
    }

    @Getter
    @Setter
    public static class OpenMetadataConfig {
        private String fernetKey;
    }
}
