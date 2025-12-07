package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.service.EventRetrievalService;
import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import com.nevzatcirak.sharedsignals.web.mapper.PollMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFPollRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFPollResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for POLL-based event delivery.
 * Implements RFC 8936 with Non-Blocking Async support.
 */
@RestController
@RequestMapping("/ssf/events")
@Tag(name = "Events (Poll Delivery)", description = "Poll endpoint for RFC 8936 compliant event retrieval.")
@SecurityRequirement(name = "bearer-key")
public class EventsController {

    private final EventRetrievalService eventRetrievalService;
    private final InactivityTimeoutService inactivityService;
    private final PollMapper pollMapper;

    public EventsController(EventRetrievalService eventRetrievalService, InactivityTimeoutService inactivityService, PollMapper pollMapper) {
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
    @Operation(
        summary = "Poll for Events",
        description = "Retrieves buffered events for the stream. Supports Long Polling via 'returnImmediately' flag."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events returned successfully.", content = @Content(schema = @Schema(implementation = SSFPollResponse.class))),
        @ApiResponse(responseCode = "404", description = "Stream not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CompletableFuture<ResponseEntity<SSFPollResponse>> pollEvents(
            @PathVariable("streamId") String streamId,
            @RequestHeader(value = HttpHeaders.CONTENT_LANGUAGE, required = false) String contentLanguage,
            @RequestBody(required = false) SSFPollRequest request) {

        if (request == null) request = new SSFPollRequest();

        inactivityService.recordActivity(streamId);
        PollCommand command = pollMapper.toCommand(request);

        return eventRetrievalService.pollEventsAsync(streamId, command)
                .thenApply(result -> ResponseEntity.ok(pollMapper.toResponse(result)));
    }
}