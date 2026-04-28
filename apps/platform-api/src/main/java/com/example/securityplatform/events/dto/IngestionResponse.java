package com.example.securityplatform.events.dto;

import com.example.securityplatform.events.domain.ProcessingStatus;

import java.util.List;
import java.util.UUID;

public record IngestionResponse(
        UUID ingestionId,
        ProcessingStatus processingStatus,
        List<String> analysisFindings,
        NormalizedEventResponse normalizedEvent,
        List<String> triggeredRules,
        UUID actorId,
        Integer actorRiskScore,
        UUID incidentId,
        Integer incidentScore,
        Double anomalyScore
) {
}
