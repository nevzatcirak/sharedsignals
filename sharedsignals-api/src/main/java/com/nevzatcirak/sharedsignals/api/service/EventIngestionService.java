package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import java.util.concurrent.CompletableFuture;

public interface EventIngestionService {
    /**
     * Ingests a validated, standardized security event.
     */
    CompletableFuture<Integer> ingest(GenericSecurityEvent event);
}
