package com.nevzatcirak.sharedsignals.api.model;
import java.util.Map;

/**
 * Request model for triggering verification.
 */
public class VerificationRequest {
    private String stream_id;
    private String state;
    private Map<String, Object> subject;

    public String getStream_id() { return stream_id; }
    public void setStream_id(String stream_id) { this.stream_id = stream_id; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Map<String, Object> getSubject() { return subject; }
    public void setSubject(Map<String, Object> subject) { this.subject = subject; }
}
