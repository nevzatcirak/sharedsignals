package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Exception thrown when the events_delivered property in a PATCH/PUT request
 * does not match the transmitter's expected value.
 * <p>
 * Results in HTTP 400 Bad Request as per SSF Spec Section 8.1.1.3 and 8.1.1.4.
 */
public class EventsDeliveredMismatchException extends SsfException {

    public EventsDeliveredMismatchException() {
        super(SsfErrorCode.INVALID_STREAM_CONFIGURATION,
                "The events_delivered property does not match the transmitter's expected value.");
    }
}