package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating stream processing mode")
public class StreamModeRequest {

    @Schema(description = "If true, the stream receives ALL events from the transmitter regardless of subject registration.", example = "true")
    @JsonProperty("process_all_subjects")
    private boolean processAllSubjects;

    public boolean isProcessAllSubjects() {
        return processAllSubjects;
    }

    public void setProcessAllSubjects(boolean processAllSubjects) {
        this.processAllSubjects = processAllSubjects;
    }
}
