package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email Subject")
public record RestEmailSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_EMAIL) String format, @NotBlank @Email String email) implements RestSubject {}
