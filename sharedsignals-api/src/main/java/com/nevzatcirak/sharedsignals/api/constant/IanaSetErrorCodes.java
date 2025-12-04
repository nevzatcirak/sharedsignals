package com.nevzatcirak.sharedsignals.api.constant;

/**
 * Standard Security Event Token (SET) Error Codes.
 * Registered in IANA as per RFC 8935 Section 7.1.
 */
public final class IanaSetErrorCodes {

    private IanaSetErrorCodes() {}

    /**
     * The request body cannot be parsed as a SET, or the Event Payload within the SET
     * does not conform to the event's definition.
     */
    public static final String INVALID_REQUEST = "invalid_request";

    /**
     * One or more keys used to encrypt or sign the SET is invalid or otherwise unacceptable
     * to the SET Recipient (expired, revoked, failed certificate validation, etc.).
     */
    public static final String INVALID_KEY = "invalid_key";

    /**
     * The SET Issuer is invalid for the SET Recipient.
     */
    public static final String INVALID_ISSUER = "invalid_issuer";

    /**
     * The SET Audience does not correspond to the SET Recipient.
     */
    public static final String INVALID_AUDIENCE = "invalid_audience";

    /**
     * The SET Recipient could not authenticate the SET Transmitter.
     */
    public static final String AUTHENTICATION_FAILED = "authentication_failed";

    /**
     * The SET Transmitter is not authorized to transmit the SET to the SET Recipient.
     */
    public static final String ACCESS_DENIED = "access_denied";
}
