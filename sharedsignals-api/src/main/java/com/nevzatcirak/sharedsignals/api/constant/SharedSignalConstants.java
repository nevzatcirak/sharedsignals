package com.nevzatcirak.sharedsignals.api.constant;

import java.util.List;

public final class SharedSignalConstants {
    private SharedSignalConstants() {}

    // --- Specifications & Protocols ---
    public static final String SPEC_VERSION_1_0 = "1_0";
    public static final String SPEC_URN_OAUTH_2 = "urn:ietf:rfc:6749";
    public static final String MEDIA_TYPE_SECEVENT_JWT = "application/secevent+jwt";
    public static final String JWT_TYPE_SECEVENT = "secevent+jwt";

    // --- Delivery Methods ---
    public static final String DELIVERY_METHOD_PUSH = "urn:ietf:rfc:8935";
    public static final String DELIVERY_METHOD_POLL = "urn:ietf:rfc:8936";

    // --- Stream Lifecycle Status ---
    public static final String STATUS_ENABLED = "enabled";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_DISABLED = "disabled";
    public static final String DEFAULT_SUBJECT_MODE = "NONE";

    // --- CAEP Events ---
    public static final String CAEP_SESSION_REVOKED = "https://schemas.openid.net/secevent/caep/event-type/session-revoked";
    public static final String CAEP_SESSION_ESTABLISHED = "https://schemas.openid.net/secevent/caep/event-type/session-established";
    public static final String CAEP_SESSION_PRESENTED = "https://schemas.openid.net/secevent/caep/event-type/session-presented";
    public static final String CAEP_CREDENTIAL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/credential-change";
    public static final String CAEP_TOKEN_CLAIMS_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/token-claims-change";
    public static final String CAEP_ASSURANCE_LEVEL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/assurance-level-change";
    public static final String CAEP_DEVICE_COMPLIANCE_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/device-compliance-change";
    public static final String CAEP_RISK_LEVEL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/risk-level-change";

    // --- RISC Events ---
    public static final String RISC_ACCOUNT_DISABLED = "https://schemas.openid.net/secevent/risc/event-type/account-disabled";
    public static final String RISC_ACCOUNT_ENABLED = "https://schemas.openid.net/secevent/risc/event-type/account-enabled";
    public static final String RISC_ACCOUNT_PURGED = "https://schemas.openid.net/secevent/risc/event-type/account-purged";
    public static final String RISC_CREDENTIAL_COMPROMISE = "https://schemas.openid.net/secevent/risc/event-type/credential-compromise";
    public static final String RISC_ACCOUNT_CREDENTIAL_CHANGE_REQUIRED = "https://schemas.openid.net/secevent/risc/event-type/account-credential-change-required";
    public static final String RISC_IDENTIFIER_CHANGED = "https://schemas.openid.net/secevent/risc/event-type/identifier-changed";
    public static final String RISC_IDENTIFIER_RECYCLED = "https://schemas.openid.net/secevent/risc/event-type/identifier-recycled";

    // RISC Opt-Out Variants
    public static final String RISC_OPT_IN = "https://schemas.openid.net/secevent/risc/event-type/opt-in";
    public static final String RISC_OPT_OUT_INITIATED = "https://schemas.openid.net/secevent/risc/event-type/opt-out-initiated";
    public static final String RISC_OPT_OUT_CANCELLED = "https://schemas.openid.net/secevent/risc/event-type/opt-out-cancelled";
    public static final String RISC_OPT_OUT_EFFECTIVE = "https://schemas.openid.net/secevent/risc/event-type/opt-out-effective";
    public static final String RISC_RECOVERY_ACTIVATED = "https://schemas.openid.net/secevent/risc/event-type/recovery-activated";
    public static final String RISC_RECOVERY_INFORMATION_CHANGED = "https://schemas.openid.net/secevent/risc/event-type/recovery-information-changed";
    public static final String RISC_VERIFICATION = "https://schemas.openid.net/secevent/risc/event-type/verification";

    // --- SSF Events ---
    public static final String SSF_VERIFICATION = "https://schemas.openid.net/secevent/ssf/event-type/verification";
    public static final String SSF_STREAM_UPDATED = "https://schemas.openid.net/secevent/ssf/event-type/stream-updated";

    // --- Subject Formats ---
    public static final String FORMAT_EMAIL = "email";
    public static final String FORMAT_ISSUER_SUBJECT = "iss_sub";
    public static final String FORMAT_OPAQUE = "opaque";
    public static final String FORMAT_PHONE = "phone_number";
    public static final String FORMAT_ACCOUNT = "account";
    public static final String FORMAT_DID = "did";
    public static final String FORMAT_JWT_ID = "jwt_id";
    public static final String FORMAT_SAML_ASSERTION_ID = "saml_assertion_id";
    public static final String FORMAT_URI = "uri";
    public static final String FORMAT_IP = "ip";
    public static final String FORMAT_COMPLEX = "complex";
    public static final String FORMAT_ALIASES = "aliases";

    // FULL LIST OF SUPPORTED EVENTS
    public static final List<String> SUPPORTED_EVENTS = List.of(
        CAEP_SESSION_REVOKED,
        CAEP_SESSION_ESTABLISHED,
        CAEP_SESSION_PRESENTED,
        CAEP_CREDENTIAL_CHANGE,
        CAEP_TOKEN_CLAIMS_CHANGE,
        CAEP_ASSURANCE_LEVEL_CHANGE,
        CAEP_DEVICE_COMPLIANCE_CHANGE,
        CAEP_RISK_LEVEL_CHANGE,

        RISC_ACCOUNT_DISABLED,
        RISC_ACCOUNT_ENABLED,
        RISC_ACCOUNT_PURGED,
        RISC_CREDENTIAL_COMPROMISE,
        RISC_ACCOUNT_CREDENTIAL_CHANGE_REQUIRED,
        RISC_IDENTIFIER_CHANGED,
        RISC_IDENTIFIER_RECYCLED,
        RISC_OPT_IN,
        RISC_OPT_OUT_INITIATED,
        RISC_OPT_OUT_CANCELLED,
        RISC_OPT_OUT_EFFECTIVE,
        RISC_RECOVERY_ACTIVATED,
        RISC_RECOVERY_INFORMATION_CHANGED,
        RISC_VERIFICATION,

        SSF_VERIFICATION,
        SSF_STREAM_UPDATED
    );
}