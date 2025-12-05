package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
import com.nevzatcirak.sharedsignals.api.service.EventRetrievalService;
import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import com.nevzatcirak.sharedsignals.web.mapper.PollMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFPollRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFPollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for POLL-based event delivery.
 * Implements RFC 8936 with Non-Blocking Async support.
 */
@RestController
@RequestMapping("/ssf/events")
public class EventsController {

    private static final Logger log = LoggerFactory.getLogger(EventsController.class);
    private final EventRetrievalService eventRetrievalService;
    private final InactivityTimeoutService inactivityService;
    private final PollMapper pollMapper;

    public EventsController(
            EventRetrievalService eventRetrievalService,
            InactivityTimeoutService inactivityService,
            PollMapper pollMapper) {
        this.eventRetrievalService = eventRetrievalService;
        this.inactivityService = inactivityService;
        this.pollMapper = pollMapper;
    }

    /**
     * Polls for events asynchronously.
     *
     * @param streamId the stream identifier
     * @param request the poll request
     * @return A CompletableFuture resolving to the Poll Response
     */
    @PostMapping("/poll/{streamId}")
    public CompletableFuture<ResponseEntity<SSFPollResponse>> pollEvents(
            @PathVariable("streamId") String streamId,
            @RequestHeader(value = HttpHeaders.CONTENT_LANGUAGE, required = false) String contentLanguage,
            @RequestBody(required = false) SSFPollRequest request) {

        if (request == null) {
            request = new SSFPollRequest();
        }

        inactivityService.recordActivity(streamId);
        PollCommand command = pollMapper.toCommand(request);
        return eventRetrievalService.pollEventsAsync(streamId, command)
                .thenApply(result -> ResponseEntity.ok(pollMapper.toResponse(result)));
    }
}