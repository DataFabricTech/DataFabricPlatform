package com.mobigen.vdap.server.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final Object causedObject;

    public CustomException(String message, Object causedObject) {
        super(message);
        this.causedObject = causedObject;
    }
    public CustomException(String message, Throwable e, Object causedObject) {
        super(message, e);
        this.causedObject = causedObject;
    }
}
