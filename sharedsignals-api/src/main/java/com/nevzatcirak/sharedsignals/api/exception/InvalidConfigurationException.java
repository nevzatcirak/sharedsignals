package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Thrown when the provided stream configuration parameters are invalid.
 */
public class InvalidConfigurationException extends SsfException {
    public InvalidConfigurationException(String message) {
        super(SsfErrorCode.INVALID_STREAM_CONFIGURATION, message);
    }
}
