package com.franchise.management.infrastructure.web.exception;

import com.franchise.management.domain.exception.BusinessValidationException;
import com.franchise.management.domain.exception.ResourceNotFoundException;
import com.franchise.management.infrastructure.web.dto.response.ErrorResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralizes exception-to-HTTP translation so controllers stay free of error handling.
 * Runs on the reactive stack: the request path is read from the {@link ServerWebExchange}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(BusinessValidationException ex,
                                                                  ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex,
                                                                 ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT,
                "The franchise was modified concurrently; reload and retry.", exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex,
                                                          ServerWebExchange exchange) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, ServerWebExchange exchange) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", exchange);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                ServerWebExchange exchange) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message,
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(status).body(body);
    }
}
