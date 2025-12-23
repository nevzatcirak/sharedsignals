package com.nevzatcirak.sharedsignals.security.context;

import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;
import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Web implementation of AuthFacade.
 * Extracts authentication details from the HTTP Servlet Request attributes.
 */
@Primary
@Component
public class SpringSecurityAuthFacade implements AuthFacade {

    private final HttpServletRequest request;

    public SpringSecurityAuthFacade(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getClientId() {
        String clientId = resolveClientIdentifier(request);

        if (clientId == null) {
            throw new SsfSecurityException("No authenticated client ID found in request context.");
        }
        return clientId;
    }

    /**
     * Extracts the Client ID from the Security Context (JWT) if authenticated.
     * Fallbacks to IP Address for anonymous requests.
     */
    private String resolveClientIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return authentication.getName();
        }

        return request.getRemoteAddr();
    }
}
