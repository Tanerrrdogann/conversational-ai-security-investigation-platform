package com.example.securityplatform.investigation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestigationIntentServiceTest {

    private final InvestigationIntentService intentService = new InvestigationIntentService();

    @Test
    void detectIntentReusesPreviousIntentForFollowUpQuestion() {
        var sessionState = new InvestigationSessionState(
                "session-1",
                InvestigationIntent.ACTOR_INVESTIGATION,
                "actor",
                "actor-1",
                "185.24.91.18",
                List.of(new InvestigationTurn("user", "Which IP is the riskiest right now?"))
        );

        var intent = intentService.detectIntent("why", sessionState);

        assertThat(intent).isEqualTo(InvestigationIntent.ACTOR_INVESTIGATION);
    }

    @Test
    void extractActorReferenceReturnsIpWhenPresent() {
        var sessionState = new InvestigationSessionState("session-1", null, null, null, null, List.of());

        var actorReference = intentService.extractActorReference("Why is 185.24.91.18 risky?", sessionState);

        assertThat(actorReference).isEqualTo("185.24.91.18");
    }
}
