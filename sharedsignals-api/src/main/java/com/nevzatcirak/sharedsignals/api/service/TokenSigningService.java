package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import java.util.Map;

/**
 * Service for signing Security Event Tokens (SET).
 */
public interface TokenSigningService {
    /**
     * Creates a signed JWT (SET) for a specific stream.
     * * @param eventPayload The 'events' claim content (Map of event types to details).
     * @param subId        The 'sub_id' claim content (The Subject Identifier).
     * @param txnId        The transaction identifier (txn).
     * @param stream       The target stream configuration.
     * @return The serialized signed JWT string.
     */
    String createSignedSet(Map<String, Object> eventPayload, Map<String, Object> subId, String txnId, StreamConfiguration stream);
}
