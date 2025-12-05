package com.nevzatcirak.sharedsignals.persistence.repository;

import com.nevzatcirak.sharedsignals.persistence.entity.StreamEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Import added
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
     */
    @Query("SELECT e FROM StreamEventEntity e " +
            "WHERE e.streamId = :streamId " +
            "AND e.acknowledged = false " +
            "ORDER BY e.createdAt ASC")
    List<StreamEventEntity> findUnacknowledgedEvents(@Param("streamId") String streamId, Pageable pageable);

    long countByStreamIdAndAcknowledgedFalse(String streamId);

    /**
     * Marks events as acknowledged.
     */
    @Modifying
    @Query("UPDATE StreamEventEntity e " +
            "SET e.acknowledged = true, e.acknowledgedAt = :acknowledgedAt " +
            "WHERE e.jti IN :jtis")
    int acknowledgeEvents(@Param("jtis") List<String> jtis, @Param("acknowledgedAt") Instant acknowledgedAt);

    void deleteByAcknowledgedTrueAndAcknowledgedAtBefore(Instant before);

    List<StreamEventEntity> findByJtiIn(List<String> jtis);
}
