package com.nevzatcirak.sharedsignals.web.controller;

import com.nevzatcirak.sharedsignals.api.model.TransmitterMetadata;
import com.nevzatcirak.sharedsignals.api.service.JwkSetService;
import com.nevzatcirak.sharedsignals.api.service.TransmitterMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.Collections;

/**
 * Controller serving standard discovery endpoints.
 * Compliant with SSF Section 7.2.
 */
@RestController
@RequestMapping("/.well-known")
@Tag(name = "Discovery", description = "Public endpoints for Transmitter Metadata and Keys. SSF Spec Section 7.")
public class WellKnownController {

    private final TransmitterMetadataService metadataService;
    private final JwkSetService jwkSetService;

    public WellKnownController(TransmitterMetadataService metadataService, JwkSetService jwkSetService) {
        this.metadataService = metadataService;
        this.jwkSetService = jwkSetService;
    }

    /**
     * SSF Configuration Endpoint.
     * Mapped with wildcards (/**) to support Issuer URLs with path components.
     * See SSF Figure 17: Request to ".../ssf-configuration/issuer1"
     */
    @GetMapping(
        value = {"/ssf-configuration", "/ssf-configuration/**"},
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "SSF Configuration", description = "Returns the Transmitter Configuration Metadata (SSF 7.1).")
    public ResponseEntity<TransmitterMetadata> getSsfConfiguration() {
        return ResponseEntity.ok(metadataService.getMetadata());
    }

    /**
     * RISC Backward Compatibility Endpoint.
     * Mapped with wildcards (/**) for consistency.
     */
    @GetMapping(
        value = {"/risc-configuration", "/risc-configuration/**"},
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "RISC Configuration", description = "Backward compatibility endpoint for RISC metadata.")
    public ResponseEntity<TransmitterMetadata> getRiscConfiguration() {
        return ResponseEntity.ok(metadataService.getMetadata());
    }

    @GetMapping(value = "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "JSON Web Key Set", description = "Returns the public keys used to verify SET signatures.")
    public ResponseEntity<Map<String, Object>> getJwkSet() {
        Map<String, Object> keys = jwkSetService.getJwkSet();
        return ResponseEntity.ok(Collections.singletonMap("keys", Collections.singletonList(keys)));
    }
}
