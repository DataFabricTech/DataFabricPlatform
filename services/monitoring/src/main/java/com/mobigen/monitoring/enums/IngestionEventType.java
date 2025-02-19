package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum IngestionEventType {
    DELETED("deleted"),
    CREATED("created"),
    UPDATED("updated"),

    CONNECTION_CHECK("connectionCheck"),

    UNKNOWN("unKnown")
    ;

    private final String name;
    IngestionEventType(String name) {this.name = name;}
}
