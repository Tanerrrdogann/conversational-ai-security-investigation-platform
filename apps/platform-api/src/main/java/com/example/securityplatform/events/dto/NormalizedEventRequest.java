package com.example.securityplatform.events.dto;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record NormalizedEventRequest(
        @NotNull EventType eventType,
        @NotNull SourceType sourceType,
        @NotNull Instant occurredAt,
        @NotNull Severity severity,
        String sourceIp,
        String destinationIp,
        Integer destinationPort,
        String username,
        String serviceName,
        String endpoint,
        @NotBlank String rawMessage,
        @NotBlank String normalizedMessage,
        String correlationKey
) {
}
