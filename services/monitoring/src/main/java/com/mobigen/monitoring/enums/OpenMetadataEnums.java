package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum OpenMetadataEnums {
    ID("id"),
    SERVICE_TYPE("serviceType"),
    DATA("data"),
    NAME("name"), // service name
    UPDATED_AT("updatedAt"),
    UPDATED_BY("updatedBy"),
    PAGING("paging"),
    PASSWORD("password"),

    FQN("fullyQualifiedName"),
    ACCESS_TOKEN("accessToken"),
    TOKEN_TYPE("tokenType"),
    BOT_USER("botUser"),
    CONNECTION("connection"),
    JWT_TOKEN("JWTToken"),

    // pipeline
    RUN_ID("runId"),
    PIPELINE_TYPE("pipelineType"),
    PIPELINE_STATE("pipelineState"),
    SERVICE("service"),
    DISPLAY_NAME("displayName"),
    END_DATE("endDate"),
    STATUS_CHANGE("status_change"),

    SERVICE_ID("service_id"),
    DESCRIPTION("description"),
    CONFIG("config"),
    DELETED("deleted"),
    FULLY_QUALIFIED_NAME("fullyQualifiedName")
    ;

    private final String name;

    OpenMetadataEnums(String name) {
        this.name = name;
    }

}
