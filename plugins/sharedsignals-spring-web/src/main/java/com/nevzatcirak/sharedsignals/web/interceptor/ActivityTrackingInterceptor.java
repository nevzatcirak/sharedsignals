package com.nevzatcirak.sharedsignals.web.interceptor;

import com.nevzatcirak.sharedsignals.api.service.InactivityTimeoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to track stream activity for inactivity timeout.
 * <p>
 * SSF Spec Section 8.1.1: The Transmitter MUST restart the inactivity
 * timeout counter when eligible activity is observed.
 * <p>
 * Eligible activities:
 * - PUSH: Any management API endpoint call
 * - POLL: Poll endpoint OR any management API endpoint call
 */
@Component
public class ActivityTrackingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ActivityTrackingInterceptor.class);

    private final InactivityTimeoutService inactivityService;

    public ActivityTrackingInterceptor(InactivityTimeoutService inactivityService) {
        this.inactivityService = inactivityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Track activity for SSF management endpoints
        String uri = request.getRequestURI();

        if (uri.startsWith("/ssf/")) {
            String streamId = extractStreamId(request);

            if (streamId != null && !streamId.isBlank()) {
                log.debug("Recording activity for stream: {} on endpoint: {}", streamId, uri);
                inactivityService.recordActivity(streamId);
            }
        }

        return true;
    }

    /**
     * Extracts stream_id from request (query param or body).
     * <p>
     * Most SSF endpoints include stream_id as a query parameter.
     *
     * @param request the HTTP request
     * @return the stream_id if found, null otherwise
     */
    private String extractStreamId(HttpServletRequest request) {
        // Try query parameter first
        String streamId = request.getParameter("stream_id");
        if (streamId != null) {
            return streamId;
        }

        // For POST requests, stream_id is in the body
        // We can't easily read the body here without consuming it
        // So we'll rely on the service layer to call recordActivity

        // For poll endpoint: /ssf/events/poll/{streamId}
        if (request.getRequestURI().contains("/ssf/events/poll/")) {
            String[] parts = request.getRequestURI().split("/");
            if (parts.length > 0) {
                return parts[parts.length - 1];
            }
        }

        return null;
    }
}