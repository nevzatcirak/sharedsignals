package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;
import com.nevzatcirak.sharedsignals.api.exception.StreamNotFoundException;
import com.nevzatcirak.sharedsignals.api.model.*;
import com.nevzatcirak.sharedsignals.api.service.SubjectManagementService;
import com.nevzatcirak.sharedsignals.api.spi.StreamStore;
import com.nevzatcirak.sharedsignals.core.validation.SubjectValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of SubjectManagementService.
 * <p>
 * Implements SSF Draft Spec Section 8.1.3 (Subject Management).
 * <p>
 * <b>Security Considerations (SSF Section 9):</b>
 * <ul>
 *   <li><b>9.1 Subject Probing:</b> This implementation uses silent ignore to prevent
 *       information leakage. Returns 200 OK even if subject is not actually added.</li>
 *   <li><b>9.2 Information Harvesting:</b> Validates subjects and checks opt-out status
 *       before adding. Transmitters should only send events they are authorized to share.</li>
 *   <li><b>9.3 Malicious Removal:</b> Subject removal is recorded with grace period support
 *       to continue sending events temporarily after removal.</li>
 * </ul>
 */
public class DefaultSubjectManagementService implements SubjectManagementService {

    private static final Logger log = LoggerFactory.getLogger(DefaultSubjectManagementService.class);
    private final StreamStore streamStore;

    public DefaultSubjectManagementService(StreamStore streamStore) {
        this.streamStore = streamStore;
    }

    /**
     * Adds a subject to a stream.
     * <p>
     * SSF Spec 8.1.3.2: The Transmitter MAY silently ignore the request if:
     * - The subject has opted out of event transmission
     * - The subject is not recognized
     * - Other privacy/security reasons
     *
     * @param command add subject command with stream_id, subject, and verified flag
     * @param owner the authenticated client ID (from JWT)
     */
    @Override
    public void addSubject(AddSubjectCommand command, String owner) {
        validateOwner(command.getStreamId(), owner);
        SubjectValidator.validate(command.getSubject());

        if (shouldSilentlyIgnoreSubject(command.getSubject())) {
            log.info("Silently ignoring add subject request for stream {} due to opt-out",
                    command.getStreamId());
            return;
        }
        streamStore.addSubject(command.getStreamId(), command.getSubject(), command.isVerified());
    }

    /**
     * Removes a subject from a stream.
     * <p>
     * SSF Spec 8.1.3.3: The Transmitter MAY silently ignore the request if
     * the subject is not recognized.
     *
     * @param command remove subject command with stream_id and subject
     * @param owner the authenticated client ID (from JWT)
     */
    @Override
    public void removeSubject(RemoveSubjectCommand command, String owner) {
        validateOwner(command.getStreamId(), owner);
        SubjectValidator.validate(command.getSubject());
        streamStore.removeSubject(command.getStreamId(), command.getSubject());
    }

    /**
     * Validates that the authenticated client is an audience of the stream.
     *
     * @param streamId the stream identifier
     * @param owner the authenticated client ID
     * @throws StreamNotFoundException if stream doesn't exist
     * @throws SsfSecurityException if owner is not in aud
     */
    private void validateOwner(String streamId, String owner) {
        StreamConfiguration stream = streamStore.findById(streamId)
                .orElseThrow(() -> new StreamNotFoundException(streamId));

        if (stream.getAud() == null || !stream.getAud().contains(owner)) {
            throw new SsfSecurityException("Access Denied: You are not an audience of this stream.");
        }
    }

    /**
     * Checks if a subject should be silently ignored.
     * <p>
     * SSF Spec 8.1.3.2: Transmitter MAY silently ignore requests for subjects
     * who have opted out or for other privacy/security reasons.
     * <p>
     * Implementation Note: This is a placeholder for opt-out logic.
     * In production, integrate with your privacy/consent management system.
     *
     * @param subject the subject to check
     * @return true if subject should be ignored, false otherwise
     */
    private boolean shouldSilentlyIgnoreSubject(java.util.Map<String, Object> subject) {
        // TODO: Implement opt-out check logic
        // Example scenarios:
        // 1. Check if email/phone is in opt-out list
        // 2. Check if user has revoked consent
        // 3. Check if subject is in blocklist
        // 4. Check privacy regulations (GDPR, CCPA, etc.)

        return false;  // Default: don't ignore
    }
}