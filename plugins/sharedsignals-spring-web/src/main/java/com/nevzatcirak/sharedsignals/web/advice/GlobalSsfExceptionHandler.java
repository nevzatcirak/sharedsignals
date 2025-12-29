package com.nevzatcirak.sharedsignals.web.advice;

import com.nevzatcirak.sharedsignals.api.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Global Exception Handler.
 * Fully compliant with SSF Spec error codes and RFC 9457 Problem Details.
 */
@RestControllerAdvice
public class GlobalSsfExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalSsfExceptionHandler.class);
    private final String issuerUrl;

    public GlobalSsfExceptionHandler(@Value("${sharedsignals.issuer}") String issuerUrl) {
        this.issuerUrl = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationErrors(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed. See 'invalid_params' for details."
        );

        problem.setTitle(SsfErrorCode.INVALID_STREAM_CONFIGURATION.getDescription());
        problem.setType(URI.create(this.issuerUrl + "/errors/" + SsfErrorCode.INVALID_STREAM_CONFIGURATION.getCode()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("code", SsfErrorCode.INVALID_STREAM_CONFIGURATION.getCode());

        Map<String, String> invalidParams = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));

        problem.setProperty("invalid_params", invalidParams);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleJsonError(HttpMessageNotReadableException e) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, SsfErrorCode.MALFORMED_REQUEST,
            "Malformed JSON request or invalid data format.");
    }

    @ExceptionHandler(CompletionException.class)
    ProblemDetail handleAsyncException(CompletionException e) {
        // Unwrap the exception thrown within CompletableFuture
        Throwable cause = e.getCause();

        if (cause instanceof SsfException ssfEx) {
            // Recursively handle known domain exceptions
            if (ssfEx instanceof InvalidConfigurationException || ssfEx instanceof SsfBadRequestException) {
                return handleBadRequest(ssfEx);
            }
            if (ssfEx instanceof StreamNotFoundException) {
                return handleStreamNotFound((StreamNotFoundException) ssfEx);
            }
            return handleGenericSsf(ssfEx);
        }

        return handleUnknown(new Exception(cause));
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

    // --- Catch-All Fallback ---
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnknown(Exception e) {
        log.error("An unexpected error occurred", e);
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