package com.example.securityplatform.sources;

import com.example.securityplatform.events.domain.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SourceManagementRequest(
        @NotBlank String name,
        @NotNull SourceType sourceType,
        @NotBlank String ingestionMethod,
        @NotBlank String description,
        boolean enabled
) {
}
