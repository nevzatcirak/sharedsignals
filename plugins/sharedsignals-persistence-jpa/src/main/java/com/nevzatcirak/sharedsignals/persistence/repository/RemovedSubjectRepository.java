package com.nevzatcirak.sharedsignals.persistence.repository;

import com.nevzatcirak.sharedsignals.persistence.entity.RemovedSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for removed subjects with grace period tracking.
 * <p>
 * SSF Spec Section 9.3: Malicious Subject Removal protection.
 */
@Repository
public interface RemovedSubjectRepository extends JpaRepository<RemovedSubjectEntity, Long> {

    /**
     * Checks if a subject is in the grace period (still eligible for events).
     *
     * @param streamId the stream identifier
     * @param subjectHash the subject hash
     * @param now the current time
     * @return the removed subject if in grace period, empty otherwise
     */
    @Query("SELECT r FROM RemovedSubjectEntity r " +
            "WHERE r.streamId = :streamId " +
            "AND r.subjectHash = :subjectHash " +
            "AND r.gracePeriodExpiresAt > :now")
    Optional<RemovedSubjectEntity> findActiveGracePeriod(
            @Param("streamId") String streamId,
            @Param("subjectHash") String subjectHash,
            @Param("now") Instant now
    );

    /**
     * Finds all removed subjects whose grace period has expired.
     *
     * @param now the current time
     * @return list of expired removed subjects
     */
    @Query("SELECT r FROM RemovedSubjectEntity r WHERE r.gracePeriodExpiresAt <= :now")
    List<RemovedSubjectEntity> findExpiredGracePeriods(@Param("now") Instant now);

    /**
     * Deletes removed subjects whose grace period has expired.
     *
     * @param expiryTime the cutoff time
     */
    void deleteByGracePeriodExpiresAtBefore(Instant expiryTime);
}