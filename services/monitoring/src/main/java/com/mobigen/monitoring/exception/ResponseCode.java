package com.mobigen.monitoring.exception;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("0"),
    ERROR_UNKNOWN("UNKNOWN"),
    ERROR_BIND("Error Bind"),
    ERROR_METHOD_NOT_SUPPORTED("METHOD NOT SUPPORTED");

    private final String name;

    ResponseCode(String name) {
        this.name = name;
    }
}
