package com.nevzatcirak.sharedsignals.core.builder;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

/**
 * Factory for creating standard CAEP and RISC event payloads.
 * <p>
 * Compliance Note: Subject information is transmitted via the top-level 'sub_id' claim
 * (SSF 4.1.6). It is NOT duplicated inside the event details.
 * </p>
 */
public class SecurityEventBuilder {

    /**
     * Creates a Verification event payload.
     * @param state The state string to be echoed back.
     * @return The event details map.
     */
    public static Map<String, Object> createVerification(String state) {
        Map<String, Object> details = new HashMap<>();
        if (state != null) {
            details.put("state", state);
        }
        details.put("event_timestamp", Instant.now().toEpochMilli());

        Map<String, Object> eventsClaim = new HashMap<>();
        eventsClaim.put(SharedSignalConstants.RISC_VERIFICATION, details);
        return eventsClaim;
    }

    /**
     * Creates a CAEP Session Revoked event payload.
     * Note: Does not include 'subject' inside details, as it's in 'sub_id'.
     * @return The event details map.
     */
    public static Map<String, Object> createSessionRevoked() {
        Map<String, Object> details = new HashMap<>();
        details.put("event_timestamp", Instant.now().toEpochMilli());

        Map<String, Object> eventsClaim = new HashMap<>();
        eventsClaim.put(SharedSignalConstants.CAEP_SESSION_REVOKED, details);
        return eventsClaim;
    }

    /**
     * Creates a generic event structure.
     * @param typeUri The URI of the event type.
     * @param extraClaims Additional event-specific claims (e.g. reason, initiating_entity).
     * @return The event details map.
     */
    public static Map<String, Object> createGenericEvent(String typeUri, Map<String, Object> extraClaims) {
        Map<String, Object> details = new HashMap<>();
        details.put("event_timestamp", Instant.now().toEpochMilli());

        if (extraClaims != null) {
            details.putAll(extraClaims);
        }

        Map<String, Object> eventsClaim = new HashMap<>();
        eventsClaim.put(typeUri, details);
        return eventsClaim;
    }

    /**
     * Utility to add additional fields (extensions) to an existing event payload.
     */
    public static void addExtensionField(Map<String, Object> eventsPayload, String eventTypeUri, String key, Object value) {
        if (eventsPayload.containsKey(eventTypeUri)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) eventsPayload.get(eventTypeUri);
            details.put(key, value);
        }
    }
}
