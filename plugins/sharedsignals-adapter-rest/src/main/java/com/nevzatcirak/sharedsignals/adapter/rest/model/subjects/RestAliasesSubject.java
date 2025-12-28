package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Aliases Subject")
public record RestAliasesSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_ALIASES) String format, List<Object> identifiers) implements RestSubject {}
