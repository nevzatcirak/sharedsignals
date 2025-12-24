package com.nevzatcirak.sharedsignals.api.spi;

import com.nevzatcirak.sharedsignals.api.enums.SubjectStatus;
import com.nevzatcirak.sharedsignals.api.model.RemoveSubjectCommand;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * SPI Port for persisting stream data and buffering poll events.
 * <p>
 * This is the core abstraction for stream persistence. Implementations
 * are provided in persistence adapter modules (e.g., JPA, MongoDB, etc.).
 */
public interface StreamStore {

    // ========== Stream Management ==========

    StreamConfiguration save(StreamConfiguration stream);

    Optional<StreamConfiguration> findById(String streamId);

    void deleteById(String streamId);

    List<StreamConfiguration> findByAudience(String audience);

    List<StreamConfiguration> findStreamsBySubject(Map<String, Object> subject);

    //  ========== Admin Operations ==========
    void updateAuthorizedEvents(String streamId, Set<String> authorizedEvents);

    void updateSubjectStatus(String streamId, String subjectHash, SubjectStatus status);

    void updateStreamMode(String streamId, boolean processAllSubjects);

    // ========== Subject Management ==========

    /**
     * Adds a subject to a stream.
     * <p>
     * SSF Spec Section 8.1.3.2: Adding a Subject to a Stream
     *
     * @param streamId the stream identifier
     * @param subject the subject to add (format: Map with "format", "email", etc.)
     * @param verified whether the receiver has verified this subject (SSF Spec: defaults to true if omitted)
     */
    void addSubject(String streamId, Map<String, Object> subject, boolean verified);

    /**
     * Removes a subject from a stream.
     * <p>
     * SSF Spec Section 8.1.3.3: Removing a Subject
     *
     * @param streamId the stream identifier
     * @param subject the subject to remove
     */
    void removeSubject(String streamId, Map<String, Object> subject);

    /**
     * Checks if a subject is registered for a stream.
     *
     * @param streamId the stream identifier
     * @param subject the subject to check
     * @return true if the subject is registered, false otherwise
     */
    boolean isSubjectRegistered(String streamId, Map<String, Object> subject);

    boolean isSubjectApproved(String streamId, Map<String, Object> subject);

    /**
     * Checks if a subject is in grace period after removal.
     * <p>
     * SSF Spec Section 9.3: Transmitters MAY continue sending events
     * for some time after subject removal.
     *
     * @param streamId the stream identifier
     * @param subject the subject to check
     * @return true if subject is in grace period, false otherwise
     */
    boolean isSubjectInGracePeriod(String streamId, Map<String, Object> subject);

    void deleteByGracePeriodExpiresAtBefore(Instant expiryTime);

    // ========== Event Buffering (Poll) ==========

    /**
     * Saves a SET (Security Event Token) to the stream's event buffer.
     *
     * @param streamId the stream identifier
     * @param jti the JWT ID (unique identifier for the event)
     * @param setToken the serialized SET token (JWT string)
     */
    void saveEvent(String streamId, String jti, String setToken);

    /**
     * Fetches events from the stream's buffer.
     *
     * @param streamId the stream identifier
     * @param maxEvents maximum number of events to fetch
     * @return map of jti -> SET token
     */
    Map<String, String> fetchEvents(String streamId, int maxEvents);

    /**
     * Acknowledges (removes) events from the buffer.
     *
     * @param streamId the stream identifier
     * @param jtis list of JWT IDs to acknowledge
     */
    void acknowledgeEvents(String streamId, List<String> jtis);

    /**
     * Checks if there are more events available in the buffer.
     *
     * @param streamId the stream identifier
     * @return true if more events are available, false otherwise
     */
    boolean hasMoreEvents(String streamId);

    /**
     * Deletes old acknowledged events (cleanup).
     *
     * @param before delete events acknowledged before this time
     */
    void deleteByAcknowledgedTrueAndAcknowledgedAtBefore(Instant before);

    long countUnacknowledgedEvents(String streamId);

    long getEventCount();
}
