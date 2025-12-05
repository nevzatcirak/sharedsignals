package com.nevzatcirak.sharedsignals.api.exception;

/**
 * Exception thrown when the events_delivered property in a PATCH/PUT request
 * does not match the transmitter's expected value.
 * <p>
 * Results in HTTP 500 Bad Request as per SSF Spec Section 8.1.1.3 and 8.1.1.4.
 */
public class EventsDeliveredFailureException extends SsfException {

    public EventsDeliveredFailureException(String message) {
        super(SsfErrorCode.DELIVERY_FAILED, message);
    }
}