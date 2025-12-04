package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Thrown when attempting to add a subject that is already monitored by the stream.
 */
public class SubjectAlreadyExistsException extends SsfException {
    public SubjectAlreadyExistsException(String message) {
        super(SsfErrorCode.SUBJECT_ALREADY_REGISTERED, message);
    }
}
