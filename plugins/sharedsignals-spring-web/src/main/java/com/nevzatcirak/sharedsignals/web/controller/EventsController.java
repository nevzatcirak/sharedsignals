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

/**
 * REST Controller for POLL-based event delivery.
 * <p>
 * Implements RFC 8936: Poll-Based Security Event Token (SET) Delivery Using HTTP
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
     * Polls for events from a stream.
     * <p>
     * RFC 8936 Section 2.2: Poll Request
     * <p>
     * SSF Spec 8.1.1: Polling constitutes eligible Receiver activity (resets inactivity timer).
     *
     * @param streamId the stream identifier (path variable)
     * @param request the poll request (optional body with ack, setErrs, maxEvents, returnImmediately)
     * @return poll response containing events and moreAvailable flag
     */
    @PostMapping("/poll/{streamId}")
    public ResponseEntity<SSFPollResponse> pollEvents(
            @PathVariable("streamId") String streamId,
            @RequestHeader(value = HttpHeaders.CONTENT_LANGUAGE, required = false) String contentLanguage,
            @RequestBody(required = false) SSFPollRequest request) {

        if (contentLanguage != null && request != null && request.getSetErrs() != null && !request.getSetErrs().isEmpty()) {
            log.info("Processing Poll Errors for Stream [{}] in language: {}", streamId, contentLanguage);
        }

        if (request == null) {
            request = new SSFPollRequest();
        }
        inactivityService.recordActivity(streamId);
        PollCommand command = pollMapper.toCommand(request);
        PollResult result = eventRetrievalService.pollEvents(streamId, command);

        return ResponseEntity.ok(pollMapper.toResponse(result));
    }
}
