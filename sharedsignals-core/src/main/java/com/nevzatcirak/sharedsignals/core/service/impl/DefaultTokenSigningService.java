package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.model.StreamConfiguration;
import com.nevzatcirak.sharedsignals.api.service.JwkSetService;
import com.nevzatcirak.sharedsignals.api.service.TokenSigningService;
import com.nevzatcirak.sharedsignals.api.constant.SharedSignalConstants;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DefaultTokenSigningService implements TokenSigningService {

    private final String issuer;
    private JWSSigner signer;
    private String keyId;

    public DefaultTokenSigningService(String issuer) {
        this.issuer = issuer;
    }

    public void setSigningKey(com.nimbusds.jose.jwk.RSAKey rsaKey) throws JOSEException {
        this.signer = new RSASSASigner(rsaKey);
        this.keyId = rsaKey.getKeyID();
    }

    @Override
    public String createSignedSet(Map<String, Object> eventPayload, Map<String, Object> subId, String txnId, StreamConfiguration stream) {
        try {
            if (signer == null) throw new IllegalStateException("Signer not initialized");

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(new JOSEObjectType(SharedSignalConstants.JWT_TYPE_SECEVENT))
                    .keyID(this.keyId)
                    .build();

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(this.issuer)
                    .audience(stream.getAud())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(Instant.now()))
                    .claim("events", eventPayload);

            if (txnId != null) {
                claimsBuilder.claim("txn", txnId);
            }

            if (subId != null) {
                claimsBuilder.claim("sub_id", subId);
            }

            SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign SET", e);
        }
    }
}
