package com.mindex.challenge.exception;

public class CompensationValidationException extends RuntimeException {

    public CompensationValidationException(String message) {
        super(message);
    }

    public CompensationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}