package com.nevzatcirak.sharedsignals.core.privacy;

import com.nevzatcirak.sharedsignals.api.spi.PrivacyPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Example implementation of PrivacyPolicyValidator with basic privacy logic.
 * <p>
 * <b>THIS IS AN EXAMPLE:</b> Organizations should implement their own validator
 * that integrates with their consent management system.
 * <p>
 * This example shows:
 * <ul>
 *   <li>Subject identifier consistency tracking</li>
 *   <li>Basic consent verification</li>
 *   <li>Data classification</li>
 * </ul>
 */
public class ExampleCustomPrivacyValidator implements PrivacyPolicyValidator {

    private static final Logger log = LoggerFactory.getLogger(ExampleCustomPrivacyValidator.class);

    // Track subject identifier formats per receiver (in-memory for example)
    // Production: Use database or cache
    private final Map<String, Set<String>> subjectFormatsPerReceiver = new HashMap<>();

    // Example consent store (in-memory)
    // Production: Integrate with consent management system
    private final Set<String> consents = new HashSet<>();

    @Override
    public PrivacyValidationResult validateEventSharing(
            String streamId,
            Map<String, Object> subject,
            String eventTypeUri,
            Map<String, Object> eventDetails,
            String receiverAudience) {

        // Example: Check if this event type requires consent
        if (requiresConsent(eventTypeUri)) {
            String consentKey = generateConsentKey(subject, receiverAudience, eventTypeUri);
            if (!consents.contains(consentKey)) {
                log.warn("No consent for event type: {} to receiver: {}", eventTypeUri, receiverAudience);
                return PrivacyValidationResult.denied("Missing consent for event type");
            }
        }

        // Example: Check if event details contain only previously shared data
        if (containsNewData(eventDetails)) {
            // Verify consent for new data
            String consentKey = generateConsentKey(subject, receiverAudience, "new_data");
            if (!consents.contains(consentKey)) {
                log.warn("Event contains new data without consent for receiver: {}", receiverAudience);
                return PrivacyValidationResult.denied("New data without consent");
            }
        }

        return PrivacyValidationResult.allowed();
    }

    @Override
    public PrivacyValidationResult validateSubjectIdentifier(
            Map<String, Object> subject,
            String receiverAudience) {

        String format = (String) subject.get("format");
        if (format == null) {
            return PrivacyValidationResult.denied("Subject format not specified");
        }

        // SSF Spec 10.1: Check format consistency
        Set<String> usedFormats = subjectFormatsPerReceiver.computeIfAbsent(
                receiverAudience, k -> new HashSet<>());

        if (!usedFormats.isEmpty() && !usedFormats.contains(format)) {
            log.warn("Subject format inconsistency for receiver {}: was {}, now {}",
                    receiverAudience, usedFormats, format);
            return PrivacyValidationResult.denied(
                    "Subject identifier format inconsistent (prevents correlation)");
        }

        usedFormats.add(format);
        return PrivacyValidationResult.allowed();
    }

    @Override
    public boolean hasConsentToShareWithReceiver(
            Map<String, Object> subject,
            String receiverAudience) {

        String consentKey = generateConsentKey(subject, receiverAudience, "general");
        return consents.contains(consentKey);
    }

    // Helper methods

    private boolean requiresConsent(String eventTypeUri) {
        // Example: Session events don't need additional consent (organizational data)
        // Password changes need consent (consentable data)
        return eventTypeUri.contains("credential-change") ||
                eventTypeUri.contains("token-claims-change");
    }

    private boolean containsNewData(Map<String, Object> eventDetails) {
        // Example: Check if event contains fields that weren't previously shared
        // Production: Query your data sharing history
        return eventDetails.containsKey("new_attribute");
    }

    private String generateConsentKey(Map<String, Object> subject, String receiver, String scope) {
        return subject.toString() + ":" + receiver + ":" + scope;
    }

    // Example method to grant consent (for testing)
    public void grantConsent(Map<String, Object> subject, String receiver, String scope) {
        String key = generateConsentKey(subject, receiver, scope);
        consents.add(key);
        log.info("Granted consent: {}", key);
    }
}