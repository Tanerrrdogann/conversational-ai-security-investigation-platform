package com.example.securityplatform.risk;

import java.time.Instant;
import java.util.UUID;

public record HostRiskScoreResponse(
        UUID hostRiskId,
        String hostIdentifier,
        Integer riskScore,
        String reason,
        Instant lastSeenAt,
        Instant createdAt
) {
}
