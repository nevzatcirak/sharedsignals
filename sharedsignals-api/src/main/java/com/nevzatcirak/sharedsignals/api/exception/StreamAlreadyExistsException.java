package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Exception thrown when attempting to create a stream that already exists
 * and the transmitter does not support multiple streams per receiver.
 * <p>
 * Results in HTTP 409 Conflict as per SSF Spec Section 8.1.1.1.
 */
public class StreamAlreadyExistsException extends SsfException {

    public StreamAlreadyExistsException(String audience) {
        super(SsfErrorCode.STREAM_ALREADY_EXISTS,
              "A stream already exists for audience: " + audience + ". Multiple streams per receiver are not supported.");
    }
}