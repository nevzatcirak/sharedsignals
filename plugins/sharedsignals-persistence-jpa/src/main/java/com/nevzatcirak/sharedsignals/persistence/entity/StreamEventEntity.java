package com.nevzatcirak.sharedsignals.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity to buffer SET tokens for Poll Delivery.
 */
@Entity
@Table(name = "ssf_stream_events_buffer", indexes = {
    @Index(name = "idx_event_stream_ts", columnList = "stream_id, created_at")
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

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public String getStreamId() { return streamId; }
    public void setStreamId(String streamId) { this.streamId = streamId; }
    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }
    public String getSetToken() { return setToken; }
    public void setSetToken(String setToken) { this.setToken = setToken; }
    public Instant getCreatedAt() { return createdAt; }
}
