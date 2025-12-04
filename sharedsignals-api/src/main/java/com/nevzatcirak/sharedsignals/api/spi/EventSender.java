package com.nevzatcirak.sharedsignals.api.spi;

import java.util.concurrent.CompletableFuture;

/**
 * SPI Port for sending raw SET tokens to receivers via HTTP.
 */
public interface EventSender {
    /**
     * Sends the signed SET to the specified URL.
     *
     * @param streamId   The ID of the stream (used for error handling/pausing).
     * @param url        The receiver's endpoint.
     * @param token      The signed JWT string.
     * @param authHeader Authorization header value (optional).
     * @return A future indicating success or failure.
     */
    CompletableFuture<Void> send(String streamId, String url, String token, String authHeader);
}
