package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Enumeration of standardized error codes for the Shared Signals Framework.
 * <p>
 * These codes define specific error scenarios as per the project requirements.
 */
public enum SsfErrorCode {
    // 404 - Not Found
    STREAM_NOT_FOUND("SSF-1001", "The requested stream configuration could not be found."),
    SUBJECT_NOT_FOUND("SSF-2002", "The subject could not be found in this stream."),

    // 409 - Conflict
    STREAM_ALREADY_EXISTS("SSF-1002", "A stream with the provided criteria already exists."),
    SUBJECT_ALREADY_REGISTERED("SSF-2001", "The subject is already registered to this stream."),

    // 400 - Bad Request
    INVALID_STREAM_CONFIGURATION("SSF-1003", "The provided stream configuration is invalid."),
    INVALID_SUBJECT_FORMAT("SSF-2003", "The subject format is not supported or is malformed."),
    UNSUPPORTED_DELIVERY_METHOD("SSF-1004", "The requested delivery method is not supported by this transmitter."),
    MALFORMED_REQUEST("SSF-1005", "The HTTP request body is malformed or invalid JSON."),

    // 429 - Rate Limiting
    TOO_MANY_REQUESTS("SSF-4029", "The client has sent too many requests in a given amount of time."),

    // 500 & Security
    SIGNATURE_GENERATION_FAILED("SSF-3001", "Failed to generate security event token signature."),
    DELIVERY_FAILED("SSF-3002", "Failed to deliver event to the receiver endpoint."),
    UNAUTHORIZED_ACCESS("SSF-3003", "Caller is not authorized to perform this operation."),
    INTERNAL_ERROR("SSF-5000", "An unexpected internal error occurred.");

    private final String code;
    private final String description;

    SsfErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
