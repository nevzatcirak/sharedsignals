package com.nevzatcirak.sharedsignals.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a Push Delivery item in the Outbox Queue.
 * Stores the signed SET token and delivery status.
 */
@Entity
@Table(name = "ssf_push_queue", indexes = {
        @Index(name = "idx_push_status_next_retry", columnList = "status, next_retry_at"),
        @Index(name = "idx_push_created", columnList = "created_at")
})
public class PushMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stream_id", nullable = false)
    private String streamId;

    @Column(name = "endpoint_url", nullable = false)
    private String endpointUrl;

    @Column(name = "auth_header", length = 2048)
    private String authHeader;

    @Column(name = "signed_token", nullable = false, columnDefinition = "TEXT")
    private String signedToken;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum DeliveryStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        PERMANENTLY_FAILED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStreamId() { return streamId; }
    public void setStreamId(String streamId) { this.streamId = streamId; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getAuthHeader() { return authHeader; }
    public void setAuthHeader(String authHeader) { this.authHeader = authHeader; }
    public String getSignedToken() { return signedToken; }
    public void setSignedToken(String signedToken) { this.signedToken = signedToken; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}