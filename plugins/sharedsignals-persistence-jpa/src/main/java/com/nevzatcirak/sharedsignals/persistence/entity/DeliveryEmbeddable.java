package com.nevzatcirak.sharedsignals.persistence.entity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/**
 * JPA Embeddable for Delivery Configuration.
 */
@Embeddable
public class DeliveryEmbeddable {
    @Column(name = "delivery_method", nullable = false)
    private String method;
    @Column(name = "endpoint_url")
    private String endpointUrl;
    @Column(name = "authorization_header", length = 2048)
    private String authorizationHeader;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getAuthorizationHeader() { return authorizationHeader; }
    public void setAuthorizationHeader(String authorizationHeader) { this.authorizationHeader = authorizationHeader; }
}
