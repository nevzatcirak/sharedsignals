package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Web layer request model for triggering a verification event.
 * <p>
 * SSF Spec Section 8.1.4.2: Triggering a Verification Event
 * <p>
 * Example:
 * <pre>
 * {
 *   "stream_id": "f67e39a0a4d34d56b3aa1bc4cff0069f",
 *   "state": "VGhpcyBpcyBhbiBleGFtcGxlIHN0YXRlIHZhbHVlLgo="
 * }
 * </pre>
 */
public class SSFVerificationRequest {

    @JsonProperty("stream_id")
    private String streamId;

    /**
     * SSF Spec 8.1.4.2: OPTIONAL. An arbitrary string that the Event Transmitter
     * MUST echo back to the Event Receiver in the Verification Event's payload.
     */
    @JsonProperty("state")
    private String state;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}