package com.nevzatcirak.sharedsignals.web.context;

import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.api.exception.SsfSecurityException;
import com.nevzatcirak.sharedsignals.api.facade.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Web implementation of AuthFacade.
 * Extracts authentication details from the HTTP Servlet Request attributes.
 */
@Component
public class WebAuthFacade implements AuthFacade {

    private final HttpServletRequest request;

    public WebAuthFacade(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getClientId() {
        String clientId = (String) request.getAttribute(SecurityConstants.ATTRIBUTE_CLIENT_ID);

        if (clientId == null) {
            throw new SsfSecurityException("No authenticated client ID found in request context.");
        }
        return clientId;
    }
}
