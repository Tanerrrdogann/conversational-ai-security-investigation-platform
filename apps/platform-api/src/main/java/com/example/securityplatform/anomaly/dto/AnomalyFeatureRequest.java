package com.example.securityplatform.anomaly.dto;

public record AnomalyFeatureRequest(
        int requestCount,
        int uniquePortCount,
        int failedLoginCount,
        int uniqueEndpointCount,
        int accessDeniedCount,
        int privilegedEndpointAccessCount,
        int offHoursActivity,
        int ruleMatchCount,
        int actorRiskScore,
        int incidentScore
) {
}
