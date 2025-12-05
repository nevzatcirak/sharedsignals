package com.nevzatcirak.sharedsignals.web.scheduler;

import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spring-based scheduled job to check stream inactivity timeouts.
 * <p>
 * SSF Spec Section 8.1.1: The Transmitter MAY pause, disable, or delete
 * streams that exceed their inactivity timeout.
 * <p>
 * This scheduler runs periodically to check for inactive streams and
 * triggers the Stream Updated event when streams are paused due to inactivity.
 * <p>
 * Configuration:
 * - Enable/disable: sharedsignals.features.inactivity-timeout-check-enabled
 * - Interval: sharedsignals.scheduler.inactivity-check-interval (default: 5 minutes)
 * - Initial delay: sharedsignals.scheduler.inactivity-check-initial-delay (default: 1 minute)
 */
@Component
@ConditionalOnProperty(
        name = "sharedsignals.features.inactivity-timeout-check-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InactivityTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(InactivityTimeoutScheduler.class);

    private final InactivityTimeoutService inactivityService;

    public InactivityTimeoutScheduler(InactivityTimeoutService inactivityService) {
        this.inactivityService = inactivityService;
    }

    /**
     * Periodically checks all streams for inactivity timeout.
     * <p>
     * Default: Runs every 5 minutes (300,000 ms) with 1 minute initial delay.
     * <p>
     * For each inactive stream:
     * 1. Updates stream status to "paused"
     * 2. Sends Stream Updated event (SSF Spec 8.1.5)
     * 3. Logs the action
     */
    @Scheduled(
            fixedDelayString = "${sharedsignals.scheduler.inactivity-check-interval:300000}",
            initialDelayString = "${sharedsignals.scheduler.inactivity-check-initial-delay:60000}"
    )
    @SchedulerLock(name = "InactivityTimeoutScheduler_checkInactiveStreams", lockAtMostFor = "5m", lockAtLeastFor = "1m")
    public void checkInactiveStreams() {
        log.debug("Starting scheduled inactivity timeout check");

        try {
            inactivityService.checkAndHandleInactiveStreams();
            log.debug("Completed inactivity timeout check");
        } catch (Exception e) {
            log.error("Error during inactivity timeout check: {}", e.getMessage(), e);
        }
    }
}