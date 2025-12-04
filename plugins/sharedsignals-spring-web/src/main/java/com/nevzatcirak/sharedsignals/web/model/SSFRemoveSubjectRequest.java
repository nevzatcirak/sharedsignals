package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Web layer request model for removing a subject from a stream.
 * <p>
 * SSF Spec Section 8.1.3.3: Removing a Subject
 * <p>
 * Example:
 * <pre>
 * {
 *   "stream_id": "f67e39a0a4d34d56b3aa1bc4cff0069f",
 *   "subject": {
 *     "format": "phone",
 *     "phone_number": "+12065550123"
 *   }
 * }
 * </pre>
 */
public class SSFRemoveSubjectRequest {

    @JsonProperty("stream_id")
    private String streamId;

    @JsonProperty("subject")
    private Map<String, Object> subject;

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
}