package com.mobigen.datafabric.relationship.configurations.dolphin;

import lombok.Getter;

@Getter
public enum StorageType {
    MYSQL("mysql"),
    POSTGRESQL("postgresql");

    private final String value;

    StorageType(String value) {
        this.value = value;
    }
}
