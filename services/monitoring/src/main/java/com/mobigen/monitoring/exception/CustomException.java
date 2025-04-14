package com.mobigen.monitoring.exception;

import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
public class CustomException extends RuntimeException {
    private final ResponseCode errorCode;
    private String msg;
    private String[] errorVars;

    public CustomException(@NotNull ResponseCode code, String errorMessage, String... vars ) {
        super( errorMessage );
        this.errorCode = code;
        this.errorVars = vars;
    }

    public CustomException( Exception e, String errMsg ) {
        super( e );
        this.msg = errMsg;
        this.errorCode = ResponseCode.ERROR_UNKNOWN;
        this.errorVars = null;
    }

    public CustomException( String errMsg ) {
        super( errMsg );
        this.msg = errMsg;
        this.errorCode = ResponseCode.ERROR_UNKNOWN;
        this.errorVars = null;
    }
}
