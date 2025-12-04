package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Web Layer Model for Poll Response.
 */
public class SSFPollResponse {

    @JsonProperty("sets")
    private Map<String, String> sets;

    @JsonProperty("moreAvailable")
    private boolean moreAvailable;

    public SSFPollResponse(Map<String, String> sets, boolean moreAvailable) {
        this.sets = sets;
        this.moreAvailable = moreAvailable;
    }

    public Map<String, String> getSets() { return sets; }
    public boolean isMoreAvailable() { return moreAvailable; }
}
