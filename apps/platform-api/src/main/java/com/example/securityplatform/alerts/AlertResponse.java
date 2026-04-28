package com.example.securityplatform.alerts;

import com.example.securityplatform.common.Severity;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID alertId,
        String title,
        Severity severity,
        String reason,
        String source,
        String status,
        UUID eventId,
        String triggeredRule,
        String supportingEvidence,
        Double anomalyContribution,
        String relatedIncidents,
        String recommendation,
        String acknowledgmentState,
        String resolutionState,
        Instant createdAt
) {
}
