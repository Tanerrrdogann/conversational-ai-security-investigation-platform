package com.example.securityplatform.sources;

import com.example.securityplatform.events.domain.SourceType;

import java.time.Instant;
import java.util.UUID;

public record SourceManagementResponse(
        UUID sourceId,
        String name,
        SourceType sourceType,
        String ingestionMethod,
        String description,
        boolean enabled,
        Instant createdAt
) {
}
