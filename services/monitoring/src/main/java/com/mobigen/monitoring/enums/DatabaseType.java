package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum DatabaseType {
    ORACLE("Oracle"),
    MINIO("MinIO"),
    MYSQL("Mysql"),
    MARIADB("Mariadb"),
    POSTGRES("Postgresql"),
    H2("H2"),
    ;

    private final String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public static DatabaseType fromString(String dbType) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.name.equalsIgnoreCase(dbType)) {
                if (type != DatabaseType.ORACLE && type != DatabaseType.MINIO && type != DatabaseType.POSTGRES) {
                    return type;
                }
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
}
