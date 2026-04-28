package com.example.securityplatform.investigation;

import java.util.List;

public record InvestigationSessionState(
        String sessionId,
        InvestigationIntent lastIntent,
        String lastTargetType,
        String lastTargetId,
        String lastTargetLabel,
        List<InvestigationTurn> conversationHistory
) {
}
