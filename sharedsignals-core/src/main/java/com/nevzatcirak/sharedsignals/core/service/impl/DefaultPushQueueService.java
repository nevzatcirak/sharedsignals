package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.PushMessage;
import com.nevzatcirak.sharedsignals.api.service.PushQueueService;
import com.nevzatcirak.sharedsignals.api.spi.PushQueueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of PushQueueService in the Core layer.
 * <p>
 * Responsible for Outbox logic (retries, status transitions)
 * and delegating storage operations to the SPI.
 */
public class DefaultPushQueueService implements PushQueueService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPushQueueService.class);
    private final PushQueueStore store;
    private static final int MAX_RETRIES = 5;

    public DefaultPushQueueService(PushQueueStore store) {
        this.store = store;
    }

    @Override
    public CompletableFuture<Void> enqueue(String streamId, String url, String token, String authHeader) {
        return store.enqueue(streamId, url, token, authHeader);
    }

    @Override
    public List<PushMessage> findReadyMessages(int batchSize) {
        return store.fetchReadyMessages(batchSize);
    }

    @Override
    public void markSuccess(Long messageId) {
        store.updateStatus(messageId, "COMPLETED", null, null, 0); // retryCount is irrelevant on success
    }

    @Override
    public void markFailure(Long messageId, String error, int currentRetryCount) {
        int newRetryCount = currentRetryCount + 1;
        String newStatus;
        Long nextRetryAt = null;

        if (newRetryCount >= MAX_RETRIES) {
            newStatus = "PERMANENTLY_FAILED";
            log.error("Message {} permanently failed after {} attempts. Error: {}", messageId, newRetryCount, error);
        } else {
            newStatus = "FAILED";
            // Exponential backoff logic in Core layer
            long backoffSeconds = (long) Math.pow(2, newRetryCount);
            nextRetryAt = Instant.now().plus(backoffSeconds, ChronoUnit.SECONDS).toEpochMilli();
        }

        store.updateStatus(messageId, newStatus, error, nextRetryAt, newRetryCount);
    }

    @Override
    public void markInProgress(Long messageId) {
        store.updateStatus(messageId, "IN_PROGRESS", null, null, 0);
    }
}