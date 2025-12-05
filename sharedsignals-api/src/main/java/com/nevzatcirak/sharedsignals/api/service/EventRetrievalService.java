package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle Poll Delivery requests.
 * Updated to support Non-Blocking Async Polling.
 */
public interface EventRetrievalService {
    /**
     * Polls for events asynchronously.
     * Implementations should NOT block the calling thread.
     */
    CompletableFuture<PollResult> pollEventsAsync(String streamId, PollCommand command);
}