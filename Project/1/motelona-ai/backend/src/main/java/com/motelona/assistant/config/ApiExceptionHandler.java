package com.motelona.assistant.config;

import com.motelona.assistant.dto.ApiError;
import com.motelona.assistant.service.AiUnavailableException;
import com.motelona.assistant.service.RateLimitException;
import com.motelona.assistant.search.SearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<ApiError> aiUnavailable(AiUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiError.of("AI_UNAVAILABLE", ex.getMessage()));
    }

    @ExceptionHandler(SearchUnavailableException.class)
    public ResponseEntity<ApiError> searchUnavailable(SearchUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiError.of("SEARCH_UNAVAILABLE", ex.getMessage()));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiError> rateLimited(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiError.of("RATE_LIMITED", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> malformedBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_JSON", "The request body is not valid JSON or does not match the expected structure."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> badParameter(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_PARAMETER", "A request parameter has an invalid format."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unhandled API exception; exceptionType={}", ex.getClass().getSimpleName());
        log.debug("Unhandled API exception details (enable debug only while diagnosing locally)", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "The request could not be completed. Check backend logs without sharing private data."));
    }
}
