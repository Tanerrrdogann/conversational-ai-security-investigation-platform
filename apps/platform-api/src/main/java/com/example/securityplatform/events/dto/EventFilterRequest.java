package com.example.securityplatform.events.dto;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;

import java.time.Instant;

public record EventFilterRequest(
        EventType eventType,
        SourceType sourceType,
        Severity severity,
        String sourceIp,
        String username,
        String correlationKey,
        Instant occurredAfter,
        Instant occurredBefore
) {
}
