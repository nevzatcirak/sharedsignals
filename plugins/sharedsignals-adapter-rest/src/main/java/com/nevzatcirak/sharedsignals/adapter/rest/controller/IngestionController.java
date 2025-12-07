package com.nevzatcirak.sharedsignals.adapter.rest.controller;

import com.nevzatcirak.sharedsignals.adapter.rest.mapper.RestRequestMapper;
import com.nevzatcirak.sharedsignals.adapter.rest.model.RestIngestRequest;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import com.nevzatcirak.sharedsignals.api.service.EventIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

/**
 * REST Endpoint for ingesting security events from external systems.
 * <p>
 * This API is designed for Identity Providers (IdPs), Risk Engines, and other security
 * systems to feed standard CAEP/RISC events into the Shared Signals Transmitter.
 */
@RestController
@RequestMapping("/api/v1/ingest")
@Tag(name = "Event Ingestion", description = "API for triggering Security Events (RISC/CAEP) from external sources.")
public class IngestionController {

    private final EventIngestionService ingestionService;
    private final RestRequestMapper mapper;

    public IngestionController(EventIngestionService ingestionService, RestRequestMapper mapper) {
        this.ingestionService = ingestionService;
        this.mapper = mapper;
    }

    /**
     * Ingests a Security Event asynchronously.
     * <p>
     * <b>Processing Logic:</b>
     * <ol>
     * <li><b>Validation:</b> The request payload is strictly validated against the SSF schemas.</li>
     * <li><b>Mapping:</b> The generic REST model is converted to a domain event.</li>
     * <li><b>Queuing:</b> The event is persisted to the database (Outbox Pattern) within a transaction.</li>
     * <li><b>Delivery:</b> A background worker picks up the event for Push delivery or it becomes available for Polling.</li>
     * </ol>
     * * @param request The structured event payload containing the Subject and Event Data.
     * @return 202 Accepted if the event was successfully queued.
     */
    @PostMapping
    @Operation(
        summary = "Trigger a Security Event",
        description = "Receives a RISC or CAEP event, validates it, and queues it for broadcast to subscribers. This operation is non-blocking.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Accepted. The event has been validated and queued for processing.",
            content = @Content(schema = @Schema(implementation = Void.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request. Validation failed (e.g., missing required fields, invalid enum values). See 'invalid_params' in response.",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Invalid or missing authentication token.",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests. Rate limit exceeded for the client or IP.",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error. An unexpected error occurred during processing.",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public CompletableFuture<ResponseEntity<Void>> ingestEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "The security event payload. Requires a valid Subject and Event Data matching the 'intent'.",
                required = true
            )
            @Valid @RequestBody RestIngestRequest request) {
        GenericSecurityEvent event = mapper.toDomain(request);
        return ingestionService.ingest(event)
                .thenApply(count -> ResponseEntity.accepted().build());
    }
}