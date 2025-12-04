package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.TransmitterMetadata;
import com.nevzatcirak.sharedsignals.api.service.TransmitterMetadataService;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import java.util.List;

public class DefaultTransmitterMetadataService implements TransmitterMetadataService {

    private final String issuer;

    public DefaultTransmitterMetadataService(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public TransmitterMetadata getMetadata() {
        TransmitterMetadata md = new TransmitterMetadata();
        md.setSpec_version(SharedSignalConstants.SPEC_VERSION_1_0);
        md.setIssuer(this.issuer);
        md.setJwks_uri(this.issuer + "/.well-known/jwks.json");

        md.setDelivery_methods_supported(List.of(
            SharedSignalConstants.DELIVERY_METHOD_PUSH,
            SharedSignalConstants.DELIVERY_METHOD_POLL
        ));

        md.setConfiguration_endpoint(this.issuer + "/ssf/stream");
        md.setStatus_endpoint(this.issuer + "/ssf/status");
        md.setAdd_subject_endpoint(this.issuer + "/ssf/subject/add");
        md.setRemove_subject_endpoint(this.issuer + "/ssf/subject/remove");
        md.setVerification_endpoint(this.issuer + "/ssf/verification");

        md.setSubject_types_supported(List.of("email", "iss_sub", "opaque", "phone_number"));
        md.setCritical_subject_members(List.of("format"));
        md.setDefault_subjects(SharedSignalConstants.DEFAULT_SUBJECT_MODE);

        TransmitterMetadata.AuthorizationScheme authScheme = new TransmitterMetadata.AuthorizationScheme(
            SharedSignalConstants.SPEC_URN_OAUTH_2,
            List.of("ssf-mgmt")
        );
        md.setAuthorization_schemes(List.of(authScheme));

        return md;
    }
}
