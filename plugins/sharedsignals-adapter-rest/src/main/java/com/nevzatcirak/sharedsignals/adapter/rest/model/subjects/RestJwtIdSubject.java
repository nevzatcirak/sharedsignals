package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "JWT ID Subject")
public record RestJwtIdSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_JWT_ID) String format, @NotBlank String iss, @NotBlank String jti) implements RestSubject {}
