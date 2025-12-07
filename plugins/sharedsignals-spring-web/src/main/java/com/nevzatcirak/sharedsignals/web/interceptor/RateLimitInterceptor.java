package com.nevzatcirak.sharedsignals.web.interceptor;

import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.api.exception.RateLimitExceededException;
import com.nevzatcirak.sharedsignals.api.model.RateLimitResult;
import com.nevzatcirak.sharedsignals.api.spi.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final boolean enabled;

    public RateLimitInterceptor(
            RateLimiter rateLimiter,
            @Value("${sharedsignals.ratelimit.enabled:true}") boolean enabled) {
        this.rateLimiter = rateLimiter;
        this.enabled = enabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!enabled) {
            return true;
        }

        if (request.getRequestURI().startsWith("/.well-known")) {
            return true;
        }

        String rateLimitKey = (String) request.getAttribute(SecurityConstants.ATTRIBUTE_CLIENT_ID);
        if (rateLimitKey == null || rateLimitKey.isBlank()) {
            rateLimitKey = request.getRemoteAddr();
        }

        RateLimitResult result = rateLimiter.tryConsume(rateLimitKey);

        if (result.isAllowed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(result.getRemainingTokens()));
            return true;
        } else {
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(result.getWaitTimeSeconds()));
            throw new RateLimitExceededException(
                String.format("Rate limit exceeded. Try again in %d seconds.", result.getWaitTimeSeconds())
            );
        }
    }
}