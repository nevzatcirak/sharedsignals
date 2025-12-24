package com.nevzatcirak.sharedsignals.api.enums;

/**
 * Internal representation of the "intent" or "event type" for logic routing.
 * Pure Java Enum without external dependencies.
 */
public enum SecurityIntent {
    // RISC
    ACCOUNT_DISABLED("account_disabled"),
    ACCOUNT_ENABLED("account_enabled"),
    ACCOUNT_PURGED("account_purged"),
    CREDENTIAL_COMPROMISE("credential_compromise"),
    ACCOUNT_CREDENTIAL_CHANGE_REQUIRED("account_credential_change_required"),
    IDENTIFIER_CHANGED("identifier_changed"),
    IDENTIFIER_RECYCLED("identifier_recycled"),
    OPT_OUT("opt_out"),
    RECOVERY_ACTIVATED("recovery_activated"),
    RECOVERY_INFORMATION_CHANGED("recovery_information_changed"),

    // CAEP
    SESSION_REVOKED("session_revoked"),
    SESSION_ESTABLISHED("session_established"),
    SESSION_PRESENTED("session_presented"),
    CREDENTIAL_CHANGE("credential_change"),
    TOKEN_CLAIMS_CHANGE("token_claims_change"),
    ASSURANCE_LEVEL_CHANGE("assurance_level_change"),
    DEVICE_COMPLIANCE_CHANGE("device_compliance_change"),
    RISK_LEVEL_CHANGE("risk_level_change"),

    // SSF
    VERIFICATION("verification");

    private final String value;

    SecurityIntent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SecurityIntent fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SecurityIntent b : SecurityIntent.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(value) || b.name().equalsIgnoreCase(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown Security Intent: '" + value + "'");
    }
}