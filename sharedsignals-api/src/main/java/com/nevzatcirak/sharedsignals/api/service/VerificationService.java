package com.nevzatcirak.sharedsignals.api.service;

import com.nevzatcirak.sharedsignals.api.model.VerificationRequest;

/**
 * Service interface for triggering stream verification events.
 */
public interface VerificationService {
    void triggerVerification(VerificationRequest request, String owner);
}
