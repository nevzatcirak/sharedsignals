package com.nevzatcirak.sharedsignals.adapter.rest.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "intent", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RestAccountDisabledData.class, name = "ACCOUNT_DISABLED"),
        @JsonSubTypes.Type(value = RestAccountEnabledData.class, name = "ACCOUNT_ENABLED"),
        @JsonSubTypes.Type(value = RestAccountPurgedData.class, name = "ACCOUNT_PURGED"),
        @JsonSubTypes.Type(value = RestCredentialCompromiseData.class, name = "CREDENTIAL_COMPROMISE"),
        @JsonSubTypes.Type(value = RestAccountCredentialChangeRequiredData.class, name = "ACCOUNT_CREDENTIAL_CHANGE_REQUIRED"),
        @JsonSubTypes.Type(value = RestIdentifierChangedData.class, name = "IDENTIFIER_CHANGED"),
        @JsonSubTypes.Type(value = RestIdentifierRecycledData.class, name = "IDENTIFIER_RECYCLED"),
        @JsonSubTypes.Type(value = RestOptOutData.class, name = "OPT_OUT"),
        @JsonSubTypes.Type(value = RestRecoveryActivatedData.class, name = "RECOVERY_ACTIVATED"),
        @JsonSubTypes.Type(value = RestRecoveryInfoChangedData.class, name = "RECOVERY_INFORMATION_CHANGED"),

        @JsonSubTypes.Type(value = RestSessionRevokedData.class, name = "SESSION_REVOKED"),
        @JsonSubTypes.Type(value = RestSessionEstablishedData.class, name = "SESSION_ESTABLISHED"),
        @JsonSubTypes.Type(value = RestSessionPresentedData.class, name = "SESSION_PRESENTED"),
        @JsonSubTypes.Type(value = RestCredentialChangeData.class, name = "CREDENTIAL_CHANGE"),
        @JsonSubTypes.Type(value = RestTokenClaimsChangeData.class, name = "TOKEN_CLAIMS_CHANGE"),
        @JsonSubTypes.Type(value = RestAssuranceLevelChangeData.class, name = "ASSURANCE_LEVEL_CHANGE"),
        @JsonSubTypes.Type(value = RestDeviceComplianceChangeData.class, name = "DEVICE_COMPLIANCE_CHANGE"),
        @JsonSubTypes.Type(value = RestRiskLevelChangeData.class, name = "RISK_LEVEL_CHANGE"),

        @JsonSubTypes.Type(value = RestVerificationData.class, name = "VERIFICATION"),
        @JsonSubTypes.Type(value = RestGenericEventData.class, name = "GENERIC")
})
@Schema(description = "Polymorphic Event Payload. The structure changes based on the 'intent' field.")
public sealed interface RestEventPayload permits
        RestAccountDisabledData, RestAccountEnabledData, RestAccountPurgedData, RestCredentialCompromiseData,
        RestAccountCredentialChangeRequiredData, RestIdentifierChangedData, RestIdentifierRecycledData, RestOptOutData,
        RestRecoveryActivatedData, RestRecoveryInfoChangedData,
        RestSessionRevokedData, RestSessionEstablishedData, RestSessionPresentedData,
        RestCredentialChangeData, RestTokenClaimsChangeData,
        RestAssuranceLevelChangeData, RestDeviceComplianceChangeData, RestRiskLevelChangeData,
        RestVerificationData, RestGenericEventData {

    String intent();
}

