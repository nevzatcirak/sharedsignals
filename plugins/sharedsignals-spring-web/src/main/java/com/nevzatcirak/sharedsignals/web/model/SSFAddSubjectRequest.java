package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Web layer request model for adding a subject to a stream.
 * <p>
 * SSF Spec Section 8.1.3.2: Adding a Subject to a Stream
 * <p>
 * Example:
 * <pre>
 * {
 *   "stream_id": "f67e39a0a4d34d56b3aa1bc4cff0069f",
 *   "subject": {
 *     "format": "email",
 *     "email": "user@example.com"
 *   },
 *   "verified": true
 * }
 * </pre>
 */
public class SSFAddSubjectRequest {

    @JsonProperty("stream_id")
    private String streamId;

    @JsonProperty("subject")
    private Map<String, Object> subject;

    /**
     * SSF Spec 8.1.3.2: OPTIONAL. A boolean value indicating whether the Event Receiver
     * has verified the Subject claim. If omitted, Transmitters SHOULD assume true.
     */
    @JsonProperty("verified")
    private Boolean verified;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public Map<String, Object> getSubject() {
        return subject;
    }

    public void setSubject(Map<String, Object> subject) {
        this.subject = subject;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}