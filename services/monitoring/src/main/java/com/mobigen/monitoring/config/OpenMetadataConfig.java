package com.mobigen.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * open metadata server 에 요청할 url 모음
 * */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "open-metadata")
public class OpenMetadataConfig {
    private Path path = new Path();
    private String origin;

    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {
        private String id;
        private String passwd;
    }

    @Getter
    @Setter
    public static class Path {
        // service 관련
        private String databaseService = "/api/v1/services/databaseServices";
        private String storageService = "/api/v1/services/storageServices";

        // model 관련
        private String databaseModel = "/api/v1/tables?database=";
        private String storageModel = "/api/v1/containers?root=true&service=";

        // table 관련
        // table 정보
        private String tables = "/api/v1/tables?fields=tableConstraints,columns,schemaDefinition,dataModel,lifeCycle&limit=1000000&database=";
        private String tableProfile = "/api/v1/tables/%s/tableProfile/latest"; // table fqn

        // ingestion
        private String ingestionPipeline = "/api/v1/services/ingestionPipelines";
        private String query = "/api/v1/search/query";

        // login 및 권한(token)
        private String login = "/api/v1/users/login";
        private String bot = "/api/v1/bots/name/ingestion-bot";
        private String authMechanism = "/api/v1/users/auth-mechanism";
    }
}
