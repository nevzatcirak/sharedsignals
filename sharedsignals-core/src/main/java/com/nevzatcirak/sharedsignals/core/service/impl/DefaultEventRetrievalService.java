package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.EventRetrievalService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultEventRetrievalService implements EventRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEventRetrievalService.class);
    private final StreamStore streamStore;

    private static final long LONG_POLL_TIMEOUT_MS = 20_000;
    private static final long POLL_INTERVAL_MS = 2_000;

    public DefaultEventRetrievalService(StreamStore streamStore) {
        this.streamStore = streamStore;
    }

    @Override
    public PollResult pollEvents(String streamId, PollCommand command) {
        if (command.getMaxEvents() < 0) {
            throw new SsfBadRequestException("maxEvents cannot be negative.");
        }

        // Validate stream exists
        streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        // RFC 8936 Section 2.2.1: Process acknowledgments
        if (command.getAckIds() != null && !command.getAckIds().isEmpty()) {
            log.info("Acknowledging {} events for stream: {}", command.getAckIds().size(), streamId);
            streamStore.acknowledgeEvents(streamId, command.getAckIds());
        }

        // RFC 8936 Section 2.2.2: Process error reports
        if (command.getErrorIds() != null && !command.getErrorIds().isEmpty()) {
            log.warn("Receiver reported {} errors for stream: {}", command.getErrorIds().size(), streamId);
            handleSetErrors(streamId, command.getErrorIds());
        }

        Map<String, String> events = Collections.emptyMap();
        long startTime = System.currentTimeMillis();

        while (true) {
            if (command.getMaxEvents() == 0) {
                break;
            }

            events = streamStore.fetchEvents(streamId, command.getMaxEvents());

            if (!events.isEmpty() || command.isReturnImmediately() || (System.currentTimeMillis() - startTime > LONG_POLL_TIMEOUT_MS)) {
                break;
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // RFC 8936 Section 2.3: Check if more events available
        boolean moreAvailable = streamStore.hasMoreEvents(streamId);

        log.info("Poll result for stream {}: {} events returned, moreAvailable: {}",
                streamId, events.size(), moreAvailable);

        return new PollResult(events, moreAvailable);
    }

    /**
     * Handles SET error reports from receiver.
     * <p>
     * RFC 8936 Section 2.2.2: Event Receiver reports errors for specific SETs.
     *
     * @param streamId the stream identifier
     * @param errors map of jti -> error details
     */
    private void handleSetErrors(String streamId, Map<String, PollCommand.PollError> errors) {
        for (Map.Entry<String, PollCommand.PollError> entry : errors.entrySet()) {
            String jti = entry.getKey();
            PollCommand.PollError error = entry.getValue();

            log.error("SET error reported by receiver: stream={}, jti={}, err={}, description={}",
                    streamId, jti, error.getCode(), error.getDescription());

            // TODO: Implement error handling strategy
            // Options:
            // 1. Remove event from buffer (if error is unrecoverable)
            // 2. Retry later (if error is transient)
            // 3. Alert monitoring system
            // 4. Store error for analytics

            // For now: Acknowledge the event to remove it from buffer
            // (assumes errors are permanent and event should be discarded)
            streamStore.acknowledgeEvents(streamId, java.util.List.of(jti));
        }
    }
}
