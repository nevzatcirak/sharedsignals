package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Thrown when a security violation occurs (e.g., unauthorized access).
 */
public class SsfSecurityException extends SsfException {
    public SsfSecurityException(String message) {
        super(SsfErrorCode.UNAUTHORIZED_ACCESS, message);
    }
}
