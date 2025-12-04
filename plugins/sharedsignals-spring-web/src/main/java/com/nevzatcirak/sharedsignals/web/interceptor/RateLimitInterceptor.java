package com.nevzatcirak.sharedsignals.web.interceptor;

import com.nevzatcirak.sharedsignals.api.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interval-based Rate Limiter.
 * Ensures that a client waits at least a certain amount of time between requests.
 * * Target: ~100 requests/minute
 * Calculation: 60,000 ms / 100 = 600 ms interval.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Minimum time (in milliseconds) required between two requests from the same IP.
    private static final long MIN_INTERVAL_MS = 600;

    // Stores the timestamp of the last successful request for each IP.
    private final Map<String, Long> clientLastRequestTime = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Skip rate limiting for public discovery endpoints
        if (request.getRequestURI().startsWith("/.well-known")) {
            return true;
        }

        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        // Check last request time atomically
        Long lastTime = clientLastRequestTime.get(clientIp);

        if (lastTime != null) {
            long timeElapsed = currentTime - lastTime;
            if (timeElapsed < MIN_INTERVAL_MS) {
                long waitTime = MIN_INTERVAL_MS - timeElapsed;
                throw new RateLimitExceededException(
                    String.format("Too many requests. Please wait %d ms before retrying.", waitTime)
                );
            }
        }

        // Update the last request time
        clientLastRequestTime.put(clientIp, currentTime);
        return true;
    }
}
