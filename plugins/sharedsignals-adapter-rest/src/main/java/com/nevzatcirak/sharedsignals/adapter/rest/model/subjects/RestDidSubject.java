package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DID Subject")
public record RestDidSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_DID) String format, @NotBlank String url) implements RestSubject {}
