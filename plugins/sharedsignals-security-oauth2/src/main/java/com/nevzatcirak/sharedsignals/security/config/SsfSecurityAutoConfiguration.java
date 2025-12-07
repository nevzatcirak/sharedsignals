package com.nevzatcirak.sharedsignals.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.security.handler.SsfAuthenticationEntryPoint;
import com.nevzatcirak.sharedsignals.security.handler.SsfAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Shared Signals Framework.
 * <p>
 * Configures OAuth2 Resource Server with JWT authentication.
 * IMPORTANT: This configuration is DISABLED in the 'test' profile to allow
 * integration tests to use a bypass configuration (TestSecurityConfig).
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
public class SsfSecurityAutoConfiguration {

    @Value("${sharedsignals.issuer}")
    private String issuerUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow access to Swagger UI and API Docs
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()

                // Allow access to Well-Known endpoints (Discovery)
                // Use strict matching and wildcard matching to cover all cases
                .requestMatchers("/.well-known/**").permitAll()

                // Allow access to Webhooks (if any)
                .requestMatchers("/webhook/**").permitAll()

                // Secure everything else (SSF API, Ingestion API)
                .requestMatchers("/ssf/**", "/api/v1/ingest/**").authenticated()
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
