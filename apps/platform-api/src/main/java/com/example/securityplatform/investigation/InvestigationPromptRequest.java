package com.example.securityplatform.investigation;

import java.util.List;

public record InvestigationPromptRequest(
        String sessionId,
        String intent,
        String userMessage,
        String mode,
        String targetType,
        String targetId,
        String targetLabel,
        String contextSummary,
        List<String> facts,
        List<String> recommendations,
        List<InvestigationTurn> conversationHistory
) {
}
