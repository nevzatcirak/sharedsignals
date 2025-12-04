package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Web Layer Model for Poll Request.
 * Supports 'ack' and 'setErrs' per RFC 8936.
 */
public class SSFPollRequest {

    @JsonProperty("ack")
    private List<String> ack;

    @JsonProperty("setErrs")
    private Map<String, SSFPollSetError> setErrs;

    @JsonProperty("maxEvents")
    private Integer maxEvents;

    @JsonProperty("returnImmediately")
    private Boolean returnImmediately;

    public List<String> getAck() { return ack; }
    public void setAck(List<String> ack) { this.ack = ack; }

    public Map<String, SSFPollSetError> getSetErrs() { return setErrs; }
    public void setSetErrs(Map<String, SSFPollSetError> setErrs) { this.setErrs = setErrs; }

    public Integer getMaxEvents() { return maxEvents; }
    public void setMaxEvents(Integer maxEvents) { this.maxEvents = maxEvents; }

    public Boolean getReturnImmediately() { return returnImmediately; }
    public void setReturnImmediately(Boolean returnImmediately) { this.returnImmediately = returnImmediately; }

    public static class SSFPollSetError {
        @JsonProperty("err")
        private String err;

        @JsonProperty("description")
        private String description;

        public String getErr() { return err; }
        public void setErr(String err) { this.err = err; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
