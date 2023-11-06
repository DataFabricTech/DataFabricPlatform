package com.mobigen.datafabric.dataLayer.model;


import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("200"),
    FAIL("400"),
    UNKNOWN("404")
    ;

    private final String value;
    ResponseCode(String value) {
        this.value = value;
    }
}
