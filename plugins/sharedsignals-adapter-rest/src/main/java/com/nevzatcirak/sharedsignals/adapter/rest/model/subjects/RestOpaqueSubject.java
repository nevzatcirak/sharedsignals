package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Opaque Subject")
public record RestOpaqueSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_OPAQUE) String format, @NotBlank String id) implements RestSubject {}
