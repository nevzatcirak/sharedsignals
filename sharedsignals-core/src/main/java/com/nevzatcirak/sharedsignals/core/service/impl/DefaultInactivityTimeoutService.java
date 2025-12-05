
package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import com.nevzatcirak.sharedsignals.api.service.StreamStatusService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of InactivityTimeoutService.
 * <p>
 * Tracks stream activity and handles inactivity timeout per SSF Spec Section 8.1.1.
 */
public class DefaultInactivityTimeoutService implements InactivityTimeoutService {

    private static final Logger log = LoggerFactory.getLogger(DefaultInactivityTimeoutService.class);

    private final StreamStore streamStore;
    private final StreamStatusService streamStatusService;

    /**
     * Tracks last activity time for each stream.
     * Key: streamId, Value: last activity timestamp
     */
    private final Map<String, Instant> lastActivityMap = new ConcurrentHashMap<>();

    public DefaultInactivityTimeoutService(StreamStore streamStore, StreamStatusService streamStatusService) {
        this.streamStore = streamStore;
        this.streamStatusService = streamStatusService;
    }

    /**
     * Records activity for a stream (resets inactivity timer).
     * <p>
     * SSF Spec 8.1.1: The Transmitter MUST restart the inactivity timeout
     * counter when eligible activity is observed.
     *
     * @param streamId the stream identifier
     */
    @Override
    public void recordActivity(String streamId) {
        if (streamId == null || streamId.isBlank()) {
            return;
        }

        Instant now = Instant.now();
        lastActivityMap.put(streamId, now);
        log.debug("Recorded activity for stream: {} at {}", streamId, now);
    }

    /**
     * Checks all streams for inactivity timeout and handles expired streams.
     * <p>
     * SSF Spec 8.1.1: After timeout, Transmitter MAY pause, disable, or delete.
     * If pausing/disabling, MUST send Stream Updated event BEFORE stopping.
     */
    @Override
    public void checkAndHandleInactiveStreams() {
        log.info("Starting inactivity timeout check for all streams");

        // Get all streams (implementation depends on your StreamStore)
        // For now, we'll need to add a method to get all streams
        // This is a limitation - we'll handle it below

        // Note: Since StreamStore doesn't have a findAll() method,
        // we'll need to track streams separately or add that method

        for (Map.Entry<String, Instant> entry : lastActivityMap.entrySet()) {
            String streamId = entry.getKey();
            Instant lastActivity = entry.getValue();

            try {
                checkStreamInactivity(streamId, lastActivity);
            } catch (Exception e) {
                log.error("Error checking inactivity for stream {}: {}", streamId, e.getMessage(), e);
            }
        }
    }

    /**
     * Checks a single stream for inactivity timeout.
     *
     * @param streamId     the stream identifier
     * @param lastActivity the last recorded activity time
     */
    private void checkStreamInactivity(String streamId, Instant lastActivity) {
        StreamConfiguration stream = streamStore.findById(streamId).orElse(null);

        if (stream == null) {
            log.debug("Stream {} no longer exists, removing from activity tracking", streamId);
            lastActivityMap.remove(streamId);
            return;
        }

        // Skip if stream is already paused or disabled
        if (SharedSignalConstants.STATUS_PAUSED.equals(stream.getStatus()) ||
                SharedSignalConstants.STATUS_DISABLED.equals(stream.getStatus())) {
            return;
        }

        Integer inactivityTimeout = stream.getInactivity_timeout();
        if (inactivityTimeout == null || inactivityTimeout <= 0) {
            return; // No timeout configured
        }

        long inactiveSeconds = Instant.now().getEpochSecond() - lastActivity.getEpochSecond();

        if (inactiveSeconds >= inactivityTimeout) {
            log.warn("Stream {} has exceeded inactivity timeout. Inactive for {} seconds (timeout: {} seconds)",
                    streamId, inactiveSeconds, inactivityTimeout);

            handleInactiveStream(stream, inactiveSeconds);
        }
    }

    /**
     * Handles a stream that has exceeded inactivity timeout.
     * <p>
     * SSF Spec 8.1.1: Transmitter MAY pause, disable, or delete the stream.
     * If pausing or disabling, MUST send Stream Updated event.
     *
     * @param stream          the inactive stream
     * @param inactiveSeconds how long the stream has been inactive
     */
    private void handleInactiveStream(StreamConfiguration stream, long inactiveSeconds) {
        String streamId = stream.getStream_id();

        // Strategy: Pause the stream (configurable)
        String newStatus = SharedSignalConstants.STATUS_PAUSED;
        String reason = String.format("Inactivity timeout exceeded (%d seconds)", inactiveSeconds);

        log.info("Pausing stream {} due to inactivity: {}", streamId, reason);

        // Update stream status
        stream.setStatus(newStatus);
        stream.setReason(reason);
        streamStore.save(stream);

        // SSF Spec 8.1.5: MUST send Stream Updated event BEFORE stopping stream
        streamStatusService.updateStatus(streamId, newStatus, reason);

        // Remove from activity tracking (will be re-added if receiver becomes active again)
        lastActivityMap.remove(streamId);
    }

    /**
     * Initializes activity tracking for a stream.
     * Should be called when a stream is created or re-enabled.
     *
     * @param streamId the stream identifier
     */
    public void initializeStreamActivity(String streamId) {
        recordActivity(streamId);
    }

    /**
     * Removes activity tracking for a stream.
     * Should be called when a stream is deleted.
     *
     * @param streamId the stream identifier
     */
    public void removeStreamActivity(String streamId) {
        lastActivityMap.remove(streamId);
        log.debug("Removed activity tracking for stream: {}", streamId);
    }
}