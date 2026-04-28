package com.example.securityplatform.incidents;

import com.example.securityplatform.common.Severity;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID incidentId,
        String title,
        Severity severity,
        String summary,
        UUID actorId,
        Integer incidentScore,
        String status,
        Instant createdAt
) {
}
