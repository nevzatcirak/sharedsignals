package com.nevzatcirak.sharedsignals.api.facade;

/**
 * Interface to access authentication details of the current operation.
 * Decouples the Core/API usage from the specific Web/Security implementation.
 */
public interface AuthFacade {

    /**
     * Retrieves the authenticated Client ID (OAuth2 Client).
     * @return The Client ID string.
     * @throws RuntimeException if the user is not authenticated.
     */
    String getClientId();
}
