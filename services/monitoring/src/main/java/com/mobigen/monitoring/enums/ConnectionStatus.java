package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum ConnectionStatus {
    DISCONNECTED("disconnected"),
    CONNECTED("connected"),
    NOT_TARGET("pass"),
    CONNECT_ERROR("connect error"),
    ;

    private final String name;

    ConnectionStatus(String name) {
        this.name = name;
    }
}
