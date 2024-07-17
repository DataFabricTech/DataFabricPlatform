package com.mobigen.dolphin.exception;

import lombok.Getter;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class SqlParseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public SqlParseException(ErrorCode errorCode, String msg) {
        this.errorCode = errorCode;
        this.message = msg;
    }

    public String getMessage() {
        return errorCode.getMessage() + ": " + message;
    }
}
