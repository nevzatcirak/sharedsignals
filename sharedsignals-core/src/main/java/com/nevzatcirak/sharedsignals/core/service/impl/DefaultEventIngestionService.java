package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import com.nevzatcirak.sharedsignals.api.service.EventIngestionService;
import com.nevzatcirak.sharedsignals.api.service.EventPublisherService;
import com.nevzatcirak.sharedsignals.core.mapper.SecurityEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultEventIngestionService implements EventIngestionService {
    private static final Logger log = LoggerFactory.getLogger(DefaultEventIngestionService.class);
    private final EventPublisherService eventPublisherService;
    private final SecurityEventMapper eventMapper;
    private final ExecutorService ingestionExecutor = Executors.newCachedThreadPool();

    public DefaultEventIngestionService(EventPublisherService eventPublisherService, SecurityEventMapper eventMapper) {
        this.eventPublisherService = eventPublisherService;
        this.eventMapper = eventMapper;
    }

    @Override
    public CompletableFuture<Integer> ingest(GenericSecurityEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SecurityEventMapper.MappedEvent mapped = eventMapper.map(event);
                int count = eventPublisherService.publishEvent(
                        mapped.subject(),
                        mapped.typeUri(),
                        mapped.payload(),
                        event.getTxnId()
                );
                log.info("Ingested Event: Intent={}, Count={}, ID={}", event.getIntent(), count, event.getId());
                return count;
            } catch (Exception e) {
                log.error("Ingestion failed: " + event.getId(), e);
                throw e;
            }
        }, ingestionExecutor);
    }
}
