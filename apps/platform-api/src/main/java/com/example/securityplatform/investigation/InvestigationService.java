package com.example.securityplatform.investigation;

import org.springframework.stereotype.Service;

@Service
public class InvestigationService {

    private final InvestigationSessionMemoryService sessionMemoryService;
    private final InvestigationIntentService intentService;
    private final InvestigationQueryOrchestrator queryOrchestrator;
    private final AiServiceClient aiServiceClient;

    public InvestigationService(
            InvestigationSessionMemoryService sessionMemoryService,
            InvestigationIntentService intentService,
            InvestigationQueryOrchestrator queryOrchestrator,
            AiServiceClient aiServiceClient
    ) {
        this.sessionMemoryService = sessionMemoryService;
        this.intentService = intentService;
        this.queryOrchestrator = queryOrchestrator;
        this.aiServiceClient = aiServiceClient;
    }

    public InvestigationResponse investigate(ChatMessageRequest request) {
        InvestigationSessionState sessionState = sessionMemoryService.getOrCreate(request.sessionId());
        InvestigationIntent intent = intentService.detectIntent(request.message(), sessionState);
        String actorReference = intentService.extractActorReference(request.message(), sessionState);
        InvestigationContext context = queryOrchestrator.buildContext(intent, request.message(), sessionState, actorReference);

        InvestigationResponse response = aiServiceClient.investigate(new InvestigationPromptRequest(
                sessionState.sessionId(),
                intent.name(),
                request.message(),
                request.mode() == null || request.mode().isBlank() ? "technical" : request.mode(),
                context.targetType(),
                context.targetId(),
                context.targetLabel(),
                context.contextSummary(),
                context.facts(),
                context.recommendations(),
                sessionState.conversationHistory()
        ));

        sessionMemoryService.remember(
                sessionState.sessionId(),
                intent,
                context.targetType(),
                context.targetId(),
                context.targetLabel(),
                request.message(),
                response.answer(),
                request.mode() == null || request.mode().isBlank() ? "technical" : request.mode(),
                response.recommendations() == null || response.recommendations().isEmpty()
                        ? context.recommendations()
                        : response.recommendations()
        );

        return new InvestigationResponse(
                response.answer(),
                response.mode(),
                response.promptSummary(),
                sessionState.sessionId(),
                intent.name(),
                response.recommendations() == null || response.recommendations().isEmpty()
                        ? context.recommendations()
                        : response.recommendations(),
                response.engine()
        );
    }
}
