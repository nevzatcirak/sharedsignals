package com.nevzatcirak.sharedsignals.adapter.rest.model.subjects;

import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Complex Subject")
public record RestComplexSubject(@Schema(allowableValues = SharedSignalConstants.FORMAT_COMPLEX) String format, Map<String, Object> user, Map<String, Object> device, Map<String, Object> session, Map<String, Object> tenant) implements RestSubject {}