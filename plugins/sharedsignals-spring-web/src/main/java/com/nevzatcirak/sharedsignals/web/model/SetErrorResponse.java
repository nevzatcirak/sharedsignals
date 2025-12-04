package com.nevzatcirak.sharedsignals.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for RFC 8935 Error Response.
 * Contains IANA "Security Event Token Error Codes".
 */
public class SetErrorResponse {

    @JsonProperty("err")
    private String err;

    @JsonProperty("description")
    private String description;

    public String getErr() { return err; }
    public void setErr(String err) { this.err = err; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
