package com.nevzatcirak.sharedsignals.core.mapper;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import com.nevzatcirak.sharedsignals.api.exception.SsfErrorCode;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain Service responsible for mapping internal events to standardized SSF Protocol formats.
 * <p>
 * This class handles:
 * <ul>
 * <li>Resolving the correct Event Type URI (e.g., RISC vs CAEP vs Opt-Out variants).</li>
 * <li>Enforcing protocol-specific rules (e.g., ensuring timestamps exist).</li>
 * <li>Handling default values for optional fields (e.g., empty strings for reasons).</li>
 * </ul>
 * This implementation is <b>Framework-Agnostic</b> (Pure Java).
 * </p>
 */
public class SecurityEventMapper {

    /**
     * Maps a generic security event to the strict format required by the SSF Specification.
     *
     * @param event The generic source event.
     * @return A {@link MappedEvent} containing the resolved Type URI and standardized Payload.
     */
    public MappedEvent map(GenericSecurityEvent event) {
        SecurityIntent intent = event.getIntent();

        // Clone payload to avoid side-effects on the source object
        Map<String, Object> finalPayload = new HashMap<>(event.getPayload());

        String typeUri = resolveTypeUri(intent, finalPayload);

        if (!finalPayload.containsKey("event_timestamp")) {
            finalPayload.put("event_timestamp", event.getOccurrenceTime().toEpochMilli());
        }

        // Rule: ACCOUNT_DISABLED should have a reason field, even if empty, for schema consistency.
        if (intent == SecurityIntent.ACCOUNT_DISABLED && finalPayload.get("reason") == null) {
            finalPayload.put("reason", "");
        }

        // Clean up internal fields (e.g., discriminator used by Adapter)
        finalPayload.remove("intent");

        return new MappedEvent(typeUri, event.getSubject(), finalPayload);
    }

    /**
     * Resolves the standard OpenID URI for a given Security Intent.
     * Handles complex logic like splitting 'OPT_OUT' into 4 different URIs based on state.
     */
    private String resolveTypeUri(SecurityIntent intent, Map<String, Object> payload) {
        return switch (intent) {
            // --- RISC ---
            case ACCOUNT_DISABLED -> SharedSignalConstants.RISC_ACCOUNT_DISABLED;
            case ACCOUNT_ENABLED -> SharedSignalConstants.RISC_ACCOUNT_ENABLED;
            case ACCOUNT_PURGED -> SharedSignalConstants.RISC_ACCOUNT_PURGED;
            case CREDENTIAL_COMPROMISE -> SharedSignalConstants.RISC_CREDENTIAL_COMPROMISE;
            case ACCOUNT_CREDENTIAL_CHANGE_REQUIRED -> SharedSignalConstants.RISC_ACCOUNT_CREDENTIAL_CHANGE_REQUIRED;
            case IDENTIFIER_CHANGED -> SharedSignalConstants.RISC_IDENTIFIER_CHANGED;
            case IDENTIFIER_RECYCLED -> SharedSignalConstants.RISC_IDENTIFIER_RECYCLED;
            case RECOVERY_ACTIVATED -> SharedSignalConstants.RISC_RECOVERY_ACTIVATED;
            case RECOVERY_INFORMATION_CHANGED -> SharedSignalConstants.RISC_RECOVERY_INFORMATION_CHANGED;

            case OPT_OUT -> {
                String state = (String) payload.get("state");
                if (state == null) {
                    throw new SsfBadRequestException(SsfErrorCode.INVALID_STREAM_CONFIGURATION, "State is required for OPT_OUT event");
                }
                yield switch (state) {
                    case "opt-in" -> SharedSignalConstants.RISC_OPT_IN;
                    case "opt-out-initiated" -> SharedSignalConstants.RISC_OPT_OUT_INITIATED;
                    case "opt-out-cancelled" -> SharedSignalConstants.RISC_OPT_OUT_CANCELLED;
                    case "opt-out-effective" -> SharedSignalConstants.RISC_OPT_OUT_EFFECTIVE;
                    default -> throw new SsfBadRequestException(SsfErrorCode.INVALID_STREAM_CONFIGURATION, "Unknown Opt-Out state: " + state);
                };
            }

            // --- CAEP ---
            case SESSION_REVOKED -> SharedSignalConstants.CAEP_SESSION_REVOKED;
            case SESSION_ESTABLISHED -> SharedSignalConstants.CAEP_SESSION_ESTABLISHED;
            case SESSION_PRESENTED -> SharedSignalConstants.CAEP_SESSION_PRESENTED;
            case CREDENTIAL_CHANGE -> SharedSignalConstants.CAEP_CREDENTIAL_CHANGE;
            case TOKEN_CLAIMS_CHANGE -> SharedSignalConstants.CAEP_TOKEN_CLAIMS_CHANGE;
            case ASSURANCE_LEVEL_CHANGE -> SharedSignalConstants.CAEP_ASSURANCE_LEVEL_CHANGE;
            case DEVICE_COMPLIANCE_CHANGE -> SharedSignalConstants.CAEP_DEVICE_COMPLIANCE_CHANGE;
            case RISK_LEVEL_CHANGE -> SharedSignalConstants.CAEP_RISK_LEVEL_CHANGE;

            // --- SSF ---
            case VERIFICATION -> SharedSignalConstants.SSF_VERIFICATION;
        };
    }

    /**
     * DTO for holding the resolved URI and Payload.
     */
    public record MappedEvent(String typeUri, Map<String, Object> subject, Map<String, Object> payload) {}
}