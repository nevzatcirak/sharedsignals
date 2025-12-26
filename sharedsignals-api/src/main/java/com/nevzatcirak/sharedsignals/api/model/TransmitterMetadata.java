package com.nevzatcirak.sharedsignals.api.model;
import java.util.List;

/**
 * Model representing the Transmitter Configuration Metadata (SSF/RISC/CAEP).
 * Conforms to SSF Section 7.1.
 */
public class TransmitterMetadata {
    // --- SSF 7.1 Fields ---
    private String spec_version;
    private String issuer;
    private String jwks_uri;
    private List<String> delivery_methods_supported;
    private String configuration_endpoint;
    private String status_endpoint;
    private String add_subject_endpoint;
    private String remove_subject_endpoint;
    private String verification_endpoint;
    private List<String> critical_subject_members;
    private List<AuthorizationScheme> authorization_schemes;
    private String default_subjects;
    private List<String> subject_types_supported; // CAEP requirement

    /**
     * Authorization Scheme definition (SSF 7.1.1).
     */
    public static class AuthorizationScheme {
        private String spec_urn;

        public AuthorizationScheme() {}
        public AuthorizationScheme(String spec_urn) {
            this.spec_urn = spec_urn;
        }
        public String getSpec_urn() { return spec_urn; }
        public void setSpec_urn(String spec_urn) { this.spec_urn = spec_urn; }
    }
    
    public String getSpec_version() { return spec_version; }
    public void setSpec_version(String spec_version) { this.spec_version = spec_version; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getJwks_uri() { return jwks_uri; }
    public void setJwks_uri(String jwks_uri) { this.jwks_uri = jwks_uri; }

    public List<String> getDelivery_methods_supported() { return delivery_methods_supported; }
    public void setDelivery_methods_supported(List<String> delivery_methods_supported) { this.delivery_methods_supported = delivery_methods_supported; }

    public String getConfiguration_endpoint() { return configuration_endpoint; }
    public void setConfiguration_endpoint(String configuration_endpoint) { this.configuration_endpoint = configuration_endpoint; }

    public String getStatus_endpoint() { return status_endpoint; }
    public void setStatus_endpoint(String status_endpoint) { this.status_endpoint = status_endpoint; }

    public String getAdd_subject_endpoint() { return add_subject_endpoint; }
    public void setAdd_subject_endpoint(String add_subject_endpoint) { this.add_subject_endpoint = add_subject_endpoint; }

    public String getRemove_subject_endpoint() { return remove_subject_endpoint; }
    public void setRemove_subject_endpoint(String remove_subject_endpoint) { this.remove_subject_endpoint = remove_subject_endpoint; }

    public String getVerification_endpoint() { return verification_endpoint; }
    public void setVerification_endpoint(String verification_endpoint) { this.verification_endpoint = verification_endpoint; }

    public List<String> getCritical_subject_members() { return critical_subject_members; }
    public void setCritical_subject_members(List<String> critical_subject_members) { this.critical_subject_members = critical_subject_members; }

    public List<AuthorizationScheme> getAuthorization_schemes() { return authorization_schemes; }
    public void setAuthorization_schemes(List<AuthorizationScheme> authorization_schemes) { this.authorization_schemes = authorization_schemes; }

    public String getDefault_subjects() { return default_subjects; }
    public void setDefault_subjects(String default_subjects) { this.default_subjects = default_subjects; }

    public List<String> getSubject_types_supported() { return subject_types_supported; }
    public void setSubject_types_supported(List<String> subject_types_supported) { this.subject_types_supported = subject_types_supported; }
}
