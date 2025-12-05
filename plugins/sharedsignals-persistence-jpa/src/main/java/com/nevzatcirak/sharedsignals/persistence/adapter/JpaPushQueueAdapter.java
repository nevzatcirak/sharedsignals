package com.nevzatcirak.sharedsignals.persistence.adapter;

import com.nevzatcirak.sharedsignals.api.model.PushMessage;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.api.spi.PushQueueStore;
import com.nevzatcirak.sharedsignals.persistence.entity.PushMessageEntity;
import com.nevzatcirak.sharedsignals.persistence.repository.PushMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Adapter implementation that bridges API SPIs to JPA Repository.
 * Implements PushQueueStore (for Core) and EventSender (for Publishing).
 */
@Component
@Primary
public class JpaPushQueueAdapter implements PushQueueStore, EventSender {

    private static final Logger log = LoggerFactory.getLogger(JpaPushQueueAdapter.class);
    private final PushMessageRepository repository;

    public JpaPushQueueAdapter(PushMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CompletableFuture<Void> send(String streamId, String url, String token, String authHeader) {
        return enqueue(streamId, url, token, authHeader);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> enqueue(String streamId, String url, String token, String authHeader) {
        PushMessageEntity entity = new PushMessageEntity();
        entity.setStreamId(streamId);
        entity.setEndpointUrl(url);
        entity.setSignedToken(token);
        entity.setAuthHeader(authHeader);
        entity.setStatus(PushMessageEntity.DeliveryStatus.PENDING);
        entity.setNextRetryAt(Instant.now());

        repository.save(entity);
        log.debug("Enqueued message for stream: {}", streamId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<PushMessage> fetchReadyMessages(int batchSize) {
        return repository.findReadyToProcess(Instant.now(), PageRequest.of(0, batchSize))
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long messageId, String statusName, String lastError, Long nextRetryAt, int retryCount) {
        repository.findById(messageId).ifPresent(msg -> {
            msg.setStatus(PushMessageEntity.DeliveryStatus.valueOf(statusName));
            msg.setLastError(lastError);
            msg.setRetryCount(retryCount);
            if (nextRetryAt != null) {
                msg.setNextRetryAt(Instant.ofEpochMilli(nextRetryAt));
            }
            repository.save(msg);
        });
    }

    private PushMessage toModel(PushMessageEntity entity) {
        PushMessage model = new PushMessage();
        model.setId(entity.getId());
        model.setStreamId(entity.getStreamId());
        model.setEndpointUrl(entity.getEndpointUrl());
        model.setAuthHeader(entity.getAuthHeader());
        model.setSignedToken(entity.getSignedToken());
        model.setStatus(entity.getStatus().name());
        model.setRetryCount(entity.getRetryCount());
        model.setNextRetryAt(entity.getNextRetryAt());
        model.setLastError(entity.getLastError());
        return model;
    }
}