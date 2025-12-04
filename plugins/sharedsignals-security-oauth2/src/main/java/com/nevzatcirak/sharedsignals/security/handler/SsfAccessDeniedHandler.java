package com.nevzatcirak.sharedsignals.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevzatcirak.sharedsignals.api.exception.SsfErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

public class SsfAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final String issuerUrl;

    public SsfAccessDeniedHandler(ObjectMapper objectMapper, String issuerUrl) {
        this.objectMapper = objectMapper;
        this.issuerUrl = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        // Use the standardized enum constant for SSF-3003 (as per original project definition)
        SsfErrorCode errorCode = SsfErrorCode.UNAUTHORIZED_ACCESS;

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Insufficient permissions");
        problem.setTitle(errorCode.getDescription());

        // DYNAMIC URL
        problem.setType(URI.create(this.issuerUrl + "/errors/" + errorCode.getCode()));

        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("code", errorCode.getCode());

        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
