package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.service.VerificationService;
import com.nevzatcirak.sharedsignals.web.mapper.VerificationMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFVerificationRequest;
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
 * REST Controller for Verification.
 * <p>
 * Implements SSF Draft Spec Section 8.1.4 (Verification).
 * <p>
 * Endpoint:
 * - POST /ssf/verification - Trigger a verification event
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-secevent-sharedsignals">SSF Specification</a>
 */
@RestController
@RequestMapping("/ssf/verification")
@Tag(name = "Verification", description = "Stream Verification. SSF Spec Section 8.1.4.")
@SecurityRequirement(name = "bearer-key")
public class VerificationController {

    private final VerificationService verificationService;
    private final AuthFacade authFacade;
    private final VerificationMapper verificationMapper;

    public VerificationController(
            VerificationService verificationService,
            AuthFacade authFacade,
            VerificationMapper verificationMapper) {
        this.verificationService = verificationService;
        this.authFacade = authFacade;
        this.verificationMapper = verificationMapper;
    }

    /**
     * Triggers a verification event for a stream.
     * <p>
     * SSF Spec Section 8.1.4.2: Triggering a Verification Event
     * <p>
     * The Transmitter will asynchronously send a Verification Event over the stream.
     * A successful 204 response indicates the event has been queued, NOT delivered.
     * <p>
     * Rate Limiting: Returns 429 if requests exceed min_verification_interval.
     *
     * @param request the verification request containing stream_id and optional state
     * @return 204 No Content on success (event queued)
     */
    @PostMapping
    @Operation(
        summary = "Trigger Verification",
        description = "Triggers a Verification Event to be sent over the stream. Returns 204 (Accepted) immediately; the event is delivered asynchronously."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Verification event queued."),
        @ApiResponse(responseCode = "429", description = "Too many verification requests (Rate Limit).", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Stream not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> triggerVerification(@RequestBody SSFVerificationRequest request) {
        if (request.getStreamId() == null || request.getStreamId().isBlank()) return ResponseEntity.badRequest().build();

        verificationService.triggerVerification(verificationMapper.toCommand(request), authFacade.getClientId());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}