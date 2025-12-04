package com.nevzatcirak.sharedsignals.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.security.handler.SsfAuthenticationEntryPoint;
import com.nevzatcirak.sharedsignals.security.handler.SsfAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Shared Signals Framework.
 * <p>
 * Configures OAuth2 Resource Server with JWT authentication and
 * defines authorization rules for SSF endpoints per specification.
 */
@Configuration
@EnableWebSecurity
public class SsfSecurityAutoConfiguration {

    @Value("${sharedsignals.issuer}")
    private String issuerUrl;

    /**
     * Configures security filter chain with SSF-compliant endpoint protection.
     * <p>
     * SSF Spec Section 8.1: Event Stream Management API endpoints require authentication.
     *
     * @param http the HttpSecurity to configure
     * @param objectMapper Jackson ObjectMapper for error response serialization
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (SSF Discovery)
                .requestMatchers("/.well-known/**").permitAll()

                // Webhook endpoints (if used for push delivery validation)
                .requestMatchers("/webhook/**").permitAll()

                // SSF Management API - All endpoints require authentication (SSF Spec 8.1)
                // - /ssf/stream (Configuration Endpoint - 8.1.1)
                // - /ssf/status (Status Endpoint - 8.1.2)
                // - /ssf/subjects (Subject Management - 8.1.3)
                // - /ssf/verification (Verification Endpoint - 8.1.4)
                .requestMatchers("/ssf/**").authenticated()

                // Default: All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(new SsfAuthenticationEntryPoint(objectMapper, issuerUrl))
                .accessDeniedHandler(new SsfAccessDeniedHandler(objectMapper, issuerUrl))
            );
        return http.build();
    }
}