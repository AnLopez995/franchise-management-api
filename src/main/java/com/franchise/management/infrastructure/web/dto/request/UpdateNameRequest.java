package com.franchise.management.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNameRequest(
        @NotBlank(message = "name must not be blank") String name
) {
}
