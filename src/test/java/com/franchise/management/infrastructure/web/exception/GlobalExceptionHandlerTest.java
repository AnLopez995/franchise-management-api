package com.franchise.management.infrastructure.web.exception;

import com.franchise.management.infrastructure.web.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimisticLockingFailureMapsToConflict() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.patch("/api/v1/franchises/f1/name"));

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLocking(
                new OptimisticLockingFailureException("stale"), exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.error()).isEqualTo("CONFLICT");
        assertThat(body.path()).isEqualTo("/api/v1/franchises/f1/name");
    }
}
