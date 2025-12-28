package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "IP Address Subject")
public record RestIpSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_IP) String format, @NotBlank String ip) implements RestSubject {}
