package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Base abstract class for all Shared Signals Framework exceptions.
 */
public abstract class SsfException extends RuntimeException {
    private final SsfErrorCode errorCode;

    protected SsfException(SsfErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SsfErrorCode getErrorCode() { return errorCode; }
}
