package com.nevzatcirak.sharedsignals.adapter.rest.controller;

import com.nevzatcirak.sharedsignals.adapter.rest.mapper.RestRequestMapper;
import com.nevzatcirak.sharedsignals.adapter.rest.model.RestIngestRequest;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import com.nevzatcirak.sharedsignals.api.service.EventIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/ingest")
@Tag(name = "Event Ingestion", description = "Feeds security events into the transmitter")
public class IngestionController {

    private final EventIngestionService ingestionService;
    private final RestRequestMapper mapper;

    public IngestionController(EventIngestionService ingestionService, RestRequestMapper mapper) {
        this.ingestionService = ingestionService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Ingest Event", description = "Validates and processes a security event.")
    public CompletableFuture<ResponseEntity<Void>> ingestEvent(@Valid @RequestBody RestIngestRequest request) {
        GenericSecurityEvent event = mapper.toDomain(request);
        return ingestionService.ingest(event)
                .thenApply(count -> ResponseEntity.accepted().build());
    }
}
