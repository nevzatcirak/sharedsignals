package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.RateLimitExceededException;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.model.TriggerVerificationCommand;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.VerificationService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of VerificationService.
 * <p>
 * Implements SSF Draft Spec Section 8.1.4 (Verification).
 */
public class DefaultVerificationService implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultVerificationService.class);

    private final EventPublisherService eventPublisher;
    private final StreamStore streamStore;

    // Rate limiting: Track last verification time per stream
    private final Map<String, Instant> lastVerificationTime = new ConcurrentHashMap<>();

    public DefaultVerificationService(EventPublisherService eventPublisher, StreamStore streamStore) {
        this.eventPublisher = eventPublisher;
        this.streamStore = streamStore;
    }

    /**
     * Triggers a verification event for the specified stream.
     * <p>
     * SSF Spec 8.1.4.2: The Transmitter responds with 204 No Content
     * to indicate the event has been queued (not necessarily delivered).
     *
     * @param command the verification trigger command
     * @param owner the authenticated client ID (from JWT)
     */
    @Override
    public void triggerVerification(TriggerVerificationCommand command, String owner) {
        String streamId = command.getStreamId();

        StreamConfiguration stream = validateOwnership(streamId, owner);

        checkRateLimit(stream);

        Map<String, Object> verificationEvent = buildVerificationEvent(command);

        Map<String, Object> subject = buildStreamSubject(streamId);

        log.info("Triggering verification event for stream: {}", streamId);
        eventPublisher.publishToStream(streamId, subject, SharedSignalConstants.SSF_VERIFICATION, verificationEvent);
        lastVerificationTime.put(streamId, Instant.now());
    }

    /**
     * Validates that the authenticated client owns the stream.
     *
     * @param streamId the stream identifier
     * @param owner the authenticated client ID
     * @return the stream configuration
     * @throws StreamNotFoundException if stream doesn't exist
     * @throws SsfSecurityException if owner is not in aud
     */
    private StreamConfiguration validateOwnership(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }

        return stream;
    }

    /**
     * Checks if verification request exceeds rate limit.
     * <p>
     * SSF Spec 8.1.4.2: Transmitters MAY respond with 429 if requests
     * exceed min_verification_interval.
     *
     * @param stream the stream configuration
     * @throws RateLimitExceededException if rate limit exceeded
     */
    private void checkRateLimit(StreamConfiguration stream) {
        Integer minInterval = stream.getMin_verification_interval();
        if (minInterval == null || minInterval <= 0) {
            return; // No rate limiting
        }

        Instant lastTime = lastVerificationTime.get(stream.getStream_id());
        if (lastTime == null) {
            return;
        }

        long secondsSinceLastVerification = Instant.now().getEpochSecond() - lastTime.getEpochSecond();
        if (secondsSinceLastVerification < minInterval) {
            log.warn("Verification rate limit exceeded for stream: {}. Min interval: {}s, Time since last: {}s",
                    stream.getStream_id(), minInterval, secondsSinceLastVerification);
            throw new RateLimitExceededException(
                    "Verification requests must be at least " + minInterval + " seconds apart. " +
                    "Time since last request: " + secondsSinceLastVerification + " seconds."
            );
        }
    }

    /**
     * Builds the verification event payload.
     * <p>
     * SSF Spec 8.1.4.1: Verification Event contains optional "state" attribute.
     *
     * @param command the verification command
     * @return the event payload
     */
    private Map<String, Object> buildVerificationEvent(TriggerVerificationCommand command) {
        Map<String, Object> event = new HashMap<>();

        // SSF Spec 8.1.4.1: state is OPTIONAL
        if (command.getState() != null && !command.getState().isBlank()) {
            event.put("state", command.getState());
        }

        return event;
    }

    /**
     * Builds the subject for verification event.
     * <p>
     * SSF Spec 8.1.4.1: The sub_id in a Verification Event MUST always
     * be set to have a simple value of type opaque. The id MUST be the
     * stream_id of the stream being verified.
     *
     * @param streamId the stream identifier
     * @return the subject (opaque with stream_id)
     */
    private Map<String, Object> buildStreamSubject(String streamId) {
        Map<String, Object> subject = new HashMap<>();
        subject.put("format", "opaque");
        subject.put("id", streamId);
        return subject;
    }
}