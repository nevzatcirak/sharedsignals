package com.nevzatcirak.sharedsignals.adapter.rest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.adapter.rest.model.*;
import com.nevzatcirak.sharedsignals.api.enums.SecurityIntent;
import com.nevzatcirak.sharedsignals.api.model.GenericSecurityEvent;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RestRequestMapper {
    private final ObjectMapper objectMapper;

    public RestRequestMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GenericSecurityEvent toDomain(RestIngestRequest request) {
        SecurityIntent intent = SecurityIntent.valueOf(request.data().intent());
        Map<String, Object> subjectMap = objectMapper.convertValue(request.subject(), Map.class);

        Map<String, Object> payloadMap = objectMapper.convertValue(request.data(), Map.class);
        payloadMap.remove("intent");
        return new GenericSecurityEvent(intent, subjectMap, payloadMap, request.occurrenceTime());
    }
}
