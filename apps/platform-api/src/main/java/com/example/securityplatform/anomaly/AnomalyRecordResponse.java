package com.example.securityplatform.anomaly;

import java.time.Instant;
import java.util.UUID;

public record AnomalyRecordResponse(
        UUID anomalyId,
        UUID eventId,
        UUID actorId,
        UUID incidentId,
        Double anomalyScore,
        String modelName,
        String featureSummary,
        Instant createdAt
) {
}
