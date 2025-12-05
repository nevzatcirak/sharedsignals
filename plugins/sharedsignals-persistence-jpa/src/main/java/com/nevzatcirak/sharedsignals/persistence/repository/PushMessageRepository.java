package com.nevzatcirak.sharedsignals.persistence.repository;

import com.nevzatcirak.sharedsignals.persistence.entity.PushMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PushMessageRepository extends JpaRepository<PushMessageEntity, Long> {

    /**
     * Finds messages ready to be processed:
     * 1. Status is PENDING
     * 2. OR Status is FAILED (Retryable) AND retry time has passed
     */
    @Query("SELECT p FROM PushMessageEntity p " +
           "WHERE p.status = 'PENDING' " +
           "OR (p.status = 'FAILED' AND p.nextRetryAt <= :now)")
    List<PushMessageEntity> findReadyToProcess(@Param("now") Instant now, Pageable pageable);
}