package com.mobigen.monitoring.enums;

public enum DatabaseType {
    ORACLE("Oracle"),
    MINIO("Minio"),
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
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown database type: " + dbType);
    }
}
