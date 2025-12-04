package com.nevzatcirak.sharedsignals.api.model;

import java.util.Map;

/**
 * Command object for removing a subject from a stream.
 * <p>
 * Framework-agnostic POJO for transporting remove subject data
 * from Web layer to Core layer.
 * <p>
 * SSF Spec Section 8.1.3.3: Removing a Subject
 */
public class RemoveSubjectCommand {

    private final String streamId;
    private final Map<String, Object> subject;

    public RemoveSubjectCommand(String streamId, Map<String, Object> subject) {
        this.streamId = streamId;
        this.subject = subject;
    }

    public String getStreamId() {
        return streamId;
    }

    public Map<String, Object> getSubject() {
        return subject;
    }
}