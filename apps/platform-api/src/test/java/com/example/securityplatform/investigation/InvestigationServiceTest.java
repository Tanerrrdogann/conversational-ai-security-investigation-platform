package com.example.securityplatform.investigation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigationServiceTest {

    @Mock
    private InvestigationSessionMemoryService sessionMemoryService;

    @Mock
    private InvestigationIntentService intentService;

    @Mock
    private InvestigationQueryOrchestrator queryOrchestrator;

    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private InvestigationService investigationService;

    @Test
    void investigateBuildsContextAndPersistsConversationMemory() {
        var request = new ChatMessageRequest("Which IP is the riskiest right now?", null, "technical");
        var sessionState = new InvestigationSessionState("session-123", null, null, null, null, List.of());
        var context = new InvestigationContext(
                InvestigationIntent.TOP_RISKY_ACTOR,
                "actor",
                "actor-123",
                "185.24.91.18",
                "Investigate actor",
                List.of("Current risk score: 80", "Recent event types: [PORT_SCAN_ATTEMPT]"),
                List.of("Review latest events")
        );

        when(sessionMemoryService.getOrCreate(null)).thenReturn(sessionState);
        when(intentService.detectIntent(request.message(), sessionState)).thenReturn(InvestigationIntent.TOP_RISKY_ACTOR);
        when(intentService.extractActorReference(request.message(), sessionState)).thenReturn("185.24.91.18");
        when(queryOrchestrator.buildContext(InvestigationIntent.TOP_RISKY_ACTOR, request.message(), sessionState, "185.24.91.18"))
                .thenReturn(context);
        when(aiServiceClient.investigate(any())).thenReturn(new InvestigationResponse(
                "The top actor is 185.24.91.18.",
                "technical",
                "prompt",
                "session-123",
                "TOP_RISKY_ACTOR",
                List.of("Review latest events"),
                "llm:openai"
        ));

        var response = investigationService.investigate(request);

        assertThat(response.sessionId()).isEqualTo("session-123");
        assertThat(response.intent()).isEqualTo("TOP_RISKY_ACTOR");
        assertThat(response.engine()).isEqualTo("llm:openai");
        assertThat(response.answer()).contains("185.24.91.18");
        verify(sessionMemoryService).remember(
                "session-123",
                InvestigationIntent.TOP_RISKY_ACTOR,
                "actor",
                "actor-123",
                "185.24.91.18",
                request.message(),
                response.answer(),
                "technical",
                List.of("Review latest events")
        );
    }
}
