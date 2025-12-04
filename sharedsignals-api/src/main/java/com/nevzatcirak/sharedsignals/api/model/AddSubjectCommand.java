package com.nevzatcirak.sharedsignals.api.model;

import java.util.Map;

/**
 * Command object for adding a subject to a stream.
 * <p>
 * Framework-agnostic POJO for transporting add subject data
 * from Web layer to Core layer.
 * <p>
 * SSF Spec Section 8.1.3.2: Adding a Subject to a Stream
 */
public class AddSubjectCommand {

    private final String streamId;
    private final Map<String, Object> subject;
    private final boolean verified;

    public AddSubjectCommand(String streamId, Map<String, Object> subject, boolean verified) {
        this.streamId = streamId;
        this.subject = subject;
        this.verified = verified;
    }

    public String getStreamId() {
        return streamId;
    }

    public Map<String, Object> getSubject() {
        return subject;
    }

    public boolean isVerified() {
        return verified;
    }
}