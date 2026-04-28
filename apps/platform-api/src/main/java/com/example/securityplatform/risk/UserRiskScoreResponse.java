package com.example.securityplatform.risk;

import java.time.Instant;
import java.util.UUID;

public record UserRiskScoreResponse(
        UUID userRiskId,
        String username,
        Integer riskScore,
        String reason,
        Instant lastSeenAt,
        Instant createdAt
) {
}
