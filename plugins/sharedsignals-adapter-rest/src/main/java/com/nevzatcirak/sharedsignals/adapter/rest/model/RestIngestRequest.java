package com.nevzatcirak.sharedsignals.adapter.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(description = "Ingestion Request Body")
public record RestIngestRequest(
    @NotNull @Valid RestSubject subject,
    @NotNull @Valid RestEventPayload data,
    @Schema(description = "Optional occurrence time") Instant occurrenceTime
) {}
