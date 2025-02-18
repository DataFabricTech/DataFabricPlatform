package com.mobigen.vdap.server.response;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("0"),
    ERROR_BIND("Error Bind"),
    ERROR_METHOD_NOT_SUPPORTED("METHOD NOT SUPPORTED"),
    ERROR_UNKNOWN("UNKNOWN"),
    ;

    private final String name;

    ResponseCode(String name) {
        this.name = name;
    }
}
