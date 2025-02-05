package com.mobigen.datafabric.relationship.models.exception;

public class DataRelationshipException extends RuntimeException {
    public DataRelationshipException(String message) {
        super(message);
    }

    public DataRelationshipException(String message, Throwable cause) {
        super(message, cause);
    }
}
