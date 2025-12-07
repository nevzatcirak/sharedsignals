package com.nevzatcirak.sharedsignals.adapter.rest.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "format", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RestEmailSubject.class, name = "email"),
    @JsonSubTypes.Type(value = RestPhoneSubject.class, name = "phone_number"),
    @JsonSubTypes.Type(value = RestOpaqueSubject.class, name = "opaque"),
    @JsonSubTypes.Type(value = RestIssSubSubject.class, name = "iss_sub")
})
@Schema(description = "Polymorphic Subject")
public sealed interface RestSubject permits RestEmailSubject, RestPhoneSubject, RestOpaqueSubject, RestIssSubSubject {
    String format();
}

@Schema(description = "Email Subject")
record RestEmailSubject(@Schema(allowableValues = "email") String format, @NotBlank @Email String email) implements RestSubject {}

@Schema(description = "Phone Subject")
record RestPhoneSubject(@Schema(allowableValues = "phone_number") String format, @NotBlank String phone_number) implements RestSubject {}

@Schema(description = "Opaque Subject")
record RestOpaqueSubject(@Schema(allowableValues = "opaque") String format, @NotBlank String id) implements RestSubject {}

@Schema(description = "Issuer Subject")
record RestIssSubSubject(@Schema(allowableValues = "iss_sub") String format, @NotBlank String iss, @NotBlank String sub) implements RestSubject {}
