package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.TriggerVerificationCommand;

/**
 * Service interface for Verification operations.
 * <p>
 * SSF Spec Section 8.1.4: Verification
 * <p>
 * Verification Events allow Event Receivers to confirm that the stream
 * is configured correctly and that end-to-end delivery is working.
 */
public interface VerificationService {

    /**
     * Triggers a verification event for the specified stream.
     * <p>
     * SSF Spec Section 8.1.4.2: Triggering a Verification Event
     * <p>
     * The Transmitter will send a Verification Event over the stream.
     * This is typically an asynchronous operation - the response indicates
     * that the event has been queued, not that it has been delivered.
     * <p>
     * Rate Limiting: Transmitters MAY reject requests that exceed
     * min_verification_interval with HTTP 429.
     *
     * @param command the verification trigger command
     * @param owner the authenticated client ID (from JWT)
     * @throws com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException if stream doesn't exist
     * @throws com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException if owner is not in aud
     * @throws com.nevzatcirak.sharedsignals.api.exception.RateLimitExceededException if too many requests
     */
    void triggerVerification(TriggerVerificationCommand command, String owner);
}