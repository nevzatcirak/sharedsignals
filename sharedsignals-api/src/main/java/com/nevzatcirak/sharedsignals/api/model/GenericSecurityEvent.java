package com.nevzatcirak.sharedsignals.api.model;

import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Pure Domain Model representing an ingested security event.
 * Decoupled from any ingestion source (REST, Kafka, etc.).
 */
public class GenericSecurityEvent {

    private final String id;
    private final SecurityIntent intent;
    private final Map<String, Object> subject;
    private final Map<String, Object> payload;
    private final Instant occurrenceTime;
    private final String txnId;

    public GenericSecurityEvent(SecurityIntent intent, Map<String, Object> subject, Map<String, Object> payload, Instant occurrenceTime) {
        this.id = UUID.randomUUID().toString();
        this.intent = intent;
        this.subject = subject;
        this.payload = payload;
        this.occurrenceTime = occurrenceTime != null ? occurrenceTime : Instant.now();
        this.txnId = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public SecurityIntent getIntent() { return intent; }
    public Map<String, Object> getSubject() { return subject; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getOccurrenceTime() { return occurrenceTime; }
    public String getTxnId() { return txnId; }
}
