package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.TokenSigningService;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultEventPublisherService implements EventPublisherService {

    private final StreamStore streamStore;
    private final TokenSigningService signingService;
    private final EventSender eventSender;

    public DefaultEventPublisherService(StreamStore streamStore, TokenSigningService signingService, EventSender eventSender) {
        this.streamStore = streamStore;
        this.signingService = signingService;
        this.eventSender = eventSender;
    }

    @Override
    public int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails) {
        return publishEvent(subject, eventTypeUri, eventDetails, UUID.randomUUID().toString());
    }

    @Override
    public int publishEvent(Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails, String txnId) {
        List<StreamConfiguration> streams = streamStore.findStreamsBySubject(subject);
        int count = 0;
        String finalTxnId = (txnId == null) ? UUID.randomUUID().toString() : txnId;

        java.util.Map<String, Object> fullEventPayload = new java.util.HashMap<>();
        fullEventPayload.put(eventTypeUri, eventDetails);

        for (StreamConfiguration stream : streams) {
            if (stream.getEvents_requested() != null && stream.getEvents_requested().contains(eventTypeUri)) {
                sendToStream(stream, fullEventPayload, subject, finalTxnId);
                count++;
            }
        }
        return count;
    }

    @Override
    public void publishToStream(String streamId, Map<String, Object> subject, Map<String, Object> fullEventPayload) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("Stream not found"));
        sendToStream(stream, fullEventPayload, subject, UUID.randomUUID().toString());
    }

    private void sendToStream(StreamConfiguration stream, Map<String, Object> eventPayload, Map<String, Object> subject, String txnId) {
        String token = signingService.createSignedSet(eventPayload, subject, txnId, stream);

        String method = (stream.getDelivery() != null) ? stream.getDelivery().getMethod() : null;

        if (SharedSignalConstants.DELIVERY_METHOD_PUSH.equals(method)) {
            // PUSH
            eventSender.send(
                    stream.getStream_id(),
                    stream.getDelivery().getEndpoint_url(),
                    token,
                    stream.getDelivery().getAuthorization_header()
            );
        } else if (SharedSignalConstants.DELIVERY_METHOD_POLL.equals(method)) {
            // POLL
            try {
                String jti = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
                streamStore.saveEvent(stream.getStream_id(), jti, token);
            } catch (ParseException e) {
                System.err.println("Error parsing generated token: " + e.getMessage());
            }
        }
    }
}
