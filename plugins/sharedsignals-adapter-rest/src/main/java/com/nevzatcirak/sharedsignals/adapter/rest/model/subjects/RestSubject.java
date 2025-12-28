package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;

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
