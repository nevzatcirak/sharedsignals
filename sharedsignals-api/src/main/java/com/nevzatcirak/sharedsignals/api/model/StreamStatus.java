package com.nevzatcirak.sharedsignals.api.model;

/**
 * Model representing the current status of a stream.
 */
public class StreamStatus {
    private String stream_id;
    private String status;
    private String reason;
    
    public String getStream_id() { return stream_id; }
    public void setStream_id(String stream_id) { this.stream_id = stream_id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
