package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Generic exception for 500 Internal Server Error scenarios defined in SSF Spec.
 */
public class SsfInternalServerException extends SsfException {

    public SsfInternalServerException(SsfErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SsfInternalServerException(String message) {
        super(SsfErrorCode.INTERNAL_ERROR, message);
    }
}
