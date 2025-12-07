package com.nevzatcirak.sharedsignals.adapter.rest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.adapter.rest.model.*;
import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import com.nevzatcirak.sharedsignals.api.exception.SsfBadRequestException;
import com.nevzatcirak.sharedsignals.api.exception.SsfErrorCode;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Mapper responsible for converting REST Adapter DTOs into Core Domain Models.
 * <p>
 * This class acts as an <b>Anti-Corruption Layer</b>, preventing Jackson annotations,
 * validation logic, and web-specific concerns from leaking into the Core Domain.
 * </p>
 */
@Component
public class RestRequestMapper {
    private final ObjectMapper objectMapper;

    public RestRequestMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a validated REST Ingestion Request into a framework-agnostic Domain Event.
     *
     * @param request The validated incoming REST request.
     * @return A {@link GenericSecurityEvent} ready for processing by the Core layer.
     */
    public GenericSecurityEvent toDomain(RestIngestRequest request) {
        SecurityIntent intent;
        try {
            intent = SecurityIntent.valueOf(request.data().intent());
        } catch (IllegalArgumentException e) {
            // Convert generic Java exception to Domain Specific Exception (HTTP 400)
            throw new SsfBadRequestException(SsfErrorCode.MALFORMED_REQUEST, "Invalid Security Intent: " + request.data().intent());
        }

        Map<String, Object> subjectMap = objectMapper.convertValue(request.subject(), Map.class);

        // Convert the Payload Record to a Map
        Map<String, Object> payloadMap = objectMapper.convertValue(request.data(), Map.class);

        // Remove the discriminator field as it is redundant in the internal map
        payloadMap.remove("intent");

        return new GenericSecurityEvent(intent, subjectMap, payloadMap, request.occurrenceTime());
    }
}