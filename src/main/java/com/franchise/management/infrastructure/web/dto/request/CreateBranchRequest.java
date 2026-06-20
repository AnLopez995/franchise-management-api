package com.franchise.management.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBranchRequest(
        @NotBlank(message = "name must not be blank") String name
) {
}
