package com.nevzatcirak.sharedsignals.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for tracking removed subjects with grace period.
 * <p>
 * SSF Spec Section 9.3: Event Transmitters MAY continue sending events
 * related to a subject for some time after removal to protect against
 * malicious subject removal attacks.
 */
@Entity
@Table(name = "ssf_removed_subjects",
        indexes = @Index(name = "idx_removed_subject", columnList = "stream_id, subject_hash"))
public class RemovedSubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stream_id", nullable = false)
    private String streamId;

    @Column(name = "subject_hash", nullable = false)
    private String subjectHash;

    @Column(name = "subject_payload", nullable = false, columnDefinition = "TEXT")
    private String subjectPayload;

    /**
     * When the subject was removed from the stream.
     */
    @Column(name = "removed_at", nullable = false)
    private Instant removedAt;

    /**
     * When the grace period expires and events should stop being sent.
     * <p>
     * SSF Spec 9.3: Transmitters MAY continue sending events for some time.
     * Default grace period: 7 days (configurable).
     */
    @Column(name = "grace_period_expires_at", nullable = false)
    private Instant gracePeriodExpiresAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getSubjectHash() {
        return subjectHash;
    }

    public void setSubjectHash(String subjectHash) {
        this.subjectHash = subjectHash;
    }

    public String getSubjectPayload() {
        return subjectPayload;
    }

    public void setSubjectPayload(String subjectPayload) {
        this.subjectPayload = subjectPayload;
    }

    public Instant getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(Instant removedAt) {
        this.removedAt = removedAt;
    }

    public Instant getGracePeriodExpiresAt() {
        return gracePeriodExpiresAt;
    }

    public void setGracePeriodExpiresAt(Instant gracePeriodExpiresAt) {
        this.gracePeriodExpiresAt = gracePeriodExpiresAt;
    }
}