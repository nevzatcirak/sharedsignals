package com.nevzatcirak.sharedsignals.api.service;

import java.util.Map;

/**
 * Service interface for exposing the public keys (JWKS) used to verify SETs.
 */
public interface JwkSetService {
    Map<String, Object> getJwkSet();
}
