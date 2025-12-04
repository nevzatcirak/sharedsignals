package com.nevzatcirak.sharedsignals.web.advice;

import com.nevzatcirak.sharedsignals.api.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Global Exception Handler.
 * Fully compliant with SSF Spec error codes including 429.
 */
@RestControllerAdvice
public class GlobalSsfExceptionHandler {

    private final String issuerUrl;

    public GlobalSsfExceptionHandler(@Value("${sharedsignals.issuer}") String issuerUrl) {
        this.issuerUrl = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
    }

    @ExceptionHandler(StreamNotFoundException.class)
    ProblemDetail handleStreamNotFound(StreamNotFoundException e) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(StreamAlreadyExistsException.class)
    ProblemDetail handleStreamConflict(StreamAlreadyExistsException e) {
        return buildProblemDetail(HttpStatus.CONFLICT, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(SubjectAlreadyExistsException.class)
    ProblemDetail handleSubjectConflict(SubjectAlreadyExistsException e) {
        return buildProblemDetail(HttpStatus.CONFLICT, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler({InvalidConfigurationException.class, SsfBadRequestException.class})
    ProblemDetail handleBadRequest(SsfException e) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleJsonError(HttpMessageNotReadableException e) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, SsfErrorCode.MALFORMED_REQUEST, "Malformed JSON request body.");
    }

    // --- NEW: 429 Too Many Requests ---
    @ExceptionHandler(RateLimitExceededException.class)
    ProblemDetail handleRateLimit(RateLimitExceededException e) {
        return buildProblemDetail(HttpStatus.TOO_MANY_REQUESTS, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(SsfSecurityException.class)
    ProblemDetail handleSecurityException(SsfSecurityException e) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(SsfException.class)
    ProblemDetail handleGenericSsf(SsfException e) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnknown(Exception e) {
        e.printStackTrace();
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, SsfErrorCode.INTERNAL_ERROR, "An unexpected error occurred.");
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, SsfErrorCode errorCode, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(errorCode.getDescription());
        problemDetail.setType(URI.create(this.issuerUrl + "/errors/" + errorCode.getCode()));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("code", errorCode.getCode());
        return problemDetail;
    }
}
