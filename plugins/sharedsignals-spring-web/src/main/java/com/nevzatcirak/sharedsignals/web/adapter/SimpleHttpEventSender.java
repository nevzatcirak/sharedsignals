package com.nevzatcirak.sharedsignals.web.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.api.constant.IanaSetErrorCodes;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.exception.EventsDeliveredFailureException;
import com.nevzatcirak.sharedsignals.api.spi.EventSender;
import com.nevzatcirak.sharedsignals.web.model.SetErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class SimpleHttpEventSender implements EventSender {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpEventSender.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private static final Set<String> AUTO_PAUSE_CODES = Set.of(
            IanaSetErrorCodes.INVALID_KEY,
            IanaSetErrorCodes.INVALID_ISSUER
    );

    public SimpleHttpEventSender(ObjectMapper objectMapper, RestTemplateBuilder builder) {
        this.objectMapper = objectMapper;

        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public CompletableFuture<Void> send(String streamId, String url, String token, String authHeader) {
        return CompletableFuture.runAsync(() -> {
            attemptSend(streamId, url, token, authHeader, 1, INITIAL_BACKOFF_MS);
        });
    }

    private void attemptSend(String streamId, String url, String token, String authHeader, int attempt, long waitTime) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(SharedSignalConstants.MEDIA_TYPE_SECEVENT_JWT));
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "SSF-Transmitter/1.0");

            if (authHeader != null && !authHeader.isBlank()) {
                headers.set("Authorization", authHeader);
            }

            HttpEntity<String> request = new HttpEntity<>(token, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SECURE EVENT DELIVERED -> Stream: {} | URL: {} | Status: {}", streamId, url, response.getStatusCode());
            } else {
                handleFailure(streamId, url, token, authHeader, attempt, waitTime, new RuntimeException("Unexpected status: " + response.getStatusCode()));
            }

        } catch (Exception e) {
            handleFailure(streamId, url, token, authHeader, attempt, waitTime, e);
        }
    }

    private void handleFailure(String streamId, String url, String token, String authHeader, int attempt, long waitTime, Exception e) {
        SetErrorResponse errorResponse = null;
        if (e instanceof RestClientResponseException) {
            errorResponse = parseErrorResponse((RestClientResponseException) e);
            if (errorResponse != null && errorResponse.getErr() != null) {
                log.error("Receiver [{}] returned SET Error Code: [{}] - Description: [{}]", url, errorResponse.getErr(), errorResponse.getDescription());
            }
        }
        if (isFatal(e, errorResponse)) {
            log.error("FATAL Delivery Error to {}: {}. Dropping event.", url, e.getMessage());
            triggerAutoPause(streamId, e, errorResponse);
            return;
        }
        if (attempt >= MAX_RETRIES) {
            log.error("MAX RETRIES EXCEEDED for {}. Event dropped. Last error: {}", url, e.getMessage());
            return;
        }
        log.warn("Delivery failed to {} (Attempt {}/{}). Retrying in {} ms.", url, attempt, MAX_RETRIES, waitTime);
        try {
            TimeUnit.MILLISECONDS.sleep(waitTime);
            attemptSend(streamId, url, token, authHeader, attempt + 1, (long) (waitTime * BACKOFF_MULTIPLIER));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
    private boolean isFatal(Exception e, SetErrorResponse errorResponse) {
        if (e instanceof HttpClientErrorException) {
            int statusCode = ((HttpClientErrorException) e).getStatusCode().value();
            if (statusCode == 429) return false;
            return true;
        }
        return false;
    }
    private void triggerAutoPause(String streamId, Exception e, SetErrorResponse errorResponse) {
        String reason = null;
        if (e instanceof HttpClientErrorException) {
            int statusCode = ((HttpClientErrorException) e).getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                reason = "Automatic pause: Receiver returned " + statusCode + " (Authentication Failed).";
            } else if (errorResponse != null && AUTO_PAUSE_CODES.contains(errorResponse.getErr())) {
                reason = "Automatic pause: Fatal Configuration Error [" + errorResponse.getErr() + "]";
            }
        }
        if (reason != null) {
            log.warn("Pausing Stream [{}] due to fatal error: {}", streamId, reason);
            throw new EventsDeliveredFailureException(reason);
        }
    }
    private SetErrorResponse parseErrorResponse(RestClientResponseException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                return objectMapper.readValue(responseBody, SetErrorResponse.class);
            }
        } catch (Exception parseEx) {
            log.debug("Could not parse error response body: {}", parseEx.getMessage());
        }
        return null;
    }
}
