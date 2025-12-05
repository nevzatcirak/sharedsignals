package com.nevzatcirak.sharedsignals.api.spi;

import com.nevzatcirak.sharedsignals.api.model.PushMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SPI interface for persisting and retrieving Outbox messages.
 * <p>
 * This interface is implemented by the Persistence layer (Adapter)
 * and used by the Core layer (Service).
 */
public interface PushQueueStore {

    /**
     * Persists a message to the storage.
     *
     * @param streamId   the stream identifier
     * @param url        the destination URL
     * @param token      the signed SET token
     * @param authHeader optional authorization header
     * @return a future completing when persisted
     */
    CompletableFuture<Void> enqueue(String streamId, String url, String token, String authHeader);

    /**
     * Retrieves messages ready for processing from storage.
     *
     * @param batchSize number of messages to fetch
     * @return list of domain models
     */
    List<PushMessage> fetchReadyMessages(int batchSize);

    /**
     * Updates the status of a message.
     *
     * @param messageId   the message ID
     * @param status      the new status (e.g., COMPLETED, FAILED)
     * @param lastError   optional error message
     * @param nextRetryAt optional next retry timestamp (epoch millis)
     * @param retryCount  updated retry count
     */
    void updateStatus(Long messageId, String status, String lastError, Long nextRetryAt, int retryCount);
}