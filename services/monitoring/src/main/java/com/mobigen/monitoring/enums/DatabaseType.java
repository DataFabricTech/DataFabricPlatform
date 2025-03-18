package com.mobigen.monitoring.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum DatabaseType {
    ORACLE("Oracle"),
    MINIO("MinIO"),
    MYSQL("Mysql"),
    MARIADB("Mariadb"),
    POSTGRES("Postgres"),
    H2("H2"),
    ;

    private final String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public static DatabaseType fromString(String dbType) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.name.equalsIgnoreCase(dbType)) {
                    return type;
            }
        }
        return null;
    }

    public static Boolean isDatabaseService(String serviceType) {
        if (DatabaseType.MINIO.name().equalsIgnoreCase(serviceType)) {
            return false;
        } else {
            return true;
        }
    }

    public static Boolean isRDBMS(String dbType) {
        if (!dbType.equalsIgnoreCase(MINIO.name()) && !dbType.equalsIgnoreCase("trino")) {
            return true;
        }
        return false;
    }
}
