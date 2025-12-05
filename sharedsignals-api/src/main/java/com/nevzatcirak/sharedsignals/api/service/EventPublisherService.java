package com.nevzatcirak.sharedsignals.api.service;

import java.util.Map;

/**
 * Service interface for publishing Security Event Tokens (SETs).
 * <p>
 * Handles event generation, signing, and delivery via configured delivery methods.
 */
public interface EventPublisherService {
    int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails);

    int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails, String txnId);

    /**
     * Publishes an event to the specified stream.
     * <p>
     * The event will be signed as a JWT SET and delivered according to
     * the stream's configured delivery method (PUSH or POLL).
     *
     * @param streamId the stream identifier
     * @param subject the subject of the event (e.g., user, device)
     * @param eventTypeUri the event type URI (e.g., session-revoked)
     * @param eventDetails the event-specific claims
     */
    void publishToStream(String streamId, Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails);
}
