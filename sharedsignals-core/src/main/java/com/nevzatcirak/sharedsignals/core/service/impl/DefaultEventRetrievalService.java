package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
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

        if (streamStore.findById(streamId).isEmpty()) {
            throw new StreamNotFoundException(streamId);
        }

        if (!command.getAckIds().isEmpty()) {
            streamStore.acknowledgeEvents(streamId, command.getAckIds());
            log.debug("Stream [{}]: Acknowledged {} events", streamId, command.getAckIds().size());
        }

        if (command.getErrorIds() != null && !command.getErrorIds().isEmpty()) {
            command.getErrorIds().forEach((jti, error) -> {
                log.error("Receiver reported error for Stream [{}], SET [{}]: {} - {}",
                    streamId, jti, error.getCode(), error.getDescription());
            });
            List<String> errorJtis = new ArrayList<>(command.getErrorIds().keySet());
            streamStore.acknowledgeEvents(streamId, errorJtis);
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

        boolean more = false;
        if (!events.isEmpty()) {
            if (events.size() >= command.getMaxEvents()) {
                more = streamStore.hasMoreEvents(streamId);
            }
        }

        return new PollResult(events, more);
    }
}
