package com.example.securityplatform.events.dto;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;

import java.time.Instant;
import java.util.UUID;

public record NormalizedEventResponse(
        UUID eventId,
        EventType eventType,
        SourceType sourceType,
        Instant occurredAt,
        Severity severity,
        String sourceIp,
        String destinationIp,
        Integer destinationPort,
        String username,
        String serviceName,
        String endpoint,
        String rawMessage,
        String normalizedMessage,
        String tags,
        String correlationKey,
        Instant createdAt
) {
}
