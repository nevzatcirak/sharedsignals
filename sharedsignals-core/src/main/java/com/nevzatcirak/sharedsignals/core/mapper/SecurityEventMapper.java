package com.nevzatcirak.sharedsignals.core.mapper;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;

import java.util.HashMap;
import java.util.Map;

public class SecurityEventMapper {

    public MappedEvent map(GenericSecurityEvent event) {
        SecurityIntent intent = event.getIntent();
        Map<String, Object> inputPayload = event.getPayload();

        String typeUri = resolveTypeUri(intent, inputPayload);

        Map<String, Object> subjectMap = event.getSubject();

        Map<String, Object> finalPayload = new HashMap<>(inputPayload);

        if (!finalPayload.containsKey("event_timestamp")) {
            finalPayload.put("event_timestamp", event.getOccurrenceTime().toEpochMilli());
        }

        if (intent == SecurityIntent.ACCOUNT_DISABLED && finalPayload.get("reason") == null) {
            finalPayload.put("reason", "");
        }

        return new MappedEvent(typeUri, subjectMap, finalPayload);
    }

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
                if (state == null) throw new IllegalArgumentException("State is required for OPT_OUT event");
                yield switch (state) {
                    case "opt-in" -> SharedSignalConstants.RISC_OPT_IN;
                    case "opt-out-initiated" -> SharedSignalConstants.RISC_OPT_OUT_INITIATED;
                    case "opt-out-cancelled" -> SharedSignalConstants.RISC_OPT_OUT_CANCELLED;
                    case "opt-out-effective" -> SharedSignalConstants.RISC_OPT_OUT_EFFECTIVE;
                    default -> throw new IllegalArgumentException("Unknown Opt-Out state: " + state);
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

    public record MappedEvent(String typeUri, Map<String, Object> subject, Map<String, Object> payload) {}
}