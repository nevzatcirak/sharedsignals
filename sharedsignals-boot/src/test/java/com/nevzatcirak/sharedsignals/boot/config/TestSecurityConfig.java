package com.nevzatcirak.sharedsignals.boot.config;

import com.nevzatcirak.sharedsignals.api.constant.SecurityConstants;
import com.nevzatcirak.sharedsignals.web.interceptor.RateLimitInterceptor;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Optional;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(testAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public Filter testAuthFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                String clientId = req.getHeader("X-SSF-Client-Id");
                if (clientId != null && req.getAttribute(SecurityConstants.ATTRIBUTE_CLIENT_ID) == null) {
                    req.setAttribute(SecurityConstants.ATTRIBUTE_CLIENT_ID, clientId);
                }
                chain.doFilter(request, response);
            }
        };
    }

    /**
     * MOCK LockProvider for Tests.
     * Always acquires the lock successfully.
     * This prevents ShedLock from blocking our manual scheduler calls in tests.
     */
    @Bean
    @Primary
    public LockProvider noOpLockProvider() {
        return new LockProvider() {
            @Override
            public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
                // Return a dummy lock that does nothing on unlock
                return Optional.of(new SimpleLock() {
                    @Override
                    public void unlock() {
                        // No-op
                    }
                });
            }
        };
    }
}
