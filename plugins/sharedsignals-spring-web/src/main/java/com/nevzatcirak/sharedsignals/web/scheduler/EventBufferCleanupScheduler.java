package com.nevzatcirak.sharedsignals.web.scheduler;

import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled job to cleanup old acknowledged events from the buffer.
 * <p>
 * RFC 8936: After acknowledgment, events can be removed from the transmitter's queue.
 */
@Component
@ConditionalOnProperty(
    name = "sharedsignals.features.event-buffer-cleanup-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class EventBufferCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventBufferCleanupScheduler.class);
    private final StreamStore streamStore;
    private final int retentionDays;

    public EventBufferCleanupScheduler(
            StreamStore streamStore,
            @Value("${sharedsignals.poll.acknowledged-event-retention-days:7}") int retentionDays) {
        this.streamStore = streamStore;
        this.retentionDays = retentionDays;
    }

    /**
     * Cleans up old acknowledged events.
     * <p>
     * Default: Runs daily at 2 AM
     */
    @Scheduled(cron = "${sharedsignals.scheduler.event-cleanup-cron:0 0 2 * * ?}")
    @SchedulerLock(name = "EventBufferCleanupScheduler_cleanup", lockAtMostFor = "1h", lockAtLeastFor = "5m")
    public void cleanupAcknowledgedEvents() {
        log.info("Starting event buffer cleanup (retention: {} days)", retentionDays);
        try {
            Instant cutoffTime = Instant.now().minusSeconds(retentionDays * 86400L);

            long countBefore = streamStore.getEventCount();
            streamStore.deleteByAcknowledgedTrueAndAcknowledgedAtBefore(cutoffTime);
            long countAfter = streamStore.getEventCount();

            long deleted = countBefore - countAfter;
            if (deleted > 0) {
                log.info("Event buffer cleanup completed: {} acknowledged events deleted", deleted);
            } else {
                log.debug("Event buffer cleanup completed: no events to delete");
            }
        } catch (Exception e) {
            log.error("Error during event buffer cleanup: {}", e.getMessage(), e);
        }
    }
}