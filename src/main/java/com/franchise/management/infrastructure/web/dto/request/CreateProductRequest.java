package com.franchise.management.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductRequest(
        @NotBlank(message = "name must not be blank") String name,
        @NotNull(message = "stock must not be null")
        @PositiveOrZero(message = "stock must not be negative") Integer stock
) {
}
