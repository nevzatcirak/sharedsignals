package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Generic exception for 400 Bad Request scenarios defined in SSF Spec.
 * Used when input validation fails or supported metadata checks fail.
 */
public class SsfBadRequestException extends SsfException {

    public SsfBadRequestException(SsfErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SsfBadRequestException(String message) {
        super(SsfErrorCode.INVALID_STREAM_CONFIGURATION, message);
    }
}
