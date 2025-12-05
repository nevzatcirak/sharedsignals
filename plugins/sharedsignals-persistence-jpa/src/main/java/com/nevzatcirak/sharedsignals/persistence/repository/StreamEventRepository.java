package com.nevzatcirak.sharedsignals.persistence.repository;

import com.nevzatcirak.sharedsignals.persistence.entity.StreamEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for event buffer management (POLL delivery - RFC 8936).
 */
@Repository
public interface StreamEventRepository extends JpaRepository<StreamEventEntity, Long> {

    /**
     * Finds unacknowledged events for a stream, ordered by creation time (FIFO).
     * <p>
     * RFC 8936: Transmitter returns events in the order they were generated.
     *
     * @param streamId the stream identifier
     * @param pageable pagination (use to limit maxEvents)
     * @return list of unacknowledged events
     */
    @Query("SELECT e FROM StreamEventEntity e " +
            "WHERE e.streamId = :streamId " +
            "AND e.acknowledged = false " +
            "ORDER BY e.createdAt ASC")
    List<StreamEventEntity> findUnacknowledgedEvents(String streamId, Pageable pageable);

    /**
     * Counts unacknowledged events for a stream.
     * <p>
     * Used to determine if "moreAvailable" flag should be set in poll response.
     *
     * @param streamId the stream identifier
     * @return count of unacknowledged events
     */
    long countByStreamIdAndAcknowledgedFalse(String streamId);

    /**
     * Marks events as acknowledged.
     * <p>
     * RFC 8936: Acknowledged events are removed from the transmitter's queue.
     *
     * @param jtis list of JWT IDs to acknowledge
     * @param acknowledgedAt timestamp of acknowledgment
     * @return number of events acknowledged
     */
    @Modifying
    @Query("UPDATE StreamEventEntity e " +
            "SET e.acknowledged = true, e.acknowledgedAt = :acknowledgedAt " +
            "WHERE e.jti IN :jtis")
    int acknowledgeEvents(List<String> jtis, Instant acknowledgedAt);

    /**
     * Deletes old acknowledged events (cleanup).
     *
     * @param before delete events acknowledged before this time
     */
    void deleteByAcknowledgedTrueAndAcknowledgedAtBefore(Instant before);

    /**
     * Finds events by JTI list (for error reporting).
     *
     * @param jtis list of JWT IDs
     * @return list of matching events
     */
    List<StreamEventEntity> findByJtiIn(List<String> jtis);
}