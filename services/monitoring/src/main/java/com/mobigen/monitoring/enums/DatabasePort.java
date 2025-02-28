package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum DatabasePort {
    MARIADB(3306),
    MYSQL(3306),
    ORACLE(1521),
    POSTGRES(5432);

    private final Integer port;

    DatabasePort(Integer port) {
        this.port = port;
    }

    public static int getPortFromHost(final String host) {
        for (DatabasePort port : DatabasePort.values()) {
            if (port.name().equalsIgnoreCase(host)) {
                return port.getPort();
            }
        }
        return 0;
    }
}
