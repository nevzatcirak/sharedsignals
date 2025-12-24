package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Request to update the list of authorized event types")
public class UpdateAuthorizedEventsRequest {

    @Schema(description = "List of event type URIs allowed for this stream", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("authorized_events")
    private Set<String> authorizedEvents;

    public Set<String> getAuthorizedEvents() { return authorizedEvents; }
    public void setAuthorizedEvents(Set<String> authorizedEvents) { this.authorizedEvents = authorizedEvents; }
}
