package com.franchise.management.infrastructure.web.dto.response;

import java.time.LocalDateTime;

/**
 * Standard error payload returned by {@code GlobalExceptionHandler}.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
