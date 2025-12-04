package com.nevzatcirak.sharedsignals.api.model;

import java.util.List;

/**
 * Model representing a Stream Configuration.
 * Fully compliant with SSF 8.1.1.
 */
public class StreamConfiguration {
    private String stream_id;
    private String iss;
    private List<String> aud;
    private StreamDelivery delivery;

    private List<String> events_supported;  // Transmitter-Supplied
    private List<String> events_requested;  // Receiver-Supplied
    private List<String> events_delivered;  // Transmitter-Supplied (Intersection)

    private Integer min_verification_interval;
    private String description;

    private Integer inactivity_timeout; // Transmitter-Supplied (Seconds)

    private String status;
    private String reason;

    public String getStream_id() { return stream_id; }
    public void setStream_id(String stream_id) { this.stream_id = stream_id; }
    public String getIss() { return iss; }
    public void setIss(String iss) { this.iss = iss; }
    public List<String> getAud() { return aud; }
    public void setAud(List<String> aud) { this.aud = aud; }
    public StreamDelivery getDelivery() { return delivery; }
    public void setDelivery(StreamDelivery delivery) { this.delivery = delivery; }
    public List<String> getEvents_requested() { return events_requested; }
    public void setEvents_requested(List<String> events_requested) { this.events_requested = events_requested; }
    public List<String> getEvents_supported() { return events_supported; }
    public void setEvents_supported(List<String> events_supported) { this.events_supported = events_supported; }
    public List<String> getEvents_delivered() { return events_delivered; }
    public void setEvents_delivered(List<String> events_delivered) { this.events_delivered = events_delivered; }
    public Integer getMin_verification_interval() { return min_verification_interval; }
    public void setMin_verification_interval(Integer min_verification_interval) { this.min_verification_interval = min_verification_interval; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getInactivity_timeout() { return inactivity_timeout; }
    public void setInactivity_timeout(Integer inactivity_timeout) { this.inactivity_timeout = inactivity_timeout; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
