package com.example.securityplatform.events.dto;

import com.example.securityplatform.events.domain.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RawEventIngestionRequest(
        @NotNull SourceType sourceType,
        @NotNull Instant occurredAt,
        String sourceIp,
        String destinationIp,
        Integer destinationPort,
        String username,
        String serviceName,
        String endpoint,
        @NotBlank String rawMessage
) {
}
