package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.StreamConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Stream Configuration Management.
 * <p>
 * Implements SSF Draft Spec Section 8.1.1 (Stream Configuration).
 * Endpoint: /ssf/stream
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-secevent-sharedsignals">SSF Specification</a>
 */
@RestController
@RequestMapping("/ssf/stream")
public class StreamConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(StreamConfigurationController.class);
    private final StreamConfigurationService streamService;
    private final AuthFacade authFacade;

    public StreamConfigurationController(StreamConfigurationService streamService, AuthFacade authFacade) {
        this.streamService = streamService;
        this.authFacade = authFacade;
    }

    /**
     * Creates a new Event Stream.
     * <p>
     * SSF Spec Section 8.1.1.1: Creating a Stream
     * <p>
     * IMPORTANT: Only Receiver-Supplied properties should be in the request body:
     * - events_requested (optional)
     * - delivery (required, defaults to POLL if omitted)
     * - description (optional)
     * <p>
     * All Transmitter-Supplied properties will be ignored if present in the request.
     *
     * @param body the stream configuration request (only Receiver-Supplied properties)
     * @return 201 Created with the created stream configuration
     */
    @PostMapping
    public ResponseEntity<StreamConfiguration> createStream(@RequestBody StreamConfiguration body) {
        if (body.getStream_id() != null) {
            log.warn("Client attempted to supply stream_id in create request. This will be ignored.");
        }
        if (body.getIss() != null) {
            log.warn("Client attempted to supply iss in create request. This will be ignored.");
        }
        if (body.getAud() != null && !body.getAud().isEmpty()) {
            log.warn("Client attempted to supply aud in create request. This will be ignored for security.");
        }
        if (body.getEvents_supported() != null) {
            log.warn("Client attempted to supply events_supported in create request. This will be ignored.");
        }
        if (body.getEvents_delivered() != null) {
            log.warn("Client attempted to supply events_delivered in create request. This will be ignored.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(streamService.createStream(body, authFacade.getClientId()));
    }

    /**
     * Reads stream configuration(s).
     * <p>
     * SSF Spec Section 8.1.1.2: Reading a Stream's Configuration
     * - If stream_id is provided: returns single stream
     * - If stream_id is omitted: returns list of all streams for the receiver
     *
     * @param streamId optional stream identifier
     * @return 200 OK with stream configuration or list of configurations
     */
    @GetMapping
    public ResponseEntity<?> getStream(@RequestParam(name = "stream_id", required = false) String streamId) {
        String owner = authFacade.getClientId();

        if (streamId != null) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(streamService.getStream(streamId, owner));
        } else {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(streamService.listStreams(owner));
        }
    }

    /**
     * Replaces a stream's configuration.
     * <p>
     * SSF Spec Section 8.1.1.4: Replacing a Stream's Configuration
     * All Receiver-Supplied properties must be present in the request body.
     *
     * @param body the complete replacement configuration
     * @return 200 OK with the updated stream configuration
     */
    @PutMapping
    public ResponseEntity<StreamConfiguration> replaceStream(@RequestBody StreamConfiguration body) {
        if (body.getStream_id() == null || body.getStream_id().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (body.getEvents_requested() != null && body.getEvents_requested().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(streamService.replaceStream(body.getStream_id(), body, authFacade.getClientId()));
    }

    /**
     * Updates specific properties of a stream's configuration.
     * <p>
     * SSF Spec Section 8.1.1.3: Updating a Stream's Configuration
     * Only provided properties will be updated; missing properties are unchanged.
     *
     * @param body partial stream configuration with properties to update
     * @return 200 OK with the updated stream configuration
     */
    @PatchMapping
    public ResponseEntity<StreamConfiguration> updateStream(@RequestBody StreamConfiguration body) {
        if (body.getStream_id() == null || body.getStream_id().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (body.getEvents_requested() != null && body.getEvents_requested().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(streamService.updateStream(body.getStream_id(), body, authFacade.getClientId()));
    }

    /**
     * Deletes an Event Stream.
     * <p>
     * SSF Spec Section 8.1.1.5: Deleting a Stream
     *
     * @param streamId the stream identifier
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteStream(@RequestParam("stream_id") String streamId) {
        streamService.deleteStream(streamId, authFacade.getClientId());
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}