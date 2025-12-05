package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.PushMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing the Outbox Queue.
 * <p>
 * Used by the Web layer to process the queue.
 * Implemented by the Core layer.
 */
public interface PushQueueService {

    /**
     * Enqueues a message (typically called via EventSender, but exposed here if needed).
     */
    CompletableFuture<Void> enqueue(String streamId, String url, String token, String authHeader);

    /**
     * Finds messages ready for delivery.
     *
     * @param batchSize number of messages
     * @return list of messages
     */
    List<PushMessage> findReadyMessages(int batchSize);

    /**
     * Marks a message as successful.
     *
     * @param messageId the message ID
     */
    void markSuccess(Long messageId);

    /**
     * Marks a message as failed and schedules retry.
     *
     * @param messageId  the message ID
     * @param error      the error message
     * @param retryCount current retry count
     */
    void markFailure(Long messageId, String error, int retryCount);

    /**
     * Marks a message as in-progress.
     *
     * @param messageId the message ID
     */
    void markInProgress(Long messageId);
}