package com.example.securityplatform.actors;

import java.time.Instant;
import java.util.UUID;

public record ActorResponse(
        UUID actorId,
        String actorKey,
        String actorType,
        String displayName,
        Integer riskScore,
        Instant lastSeenAt,
        Instant createdAt
) {
}
