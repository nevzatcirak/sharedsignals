package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "SAML Assertion ID Subject")
public record RestSamlAssertionIdSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_SAML_ASSERTION_ID) String format, @NotBlank String iss, @NotBlank String saml_assertion_id) implements RestSubject {}
