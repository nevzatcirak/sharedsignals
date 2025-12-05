package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.service.JwkSetService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;

public class DefaultJwkSetService implements JwkSetService {

    private final RSAKey rsaKey;

    public DefaultJwkSetService() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            this.rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key", e);
        }
    }

    @Override
    public Map<String, Object> getJwkSet() {
        return this.rsaKey.toPublicJWK().toJSONObject();
    }

    public RSAKey getRsaKey() {
        return this.rsaKey;
    }
}
