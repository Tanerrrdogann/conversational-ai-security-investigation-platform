package com.example.securityplatform.anomaly.dto;

public record AnomalyScoreResponse(
        double anomalyScore,
        String modelName,
        String featureSummary
) {
}
