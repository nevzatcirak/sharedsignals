package com.nevzatcirak.sharedsignals.api.constant;

import java.util.List;

/**
 * Constants for OpenID Shared Signals Framework (SSF), CAEP, and RISC specifications.
 * Defines standard URNs for Event Types, Subject Formats, Delivery Methods, and Protocols.
 * Moved to API module to be accessible by all layers.
 */
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

    public static final List<String> SUPPORTED_EVENTS = List.of(
            SharedSignalConstants.CAEP_SESSION_REVOKED,
            SharedSignalConstants.CAEP_CREDENTIAL_CHANGE,
            SharedSignalConstants.CAEP_TOKEN_CLAIMS_CHANGE,
            SharedSignalConstants.CAEP_ASSURANCE_LEVEL_CHANGE,
            SharedSignalConstants.CAEP_DEVICE_COMPLIANCE_CHANGE,
            SharedSignalConstants.RISC_ACCOUNT_DISABLED,
            SharedSignalConstants.RISC_ACCOUNT_ENABLED,
            SharedSignalConstants.RISC_ACCOUNT_PURGED,
            SharedSignalConstants.RISC_CREDENTIAL_COMPROMISE,
            SharedSignalConstants.RISC_IDENTIFIER_CHANGED,
            SharedSignalConstants.RISC_OPT_OUT,
            SharedSignalConstants.RISC_VERIFICATION
    );

    // --- CAEP Events ---
    public static final String CAEP_SESSION_REVOKED = "https://schemas.openid.net/secevent/caep/event-type/session-revoked";
    public static final String CAEP_CREDENTIAL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/credential-change";
    public static final String CAEP_TOKEN_CLAIMS_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/token-claims-change";
    public static final String CAEP_ASSURANCE_LEVEL_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/assurance-level-change";
    public static final String CAEP_DEVICE_COMPLIANCE_CHANGE = "https://schemas.openid.net/secevent/caep/event-type/device-compliance-change";

    // --- RISC Events ---
    public static final String RISC_ACCOUNT_DISABLED = "https://schemas.openid.net/secevent/risc/event-type/account-disabled";
    public static final String RISC_ACCOUNT_ENABLED = "https://schemas.openid.net/secevent/risc/event-type/account-enabled";
    public static final String RISC_ACCOUNT_PURGED = "https://schemas.openid.net/secevent/risc/event-type/account-purged";
    public static final String RISC_CREDENTIAL_COMPROMISE = "https://schemas.openid.net/secevent/risc/event-type/credential-compromise";
    public static final String RISC_IDENTIFIER_CHANGED = "https://schemas.openid.net/secevent/risc/event-type/identifier-changed";
    public static final String RISC_OPT_OUT = "https://schemas.openid.net/secevent/risc/event-type/opt-out";
    public static final String RISC_VERIFICATION = "https://schemas.openid.net/secevent/risc/event-type/verification";

    // SSF Event Types
    public static final String SSF_VERIFICATION = "https://schemas.openid.net/secevent/ssf/event-type/verification";
    public static final String SSF_STREAM_UPDATED = "https://schemas.openid.net/secevent/ssf/event-type/stream-updated";

    // --- Subject Formats ---
    public static final String FORMAT_EMAIL = "email";
    public static final String FORMAT_ISSUER_SUBJECT = "iss_sub";
    public static final String FORMAT_OPAQUE = "opaque";
    public static final String FORMAT_PHONE = "phone_number";
    public static final String FORMAT_ACCOUNT = "account";
    public static final String FORMAT_DID = "did";
    public static final String FORMAT_URI = "uri";
    public static final String FORMAT_IP = "ip";
    public static final String FORMAT_COMPLEX = "complex";
    public static final String FORMAT_ALIASES = "aliases";
}
