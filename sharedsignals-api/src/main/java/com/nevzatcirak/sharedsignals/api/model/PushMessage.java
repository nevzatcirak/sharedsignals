package com.nevzatcirak.sharedsignals.api.model;

import java.time.Instant;

/**
 * Domain Model representing a Push Delivery item.
 * <p>
 * This POJO decouples the Web/Core layers from the JPA Entity.
 */
public class PushMessage {
    private Long id;
    private String streamId;
    private String endpointUrl;
    private String authHeader;
    private String signedToken;
    private String status;
    private int retryCount;
    private Instant nextRetryAt;
    private String lastError;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}