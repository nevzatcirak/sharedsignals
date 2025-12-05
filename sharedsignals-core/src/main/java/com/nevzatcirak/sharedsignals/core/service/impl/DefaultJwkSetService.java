package com.nevzatcirak.sharedsignals.core.service.impl;

import com.nevzatcirak.sharedsignals.api.service.JwkSetService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class DefaultJwkSetService implements JwkSetService {

    private static final Logger log = LoggerFactory.getLogger(DefaultJwkSetService.class);
    private final RSAKey rsaKey;

    /**
     * Constructor.
     *
     * @param base64PrivateKey The Base64 encoded PKCS#8 RSA Private Key.
     * If null or empty, a random ephemeral key will be generated (NOT for production).
     * @param keyId            Optional Key ID. If null, a random UUID is used (or derived from key).
     */
    public DefaultJwkSetService(String base64PrivateKey, String keyId) {
        if (base64PrivateKey != null && !base64PrivateKey.isBlank()) {
            this.rsaKey = loadStaticKey(base64PrivateKey, keyId);
        } else {
            this.rsaKey = generateEphemeralKey();
        }
    }

    private RSAKey loadStaticKey(String base64Key, String keyId) {
        try {
            log.info("Loading persistent RSA signing key...");
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(spec);

            // Derive Public Key from Private Key (RSAPrivateCrtKey interface allows this)
            if (privateKey instanceof java.security.interfaces.RSAPrivateCrtKey) {
                java.security.interfaces.RSAPrivateCrtKey crtKey = (java.security.interfaces.RSAPrivateCrtKey) privateKey;
                java.security.spec.RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(
                        crtKey.getModulus(), crtKey.getPublicExponent());
                RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec);

                String finalKid = (keyId != null && !keyId.isBlank()) ? keyId : "ssf-signing-key-1";

                return new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyUse(KeyUse.SIGNATURE)
                        .algorithm(JWSAlgorithm.RS256)
                        .keyID(finalKid)
                        .build();
            } else {
                throw new IllegalStateException("Provided private key is not an RSAPrivateCrtKey instance, cannot derive public key.");
            }
        } catch (Exception e) {
            log.error("Failed to load static private key. Falling back to ephemeral key.", e);
            return generateEphemeralKey();
        }
    }

    private RSAKey generateEphemeralKey() {
        log.warn("!!! SECURITY WARNING !!! Using EPHEMERAL (Random) Signing Key. " +
                "Events signed with this key cannot be verified after application restart. " +
                "Configure 'sharedsignals.security.signing-key' for production.");
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ephemeral RSA key", e);
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