package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Phone Subject")
public record RestPhoneSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_PHONE) String format, @NotBlank String phone_number) implements RestSubject {}