// --- RISC ---
@Schema(description = "RISC: Signals that the account was disabled.")
record RestAccountDisabledData(
        @Schema(allowableValues = "ACCOUNT_DISABLED") @NotNull String intent,
        @Schema(
                description = "OPTIONAL. The reason the account was disabled. Recommended values: hijacking, bulk-account, policy-violation.",
                example = "hijacking"
        )
        String reason
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that the account was enabled.")
record RestAccountEnabledData(
        @Schema(allowableValues = "ACCOUNT_ENABLED") @NotNull String intent
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that the account was permanently deleted.")
record RestAccountPurgedData(
        @Schema(allowableValues = "ACCOUNT_PURGED") @NotNull String intent
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that credentials were compromised.")
record RestCredentialCompromiseData(
        @Schema(allowableValues = "CREDENTIAL_COMPROMISE") @NotNull String intent,

        @Schema(
                description = "REQUIRED. The type of credential compromised.",
                example = "password",
                allowableValues = {"password", "pin", "x509", "fido2-platform", "fido2-roaming", "fido-u2f", "verifiable-credential", "phone-voice", "phone-sms", "app"}
        )
        @NotBlank @Pattern(regexp = "^(password|pin|x509|fido2-platform|fido2-roaming|fido-u2f|verifiable-credential|phone-voice|phone-sms|app)$")
        String credential_type,

        @Schema(description = "OPTIONAL. Reason for admin.", example = "Credential found in public database.")
        String reason_admin,

        @Schema(description = "OPTIONAL. Reason for user.", example = "Your password appeared in a data breach.")
        String reason_user,

        @Schema(description = "OPTIONAL. Timestamp of discovery (seconds since epoch).", example = "1717152000")
        Long event_timestamp
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that the user must change their credential.")
record RestAccountCredentialChangeRequiredData(
        @Schema(allowableValues = "ACCOUNT_CREDENTIAL_CHANGE_REQUIRED") @NotNull String intent
) implements RestEventPayload {
}

@Schema(description = "RISC: Identifier Changed signals that the identifier specified in the subject has changed. The subject type MUST be either email or phone and it MUST specify the old value.\n" +
        "\n" +
        "This event SHOULD be issued only by the provider that is authoritative over the identifier. For example, if the person that owns john.doe@example.com goes through a name change and wants the new john.roe@example.com email then only the email provider example.com SHOULD issue an Identifier Changed event as shown in the example below.")
record RestIdentifierChangedData(
        @Schema(allowableValues = "IDENTIFIER_CHANGED") @NotNull String intent,
        @Schema(description = "OPTIONAL. The new value of the identifier.", example = "new-email@example.com")
        String new_value
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that an identifier was recycled (assigned to a new user).")
record RestIdentifierRecycledData(
        @Schema(allowableValues = "IDENTIFIER_RECYCLED") @NotNull String intent
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals user opt-in/opt-out status.")
record RestOptOutData(
        @Schema(allowableValues = "OPT_OUT") @NotNull String intent,

        @Schema(
                description = "REQUIRED. The opt-out state.",
                example = "opt-out-initiated",
                allowableValues = {"opt-in", "opt-out-initiated", "opt-out-cancelled", "opt-out-effective"}
        )
        @NotBlank @Pattern(regexp = "^(opt-in|opt-out-initiated|opt-out-cancelled|opt-out-effective)$")
        String state
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that account recovery flow started.")
record RestRecoveryActivatedData(
        @Schema(allowableValues = "RECOVERY_ACTIVATED") @NotNull String intent
) implements RestEventPayload {
}

@Schema(description = "RISC: Signals that recovery info changed.")
record RestRecoveryInfoChangedData(
        @Schema(allowableValues = "RECOVERY_INFORMATION_CHANGED") @NotNull String intent
) implements RestEventPayload {
}

// --- CAEP ---
@Schema(description = "CAEP: Signals that a session was revoked.")
record RestSessionRevokedData(
        @Schema(allowableValues = "SESSION_REVOKED") @NotNull String intent,
        @Schema(description = "REQUIRED. Event timestamp (epoch seconds).", example = "1717152000") @NotNull Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "admin", allowableValues = {"admin", "user", "policy", "system"}) String initiating_entity,
        @Schema(description = "OPTIONAL. Localized admin reason.", example = "{\"en\": \"Policy Violation\"}") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. Localized user reason.", example = "{\"en\": \"Session expired\"}") Map<String, String> reason_user
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a new session was established.")
record RestSessionEstablishedData(
        @Schema(allowableValues = "SESSION_ESTABLISHED") @NotNull String intent,
        @Schema(description = "REQUIRED. Timestamp.", example = "1717152000") @NotNull Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "user") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User reason.") Map<String, String> reason_user,
        @Schema(description = "OPTIONAL. Fingerprint of the user agent.", example = "abb0b6e7da81a42233f8f2b1a8ddb1b9a4c81611") String fp_ua,
        @Schema(description = "OPTIONAL. Authentication Context Class Reference (ACR).", example = "AAL2") String acr,
        @Schema(description = "OPTIONAL. Authentication Methods References (AMR).", example = "[\"pwd\", \"otp\"]") List<String> amr,
        @Schema(description = "OPTIONAL. External session identifier.", example = "sid-123456") String ext_id
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a session was presented/used.")
record RestSessionPresentedData(
        @Schema(allowableValues = "SESSION_PRESENTED") @NotNull String intent,
        @Schema(description = "REQUIRED. Timestamp.", example = "1717152000") @NotNull Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "system") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User reason.") Map<String, String> reason_user,
        @Schema(description = "OPTIONAL. Fingerprint of the user agent.", example = "a1b2c3d4") String fp_ua,
        @Schema(description = "OPTIONAL. External session identifier.", example = "sid-123456") String ext_id
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a change in assurance level (NIST AAL/IAL).")
record RestAssuranceLevelChangeData(
        @Schema(allowableValues = "ASSURANCE_LEVEL_CHANGE") @NotNull String intent,

        @Schema(
                description = "REQUIRED. Namespace. Custom values are allowed.",
                example = "The namespace of the values in the current_level and previous_level claims. This string MAY be one of the following strings: RFC8176, RFC6711, ISO-IEC-29115, NIST-IAL, NIST-AAL, NIST-FAL. Any other value that is an alias for a custom namespace agreed between the Transmitter and the Receiver"
        )
        @NotBlank String namespace,

        @Schema(
            description = """
                REQUIRED. The new assurance level value.
                Valid values depend on the namespace:
                * NIST-AAL: `nist-aal1`, `nist-aal2`, `nist-aal3`
                * NIST-IAL: `nist-ial1`, `nist-ial2`, `nist-ial3`
                """,
            example = "nist-aal2"
        )
        @NotBlank String current_level,

        @Schema(description = "OPTIONAL. The previous assurance level.", example = "nist-aal1") String previous_level,
        @Schema(description = "OPTIONAL. Change Direction.", example = "increase", allowableValues = {"increase", "decrease"})
        @Pattern(regexp = "^(increase|decrease)$") String change_direction,

        @Schema(description = "OPTIONAL. Event Timestamp.", example = "1717152000") Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "policy") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin Reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User Reason.") Map<String, String> reason_user
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a change in device compliance status.")
record RestDeviceComplianceChangeData(
        @Schema(allowableValues = "DEVICE_COMPLIANCE_CHANGE", example = "DEVICE_COMPLIANCE_CHANGE") @NotNull String intent,
        @Schema(description = "REQUIRED. Previous Status.", example = "compliant", allowableValues = {"compliant", "not-compliant"})
        @NotBlank @Pattern(regexp = "^(compliant|not-compliant)$") String previous_status,
        @Schema(description = "REQUIRED. Current Status.", example = "not-compliant", allowableValues = {"compliant", "not-compliant"})
        @NotBlank @Pattern(regexp = "^(compliant|not-compliant)$") String current_status,
        @Schema(description = "REQUIRED. Timestamp.", example = "1717152000") @NotNull Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "system") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin Reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User Reason.") Map<String, String> reason_user
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a change in risk level.")
record RestRiskLevelChangeData(
        @Schema(allowableValues = "RISK_LEVEL_CHANGE") @NotNull String intent,
        @Schema(description = "RECOMMENDED. Indicates the reason that contributed to the risk level changes by the Transmitter.", example = "PASSWORD_FOUND_IN_DATA_BREACH") String risk_reason,
        @Schema(description = "RECOMMENDED. Indicates the reason that contributed to the risk level changes by the Transmitter.",
                allowableValues = {"USER", "DEVICE", "SESSION", "TENANT", "ORG_UNIT", "GROUP"}, example = "USER")
        @Pattern(regexp = "^(USER|DEVICE|SESSION|TENANT|ORG_UNIT|GROUP)$") String principal,
        @Schema(description = "REQUIRED. Current risk level.", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"}) @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$") @NotBlank String current_level,
        @Schema(description = "OPTIONAL. Previous risk level.", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"}) @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$") String previous_level,

        @Schema(description = "OPTIONAL. Timestamp.", example = "1717152000") Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "policy") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin Reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User Reason.") Map<String, String> reason_user
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals a credential was created, updated, or revoked.")
record RestCredentialChangeData(
        @Schema(allowableValues = "CREDENTIAL_CHANGE") @NotNull String intent,

        @Schema(description = "REQUIRED. Credential Type.", example = "fido2-roaming", allowableValues = {"password", "pin", "x509", "fido2-platform", "fido2-roaming", "fido-u2f", "verifiable-credential", "phone-voice", "phone-sms", "app"})
        @NotBlank String credentialType,

        @Schema(description = "REQUIRED. Change Type.", example = "create", allowableValues = {"create", "revoke", "update", "delete"})
        @NotBlank @Pattern(regexp = "^(create|revoke|update|delete)$") String changeType,

        @Schema(description = "OPTIONAL. Friendly name.", example = "Jane's USB authenticator") String friendly_name,
        @Schema(description = "OPTIONAL. X509 Issuer.") String x509_issuer,
        @Schema(description = "OPTIONAL. X509 Serial.") String x509_serial,
        @Schema(description = "OPTIONAL. FIDO2 AAGUID.", example = "accced6a-63f5-490a-9eea-e59bc1896cfc") String fido2_aaguid,

        @Schema(description = "OPTIONAL. Timestamp.", example = "1615304991") Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "user") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin Reason.", example = "{\"en\": \"User self-enrollment\"}") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User Reason.") Map<String, String> reason_user
) implements RestEventPayload {
}

@Schema(description = "CAEP: Signals that claims in a token have changed.")
record RestTokenClaimsChangeData(
        @Schema(allowableValues = "TOKEN_CLAIMS_CHANGE") @NotNull String intent,

        @Schema(description = "REQUIRED. Map of changed claims.", example = "{\"role\": \"ro-admin\", \"group\": \"engineering\"}")
        @NotNull Map<String, Object> claims,

        @Schema(description = "REQUIRED. Timestamp.", example = "1717152000") @NotNull Long event_timestamp,
        @Schema(description = "OPTIONAL. Entity that invoked the event.", example = "admin") String initiating_entity,
        @Schema(description = "OPTIONAL. Admin Reason.") Map<String, String> reason_admin,
        @Schema(description = "OPTIONAL. User Reason.") Map<String, String> reason_user
) implements RestEventPayload {
}

// --- CONTROL ---
@Schema(description = "SSF: Verification Event")
record RestVerificationData(
        @Schema(allowableValues = "VERIFICATION") @NotNull String intent,
        @Schema(description = "OPTIONAL. State to echo back.", example = "test-state-123") String state
) implements RestEventPayload {
}

@Schema(description = "Fallback Generic Payload")
record RestGenericEventData(
        @Schema(allowableValues = "GENERIC") @NotNull String intent,
        @Schema(description = "Free form data map.") Map<String, Object> data
) implements RestEventPayload {
}