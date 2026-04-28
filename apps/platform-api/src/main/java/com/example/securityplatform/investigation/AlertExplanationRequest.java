package com.example.securityplatform.investigation;

public record AlertExplanationRequest(
        String title,
        String severity,
        String reason,
        String source,
        String triggeredRule,
        String supportingEvidence,
        Double anomalyContribution,
        String relatedIncidents,
        String recommendation,
        String mode
) {
}
