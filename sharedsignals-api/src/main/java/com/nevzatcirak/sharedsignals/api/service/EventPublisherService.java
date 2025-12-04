package com.nevzatcirak.sharedsignals.api.service;

import java.util.Map;

/**
 * Service for publishing events to receivers.
 */
public interface EventPublisherService {

    int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails);

    int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails, String txnId);

    void publishToStream(String streamId, Map<String, Object> subject, Map<String, Object> eventDetails);
}
