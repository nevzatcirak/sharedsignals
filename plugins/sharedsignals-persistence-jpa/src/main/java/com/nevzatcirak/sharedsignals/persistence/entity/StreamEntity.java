package com.nevzatcirak.sharedsignals.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;

@Entity
@Table(name = "ssf_streams")
public class StreamEntity {
    @Id
    @Column(name = "stream_id", nullable = false, updatable = false)
    private String streamId;
    @Column(name = "iss", nullable = false)
    private String issuer;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ssf_stream_audience", joinColumns = @JoinColumn(name = "stream_id"))
    @Column(name = "aud")
    private Set<String> audience = new HashSet<>();
    @Embedded
    private DeliveryEmbeddable delivery;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ssf_stream_events_requested", joinColumns = @JoinColumn(name = "stream_id"))
    @Column(name = "event_type_uri")
    private Set<String> eventsRequested = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "stream_events_authorized", joinColumns = @JoinColumn(name = "stream_id"))
    @Column(name = "event_type")
    private Set<String> eventsAuthorized = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ssf_stream_events_delivered", joinColumns = @JoinColumn(name = "stream_id"))
    @Column(name = "event_type_uri")
    private Set<String> eventsDelivered = new HashSet<>();
    @Column(name = "description")
    private String description;
    @Column(name = "min_verification_interval")
    private Integer minVerificationInterval;
    @Column(name = "inactivity_timeout")
    private Integer inactivityTimeout;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "status_reason")
    private String statusReason;
    @Version
    @Column(name = "opt_lock_version")
    private Long version;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "process_all_subjects", nullable = false)
    private boolean processAllSubjects = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.streamId == null) {
            this.streamId = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = SharedSignalConstants.STATUS_ENABLED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public StreamEntity() {}

    public String getStreamId() { return streamId; }
    public void setStreamId(String streamId) { this.streamId = streamId; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public Set<String> getAudience() { return audience; }
    public void setAudience(Set<String> audience) { this.audience = audience; }
    public DeliveryEmbeddable getDelivery() { return delivery; }
    public void setDelivery(DeliveryEmbeddable delivery) { this.delivery = delivery; }
    public Set<String> getEventsRequested() { return eventsRequested; }
    public void setEventsRequested(Set<String> eventsRequested) { this.eventsRequested = eventsRequested; }

    public Set<String> getEventsAuthorized() { return eventsAuthorized; }
    public void setEventsAuthorized(Set<String> eventsAuthorized) { this.eventsAuthorized = eventsAuthorized; }

    public Set<String> getEventsDelivered() { return eventsDelivered; }
    public void setEventsDelivered(Set<String> eventsDelivered) { this.eventsDelivered = eventsDelivered; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMinVerificationInterval() { return minVerificationInterval; }
    public void setMinVerificationInterval(Integer minVerificationInterval) { this.minVerificationInterval = minVerificationInterval; }
    public Integer getInactivityTimeout() { return inactivityTimeout; }
    public void setInactivityTimeout(Integer inactivityTimeout) { this.inactivityTimeout = inactivityTimeout; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isProcessAllSubjects() { return processAllSubjects; }
    public void setProcessAllSubjects(boolean processAllSubjects) { this.processAllSubjects = processAllSubjects; }
}
