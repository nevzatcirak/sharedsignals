package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Thrown when the Event Receiver exceeds the allowed request rate.
 * Maps to HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends SsfException {
    public RateLimitExceededException(String message) {
        super(SsfErrorCode.TOO_MANY_REQUESTS, message);
    }
}
