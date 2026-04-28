package com.example.securityplatform.actors.service;

import com.example.securityplatform.actors.domain.Actor;

public record ActorResolutionResult(
        Actor actor,
        int updatedRiskScore
) {
}
