package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.exception.EventsDeliveredFailureException;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.TokenSigningService;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.api.spi.PrivacyPolicyValidator;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultEventPublisherService implements EventPublisherService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventPublisherService.class);
    private final StreamStore streamStore;
    private final TokenSigningService signingService;
    private final EventSender eventSender;
    private final PrivacyPolicyValidator privacyValidator;

    public DefaultEventPublisherService(StreamStore streamStore, TokenSigningService signingService, EventSender eventSender, PrivacyPolicyValidator privacyValidator) {
        this.streamStore = streamStore;
        this.signingService = signingService;
        this.eventSender = eventSender;
        this.privacyValidator = privacyValidator;
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
                sendToStream(stream, eventTypeUri, fullEventPayload, subject, finalTxnId);
                count++;
            }
        }
        return count;
    }

    @Override
    public void publishToStream(String streamId, Map<String, Object> subject, String eventTypeUri, Map<String, Object> eventDetails) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new IllegalArgumentException("Stream not found"));
        String txnId = UUID.randomUUID().toString();
        java.util.Map<String, Object> fullEventPayload = new java.util.HashMap<>();
        fullEventPayload.put(eventTypeUri, eventDetails);
        if (stream.getEvents_requested() != null && stream.getEvents_requested().contains(eventTypeUri)) {
            sendToStream(stream, eventTypeUri, fullEventPayload, subject, txnId);
        }
    }

    private void sendToStream(StreamConfiguration stream, String eventTypeUri, Map<String, Object> eventPayload, Map<String, Object> subject, String txnId) {
        // Check if subject is registered OR in grace period
        // SSF Spec 9.3: Continue sending events during grace period
        boolean isRegistered = streamStore.isSubjectRegistered(stream.getStream_id(), subject);
        boolean isInGracePeriod = streamStore.isSubjectInGracePeriod(stream.getStream_id(), subject);

        if (!isRegistered && !isInGracePeriod) {
            log.debug("Subject not registered and not in grace period for stream: {}", stream.getStream_id());
            return;
        }

        if (isInGracePeriod) {
            log.info("Sending event for subject in grace period (SSF 9.3): stream={}", stream.getStream_id());
        }

        String receiverAudience = stream.getAud() != null && !stream.getAud().isEmpty()
                ? stream.getAud().getFirst()
                : null;

        if (receiverAudience == null) {
            log.error("Stream {} has no audience, cannot validate privacy", stream.getStream_id());
            return;
        }

        PrivacyPolicyValidator.PrivacyValidationResult subjectValidation =
                privacyValidator.validateSubjectIdentifier(subject, receiverAudience);
        if (!subjectValidation.isAllowed()) {
            log.warn("Privacy check failed for subject identifier: {} - Reason: {}",
                    subject, subjectValidation.getReason());
            return;
        }

        PrivacyPolicyValidator.PrivacyValidationResult eventValidation =
                privacyValidator.validateEventSharing(stream.getStream_id(), subject, eventTypeUri, eventPayload, receiverAudience);

        if (!eventValidation.isAllowed()) {
            log.warn("Privacy check failed for event sharing: stream={}, event={} - Reason: {}",
                    stream.getStream_id(), eventTypeUri, eventValidation.getReason());
            return;
        }


        if (!privacyValidator.hasConsentToShareWithReceiver(subject, receiverAudience)) {
            log.warn("No consent to share data with receiver: {}", receiverAudience);
            return;
        }

        log.debug("Privacy validation passed for stream: {}, receiver: {}", stream.getStream_id(), receiverAudience);

        String token = signingService.createSignedSet(eventPayload, subject, txnId, stream);

        String method = (stream.getDelivery() != null) ? stream.getDelivery().getMethod() : null;

        if (SharedSignalConstants.DELIVERY_METHOD_PUSH.equals(method)) {
            // PUSH
            try {
                eventSender.send(
                        stream.getStream_id(),
                        stream.getDelivery().getEndpoint_url(),
                        token,
                        stream.getDelivery().getAuthorization_header()
                );
            } catch (EventsDeliveredFailureException e) {
                //TODO: Update Stream (streamStatusService.updateStatus(streamId, "paused", reason);)
            }
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
