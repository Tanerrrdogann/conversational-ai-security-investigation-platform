package com.example.securityplatform.investigation;

import java.util.List;

public record InvestigationResponse(
        String answer,
        String mode,
        String promptSummary,
        String sessionId,
        String intent,
        List<String> recommendations,
        String engine
) {
}
