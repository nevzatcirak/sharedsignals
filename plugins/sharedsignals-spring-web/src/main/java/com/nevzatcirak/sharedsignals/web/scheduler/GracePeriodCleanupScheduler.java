package com.nevzatcirak.sharedsignals.web.scheduler;

import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduled job to cleanup expired grace period subjects.
 * <p>
 * SSF Spec Section 9.3: After grace period expires, no need to track removed subjects.
 * <p>
 * Runs daily to cleanup expired entries.
 */
@Component
@ConditionalOnProperty(
        name = "sharedsignals.security.grace-period-cleanup-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class GracePeriodCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(GracePeriodCleanupScheduler.class);

    private final StreamStore streamStore;

    public GracePeriodCleanupScheduler(StreamStore streamStore) {
        this.streamStore = streamStore;
    }

    /**
     * Cleans up expired grace period subjects.
     * <p>
     * Default: Runs daily (86400000 ms) with 1 hour initial delay.
     */
    @Scheduled(
            fixedDelayString = "${sharedsignals.scheduler.grace-period-cleanup-interval:86400000}",
            initialDelayString = "${sharedsignals.scheduler.grace-period-cleanup-initial-delay:3600000}"
    )
    public void cleanupExpiredGracePeriods() {
        log.debug("Starting grace period cleanup");

        try {
            streamStore.deleteByGracePeriodExpiresAtBefore(Instant.now());
        } catch (Exception e) {
            log.error("Error during grace period cleanup: {}", e.getMessage(), e);
        }
    }
}