package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.model.PollCommand;
import com.nevzatcirak.sharedsignals.api.model.PollResult;
import com.nevzatcirak.sharedsignals.api.service.EventRetrievalService;
import com.nevzatcirak.sharedsignals.web.mapper.PollMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFPollRequest;
import com.nevzatcirak.sharedsignals.web.model.SSFPollResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ssf/events")
public class EventsController {

    private static final Logger log = LoggerFactory.getLogger(EventsController.class);
    private final EventRetrievalService eventRetrievalService;
    private final PollMapper pollMapper;

    public EventsController(EventRetrievalService eventRetrievalService, PollMapper pollMapper) {
        this.eventRetrievalService = eventRetrievalService;
        this.pollMapper = pollMapper;
    }

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

        PollCommand command = pollMapper.toCommand(request);
        PollResult result = eventRetrievalService.pollEvents(streamId, command);

        return ResponseEntity.ok(pollMapper.toResponse(result));
    }
}
