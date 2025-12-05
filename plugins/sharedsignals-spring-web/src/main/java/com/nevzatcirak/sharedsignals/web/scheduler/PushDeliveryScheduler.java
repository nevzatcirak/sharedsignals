package com.nevzatcirak.sharedsignals.web.scheduler;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.model.PushMessage;
import com.nevzatcirak.sharedsignals.api.service.PushQueueService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Worker that processes the Outbox Queue using the Service Interface.
 * <p>
 * Uses ShedLock to ensure only one instance processes the queue at a time
 * to prevent duplicate delivery attempts.
 */
@Component
@ConditionalOnProperty(name = "sharedsignals.features.push-delivery-enabled", havingValue = "true", matchIfMissing = true)
public class PushDeliveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PushDeliveryScheduler.class);
    private final PushQueueService queueService;
    private final RestTemplate restTemplate;

    private static final int BATCH_SIZE = 50;

    public PushDeliveryScheduler(PushQueueService queueService, RestTemplateBuilder restTemplateBuilder) {
        this.queueService = queueService;
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Processes pending push events.
     * <p>
     * Lock Configuration:
     * - name: Unique identifier for this task.
     * - lockAtMostFor: "30s" - If this node crashes, lock is released after 30s.
     * - lockAtLeastFor: "500ms" - Prevents rapid re-execution by other nodes if this task finishes instantly.
     */
    @Scheduled(fixedDelayString = "${sharedsignals.scheduler.push-delivery-interval:1000}")
    @SchedulerLock(name = "PushDeliveryScheduler_processOutbox", lockAtMostFor = "30s", lockAtLeastFor = "500ms")
    public void processOutbox() {
        List<PushMessage> messages = queueService.findReadyMessages(BATCH_SIZE);

        if (messages.isEmpty()) return;

        log.debug("Processing {} pending push events...", messages.size());

        for (PushMessage msg : messages) {
            try {
                processMessage(msg);
            } catch (Exception e) {
                log.error("Unexpected error processing message ID: " + msg.getId(), e);
            }
        }
    }

    private void processMessage(PushMessage msg) {
        try {
            queueService.markInProgress(msg.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(SharedSignalConstants.MEDIA_TYPE_SECEVENT_JWT));
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "SSF-Transmitter/1.0");

            if (msg.getAuthHeader() != null) {
                headers.set("Authorization", msg.getAuthHeader());
            }

            HttpEntity<String> request = new HttpEntity<>(msg.getSignedToken(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(msg.getEndpointUrl(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                queueService.markSuccess(msg.getId());
            } else {
                queueService.markFailure(msg.getId(), "HTTP " + response.getStatusCode(), msg.getRetryCount());
            }

        } catch (Exception e) {
            queueService.markFailure(msg.getId(), e.getMessage(), msg.getRetryCount());
        }
    }
}