package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.model.StreamStatus;
import com.nevzatcirak.sharedsignals.api.service.StreamStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing the status of Event Streams.
 * <p>
 * Implements the endpoints defined in SSF Specification Section 8.1.2.
 */
@RestController
@RequestMapping("/ssf/status")
@Tag(name = "Stream Status", description = "Check or update the status of a stream. SSF Spec Section 8.1.2.")
@SecurityRequirement(name = "bearer-key")
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
    @Operation(summary = "Get Stream Status", description = "Returns the current status (enabled/paused/disabled) of the stream.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status returned.", content = @Content(schema = @Schema(implementation = StreamStatus.class))),
        @ApiResponse(responseCode = "404", description = "Stream not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<StreamStatus> getStreamStatus(@RequestParam(name = "stream_id") String streamId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(statusService.getStatus(streamId, authFacade.getClientId()));
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
    @Operation(summary = "Update Stream Status", description = "Updates the status (e.g., to pause or restart the stream).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated.", content = @Content(schema = @Schema(implementation = StreamStatus.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<StreamStatus> updateStreamStatus(@RequestBody StreamStatus body) {
        if (body.getStream_id() == null || body.getStatus() == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(statusService.updateStatus(body.getStream_id(), body.getStatus(), body.getReason(), authFacade.getClientId()));
    }
}