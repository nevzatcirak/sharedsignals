package com.nevzatcirak.sharedsignals.api.spi;

import java.util.Map;

/**
 * SPI for privacy policy and consent validation.
 * <p>
 * SSF Spec Section 10: Privacy Considerations
 * <p>
 * This interface allows implementors to plug in their own privacy policy
 * and consent management logic. The default implementation permits all
 * data sharing (assumes consent is managed externally).
 * <p>
 * <b>Privacy Requirements:</b>
 * <ul>
 *   <li><b>10.1 Subject Information Leakage:</b> Validate subject identifier consistency</li>
 *   <li><b>10.2 Previously Consented Data:</b> Check if data was previously shared</li>
 *   <li><b>10.3 New Data:</b> Verify consent for new attributes</li>
 * </ul>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-secevent-sharedsignals#section-10">SSF Spec Section 10</a>
 */
public interface PrivacyPolicyValidator {

    /**
     * Validates if the event can be shared with the receiver based on privacy policy.
     * <p>
     * SSF Spec 10.3: Transmitters MUST obtain consent before sharing new data.
     *
     * @param streamId the stream identifier
     * @param subject the subject of the event
     * @param eventTypeUri the event type URI
     * @param eventDetails the event payload
     * @param receiverAudience the receiver audience (from stream aud)
     * @return validation result
     */
    PrivacyValidationResult validateEventSharing(
            String streamId,
            Map<String, Object> subject,
            String eventTypeUri,
            Map<String, Object> eventDetails,
            String receiverAudience
    );

    /**
     * Validates if the subject identifier type is appropriate for the receiver.
     * <p>
     * SSF Spec 10.1: Parties SHOULD NOT identify a subject using a given
     * Subject Identifier Type if doing so will allow the recipient to
     * correlate different claims about the subject.
     *
     * @param subject the subject identifier
     * @param receiverAudience the receiver audience
     * @return validation result
     */
    PrivacyValidationResult validateSubjectIdentifier(
            Map<String, Object> subject,
            String receiverAudience
    );

    /**
     * Checks if the receiver has consent to receive data about the subject.
     * <p>
     * SSF Spec 10.3.2: Transmitters MUST obtain consent to release
     * consentable data from the user.
     *
     * @param subject the subject
     * @param receiverAudience the receiver audience
     * @return true if consent exists, false otherwise
     */
    boolean hasConsentToShareWithReceiver(
            Map<String, Object> subject,
            String receiverAudience
    );

    /**
     * Result of privacy validation.
     */
    class PrivacyValidationResult {
        private final boolean allowed;
        private final String reason;

        private PrivacyValidationResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public static PrivacyValidationResult allowed() {
            return new PrivacyValidationResult(true, null);
        }

        public static PrivacyValidationResult denied(String reason) {
            return new PrivacyValidationResult(false, reason);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }
    }
}