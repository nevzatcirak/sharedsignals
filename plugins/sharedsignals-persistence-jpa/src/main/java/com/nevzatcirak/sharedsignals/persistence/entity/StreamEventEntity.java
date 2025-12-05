package com.nevzatcirak.sharedsignals.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity to buffer SET tokens for Poll Delivery (RFC 8936).
 */
@Entity
@Table(name = "ssf_stream_events_buffer", indexes = {
    @Index(name = "idx_event_stream_ts", columnList = "stream_id, created_at"),
    @Index(name = "idx_jti_unique", columnList = "jti", unique = true),
    @Index(name = "idx_stream_unack", columnList = "stream_id, acknowledged")
})
public class StreamEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stream_id", nullable = false)
    private String streamId;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "set_token", nullable = false, columnDefinition = "TEXT")
    private String setToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * RFC 8936: Whether the receiver has acknowledged this event.
     * Acknowledged events can be removed from the buffer.
     */
    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;

    /**
     * When the event was acknowledged (if acknowledged).
     */
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

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

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getSetToken() {
        return setToken;
    }

    public void setSetToken(String setToken) {
        this.setToken = setToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}