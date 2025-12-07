package com.nevzatcirak.sharedsignals.adapter.rest.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "format", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RestEmailSubject.class, name = SharedSignalConstants.FORMAT_EMAIL),
    @JsonSubTypes.Type(value = RestPhoneSubject.class, name = SharedSignalConstants.FORMAT_PHONE),
    @JsonSubTypes.Type(value = RestIssSubSubject.class, name = SharedSignalConstants.FORMAT_ISSUER_SUBJECT),
    @JsonSubTypes.Type(value = RestOpaqueSubject.class, name = SharedSignalConstants.FORMAT_OPAQUE),
    @JsonSubTypes.Type(value = RestAccountSubject.class, name = SharedSignalConstants.FORMAT_ACCOUNT),
    @JsonSubTypes.Type(value = RestDidSubject.class, name = SharedSignalConstants.FORMAT_DID),
    @JsonSubTypes.Type(value = RestUriSubject.class, name = SharedSignalConstants.FORMAT_URI),
    @JsonSubTypes.Type(value = RestIpSubject.class, name = SharedSignalConstants.FORMAT_IP),
    @JsonSubTypes.Type(value = RestJwtIdSubject.class, name = SharedSignalConstants.FORMAT_JWT_ID),
    @JsonSubTypes.Type(value = RestSamlAssertionIdSubject.class, name = SharedSignalConstants.FORMAT_SAML_ASSERTION_ID),
    @JsonSubTypes.Type(value = RestComplexSubject.class, name = SharedSignalConstants.FORMAT_COMPLEX),
    @JsonSubTypes.Type(value = RestAliasesSubject.class, name = SharedSignalConstants.FORMAT_ALIASES)
})
@Schema(description = "Polymorphic Subject Definition")
public sealed interface RestSubject permits
    RestEmailSubject, RestPhoneSubject, RestIssSubSubject, RestOpaqueSubject,
    RestAccountSubject, RestDidSubject, RestUriSubject, RestIpSubject,
    RestJwtIdSubject, RestSamlAssertionIdSubject, RestComplexSubject, RestAliasesSubject {
    String format();
}

@Schema(description = "Email Subject")
record RestEmailSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_EMAIL) String format, @NotBlank @Email String email) implements RestSubject {}

@Schema(description = "Phone Subject")
record RestPhoneSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_PHONE) String format, @NotBlank String phone_number) implements RestSubject {}

@Schema(description = "Issuer and Subject")
record RestIssSubSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ISSUER_SUBJECT) String format, @NotBlank String iss, @NotBlank String sub) implements RestSubject {}

@Schema(description = "Opaque Subject")
record RestOpaqueSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_OPAQUE) String format, @NotBlank String id) implements RestSubject {}

@Schema(description = "Account Subject")
record RestAccountSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ACCOUNT) String format, @NotBlank String uri) implements RestSubject {}

@Schema(description = "DID Subject")
record RestDidSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_DID) String format, @NotBlank String url) implements RestSubject {}

@Schema(description = "URI Subject")
record RestUriSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_URI) String format, @NotBlank String uri) implements RestSubject {}

@Schema(description = "IP Address Subject")
record RestIpSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_IP) String format, @NotBlank String ip) implements RestSubject {}

@Schema(description = "JWT ID Subject")
record RestJwtIdSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_JWT_ID) String format, @NotBlank String iss, @NotBlank String jti) implements RestSubject {}

@Schema(description = "SAML Assertion ID Subject")
record RestSamlAssertionIdSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_SAML_ASSERTION_ID) String format, @NotBlank String iss, @NotBlank String saml_assertion_id) implements RestSubject {}

@Schema(description = "Complex Subject")
record RestComplexSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_COMPLEX) String format, Map<String, Object> user, Map<String, Object> device, Map<String, Object> session, Map<String, Object> tenant) implements RestSubject {}

@Schema(description = "Aliases Subject")
record RestAliasesSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ALIASES) String format, List<Object> identifiers) implements RestSubject {}
