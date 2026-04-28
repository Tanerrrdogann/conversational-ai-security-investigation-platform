package com.example.securityplatform.investigation;

import java.util.List;

public record InvestigationContext(
        InvestigationIntent intent,
        String targetType,
        String targetId,
        String targetLabel,
        String contextSummary,
        List<String> facts,
        List<String> recommendations
) {
}
