package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.StreamStatus;
import com.nevzatcirak.sharedsignals.api.model.TriggerVerificationCommand;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.StreamStatusService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultStreamStatusService implements StreamStatusService {
    private static final Logger log = LoggerFactory.getLogger(DefaultStreamStatusService.class);

    private final StreamStore streamStore;
    private final EventPublisherService eventPublisher;

    public DefaultStreamStatusService(StreamStore streamStore, EventPublisherService eventPublisher) {
        this.streamStore = streamStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public StreamStatus getStatus(String streamId, String owner) {
        validateOwner(streamId, owner);
        return fetchStatus(streamId);
    }

    @Override
    public StreamStatus updateStatus(String streamId, String status, String reason, String owner) {
        validateOwner(streamId, owner);
        return performUpdate(streamId, status, reason);
    }

    @Override
    public void updateStatus(String streamId, String status, String reason) {
        performUpdate(streamId, status, reason);
    }

    private StreamStatus performUpdate(String streamId, String newStatus, String reason) {
       StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));
        String oldStatus = stream.getStatus() != null ? stream.getStatus() : SharedSignalConstants.STATUS_ENABLED;

        stream.setStatus(newStatus);
        stream.setReason(reason);
        streamStore.save(stream);

        // SSF Spec 8.1.5: Send Stream Updated event if status changed
        if (shouldSendStreamUpdatedEvent(oldStatus, newStatus)) {
            log.info("Status changed from {} to {} for stream {}. Sending Stream Updated event.",
                    oldStatus, newStatus, streamId);
            Map<String, Object> subject = buildStreamSubject(streamId);
            Map<String, Object> eventDetails = buildStreamUpdatedEvent(newStatus, reason);
            eventPublisher.publishToStream(streamId, subject, SharedSignalConstants.SSF_STREAM_UPDATED, eventDetails);
        }

        StreamStatus status = new StreamStatus();
        status.setStream_id(streamId);
        status.setStatus(newStatus);
        status.setReason(reason);

        return status;
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

    private StreamStatus fetchStatus(String streamId) {
        StreamConfiguration config = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        StreamStatus status = new StreamStatus();
        status.setStream_id(config.getStream_id());
        status.setStatus(config.getStatus());
        status.setReason(config.getReason());
        return status;
    }

    private void validateOwner(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }
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
