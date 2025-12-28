package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Account Subject")
public record RestAccountSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ACCOUNT) String format, @NotBlank String uri) implements RestSubject {}