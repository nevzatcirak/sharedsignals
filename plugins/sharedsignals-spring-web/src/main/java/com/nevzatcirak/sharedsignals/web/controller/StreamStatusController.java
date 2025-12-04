package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.model.StreamStatus;
import com.nevzatcirak.sharedsignals.api.service.StreamStatusService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing the status of Event Streams.
 * <p>
 * Implements the endpoints defined in SSF Specification Section 8.1.2.
 */
@RestController
@RequestMapping("/ssf/status")
public class StreamStatusController {

    private final StreamStatusService statusService;
    private final AuthFacade authFacade;

    public StreamStatusController(StreamStatusService statusService, AuthFacade authFacade) {
        this.statusService = statusService;
        this.authFacade = authFacade;
    }

    /**
     * Checks the current status of an Event Stream.
     * <p>
     * See SSF Specification Section 8.1.2.1.
     *
     * @param streamId A string identifying the stream whose status is being queried.
     * @return A {@link ResponseEntity} containing the {@link StreamStatus} and HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<StreamStatus> getStreamStatus(@RequestParam(name = "stream_id") String streamId) {
        String owner = authFacade.getClientId();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(statusService.getStatus(streamId, owner));
    }

    /**
     * Updates the current status of an Event Stream.
     * <p>
     * See SSF Specification Section 8.1.2.2.
     *
     * @param body The {@link StreamStatus} object containing the new status configuration.
     * The 'stream_id' and 'status' fields are required.
     * @return A {@link ResponseEntity} containing the updated {@link StreamStatus} and HTTP 200 OK,
     * or HTTP 400 Bad Request if required fields are missing.
     */
    @PostMapping
    public ResponseEntity<StreamStatus> updateStreamStatus(@RequestBody StreamStatus body) {
        if (body.getStream_id() == null || body.getStream_id().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (body.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        String owner = authFacade.getClientId();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(statusService.updateStatus(body.getStream_id(), body.getStatus(), body.getReason(), owner));
    }
}
