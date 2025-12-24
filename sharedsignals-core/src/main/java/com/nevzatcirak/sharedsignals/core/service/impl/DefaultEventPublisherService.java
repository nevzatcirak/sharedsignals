package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.EventsDeliveredFailureException;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.TokenSigningService;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.api.spi.PrivacyPolicyValidator;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.HashMap;
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

        // Verification events might not be in 'events_requested', so we don't filter them strictly here
        // or we ensure they are added to supported list.
        sendToStream(stream, eventTypeUri, fullEventPayload, subject, txnId);
    }

    private void sendToStream(StreamConfiguration stream, String eventTypeUri, Map<String, Object> eventPayload, Map<String, Object> subject, String txnId) {
        boolean isControlEvent = SharedSignalConstants.SSF_VERIFICATION.equals(eventTypeUri) ||
                                 SharedSignalConstants.SSF_STREAM_UPDATED.equals(eventTypeUri);

        if (!isControlEvent) {
            boolean isBroadcastMode = stream.isProcessAllSubjects();
            boolean isApproved = false;
            boolean isInGracePeriod = false;

            if (isBroadcastMode) {
                log.debug("Stream {} is in Broadcast Mode. Skipping explicit subject registration check.", stream.getStream_id());
                isApproved = true;
            } else {
                isApproved = streamStore.isSubjectApproved(stream.getStream_id(), subject);
                if (!isApproved) {
                    isInGracePeriod = streamStore.isSubjectInGracePeriod(stream.getStream_id(), subject);
                }
            }

            if (!isApproved && !isInGracePeriod) {
                log.debug("Event rejected. Subject not approved/registered and not in grace period. Stream: {}", stream.getStream_id());
                return;
            }

            if (isInGracePeriod) {
                log.info("Sending event for subject in grace period (SSF 9.3): stream={}", stream.getStream_id());
            }
        }

        String receiverAudience = stream.getAud() != null && !stream.getAud().isEmpty()
                ? stream.getAud().getFirst()
                : null;

        if (receiverAudience == null) {
            log.error("Stream {} has no audience, cannot validate privacy", stream.getStream_id());
            return;
        }

        // Privacy check skip for control events (optional, but usually safe as they contain no PII)
        if (!isControlEvent) {
            PrivacyPolicyValidator.PrivacyValidationResult subjectValidation =
                    privacyValidator.validateSubjectIdentifier(subject, receiverAudience);
            if (!subjectValidation.isAllowed()) {
                log.warn("Privacy check failed for subject identifier: {} - Reason: {}",
                        subject, subjectValidation.getReason());
                return;
            }

            if (!privacyValidator.hasConsentToShareWithReceiver(subject, receiverAudience)) {
                log.warn("No consent to share data with receiver: {}", receiverAudience);
                return;
            }
        }

        log.debug("Publishing event {} to stream {}", eventTypeUri, stream.getStream_id());
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
                performUpdate(stream.getStream_id(), "paused", e.getMessage());
            }
        } else if (SharedSignalConstants.DELIVERY_METHOD_POLL.equals(method)) {
            // POLL
            try {
                String jti = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
                streamStore.saveEvent(stream.getStream_id(), jti, token);
            } catch (ParseException e) {
                log.error("Error parsing generated token: " + e.getMessage());
            }
        }
    }

    private void performUpdate(String streamId, String newStatus, String reason) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));
        String oldStatus = stream.getStatus() != null ? stream.getStatus() : SharedSignalConstants.STATUS_ENABLED;

        stream.setStatus(newStatus);
        stream.setReason(reason);
        streamStore.save(stream);

        if (shouldSendStreamUpdatedEvent(oldStatus, newStatus)) {
            log.info("Status changed from {} to {} for stream {}. Sending Stream Updated event.",
                    oldStatus, newStatus, streamId);
            Map<String, Object> subject = buildStreamSubject(streamId);
            Map<String, Object> eventDetails = buildStreamUpdatedEvent(newStatus, reason);
            publishToStream(streamId, subject, SharedSignalConstants.SSF_STREAM_UPDATED, eventDetails);
        }
    }

    /**
     * Determines if a Stream Updated event should be sent.
     * <p>
     * SSF Spec 8.1.5:
     * - MUST send when changing from "enabled" to "paused" or "disabled"
     * - MUST send when changing from "paused" or "disabled" to "enabled"
     *
     * @param oldStatus the previous status
     * @param newStatus the new status
     * @return true if event should be sent
     */
    private boolean shouldSendStreamUpdatedEvent(String oldStatus, String newStatus) {
        if (oldStatus.equals(newStatus)) {
            return false; // No change
        }

        // enabled -> paused/disabled
        if (SharedSignalConstants.STATUS_ENABLED.equals(oldStatus) &&
                (SharedSignalConstants.STATUS_PAUSED.equals(newStatus) ||
                        SharedSignalConstants.STATUS_DISABLED.equals(newStatus))) {
            return true;
        }

        // paused/disabled -> enabled
        if ((SharedSignalConstants.STATUS_PAUSED.equals(oldStatus) ||
                SharedSignalConstants.STATUS_DISABLED.equals(oldStatus)) &&
                SharedSignalConstants.STATUS_ENABLED.equals(newStatus)) {
            return true;
        }

        return false;
    }

    private Map<String, Object> buildStreamUpdatedEvent(String newStatus, String reason) {
        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("status", newStatus);
        if (reason != null && !reason.isBlank()) {
            eventDetails.put("reason", reason);
        }

        return eventDetails;
    }

    /**
     * Builds the subject for stream updated event.
     * <p>
     * SSF Spec 8.1.5: The sub_id in a Stream Status Updated Event MUST always
     * be set to have a simple value of type opaque. The id MUST be the
     * stream_id of the stream being verified.
     *
     * @param streamId the stream identifier
     * @return the subject (opaque with stream_id)
     */
    private Map<String, Object> buildStreamSubject(String streamId) {
        // Build subject (stream itself as opaque subject)
        // SSF Spec 8.1.5: sub_id MUST be of format opaque with stream_id
        Map<String, Object> subject = new HashMap<>();
        subject.put("format", "opaque");
        subject.put("id", streamId);
        return subject;
    }
}
