package com.mobigen.monitoring.exception;

import lombok.Getter;
@Getter
public enum ResponseCode {
    SUCCESS("0"),
    ERROR_BIND("Error Bind"),
    ERROR_METHOD_NOT_SUPPORTED("METHOD NOT SUPPORTED"),
    ERROR_UNKNOWN( "UNKNOWN"),
    DFM0000( "DFM0000"),         //  "Invalid request path"
    DFM1000( "DFM1000"),         //  "K8S API error"
    DFM1001( "DFM1001"),         //  "Not found [{0}]'s node port"
    DFM2000( "DFM2000"),         //  "No response of open metadata api"
    DFM2001( "DFM2001"),         //  "Form of response is invalid json form"
    DFM3000( "DFM3000"),         //  "API [{0}] Connection failed"
    DFM4000( "DFM4000"),         //  "Failed to load query file [{0}]"
    DFM5000( "DFM5000"),         //  "Unsupported database type [{0}]"
    DFM6000( "DFM6000"),         //  "Not found serviceId [{0}]"
    DFM7000( "DFM7000"),         //  "Insufficient privileges"
    ;

    private final String name;

    ResponseCode( String name ) {
        this.name = name;
    }
}
