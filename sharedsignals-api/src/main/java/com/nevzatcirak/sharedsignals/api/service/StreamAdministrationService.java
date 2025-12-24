package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import java.util.Set;

/**
 * Service definition for Administrative operations on Streams and Subjects.
 * These operations are typically performed by the Transmitter operator, not the Receiver.
 */
public interface StreamAdministrationService {

    /**
     * Updates the approval status of a subject within a stream.
     * Used to mitigate Information Harvesting (SSF Section 9.2).
     *
     * @param streamId The ID of the stream.
     * @param subjectHash The computed hash of the subject (unique identifier within stream).
     * @param status The new status (APPROVED, REJECTED, PENDING).
     * @param owner The administrator performing the action (for audit).
     */
    void updateSubjectStatus(String streamId, String subjectHash, SubjectStatus status, String owner);

    /**
     * Sets the list of event types that are authorized to be sent over this stream.
     * Acts as an allow-list filter.
     *
     * @param streamId The ID of the stream.
     * @param authorizedEvents The set of allowed event type URIs.
     * @param owner The administrator performing the action.
     */
    void updateAuthorizedEvents(String streamId, Set<String> authorizedEvents, String owner);

    void setStreamBroadcastMode(String streamId, boolean enabled, String owner);
}
