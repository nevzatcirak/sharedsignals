package com.nevzatcirak.sharedsignals.api.model;

/**
 * Model representing the delivery method configuration (e.g., Push/Poll).
 */
public class StreamDelivery {
    private String method;
    private String endpoint_url;
    private String authorization_header;
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEndpoint_url() { return endpoint_url; }
    public void setEndpoint_url(String endpoint_url) { this.endpoint_url = endpoint_url; }
    public String getAuthorization_header() { return authorization_header; }
    public void setAuthorization_header(String authorization_header) { this.authorization_header = authorization_header; }
}
