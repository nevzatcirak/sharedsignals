package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Issuer and Subject")
public record RestIssSubSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ISSUER_SUBJECT) String format, @NotBlank String iss, @NotBlank String sub) implements RestSubject {}
