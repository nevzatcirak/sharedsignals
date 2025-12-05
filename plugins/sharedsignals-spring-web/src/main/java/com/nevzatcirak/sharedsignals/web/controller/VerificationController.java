
package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import com.nevzatcirak.sharedsignals.api.service.VerificationService;
import com.nevzatcirak.sharedsignals.web.mapper.VerificationMapper;
import com.nevzatcirak.sharedsignals.web.model.SSFVerificationRequest;
import org.springframework.http.CacheControl;
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
    public ResponseEntity<Void> triggerVerification(@RequestBody SSFVerificationRequest request) {
        // Fail-fast validation
        if (request.getStreamId() == null || request.getStreamId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String clientId = authFacade.getClientId();
        verificationService.triggerVerification(verificationMapper.toCommand(request), clientId);

        // SSF Spec 8.1.4.2: Return 204 No Content (event queued)
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}