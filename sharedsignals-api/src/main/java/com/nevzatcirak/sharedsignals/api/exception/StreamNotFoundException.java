package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Thrown when a specific stream ID cannot be found in the store.
 */
public class StreamNotFoundException extends SsfException {
    public StreamNotFoundException(String streamId) {
        super(SsfErrorCode.STREAM_NOT_FOUND, "Stream not found with ID: " + streamId);
    }
}
