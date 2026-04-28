package com.example.securityplatform.actors;

import com.example.securityplatform.actors.domain.Actor;
import com.example.securityplatform.actors.domain.ActorRiskHistory;
import com.example.securityplatform.actors.repository.ActorRepository;
import com.example.securityplatform.actors.repository.ActorRiskHistoryRepository;
import com.example.securityplatform.actors.service.ActorService;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.rules.service.RuleEvaluationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private ActorRiskHistoryRepository actorRiskHistoryRepository;

    @InjectMocks
    private ActorService actorService;

    @Test
    void resolveAndScoreActorCreatesActorAndHistory() {
        var event = new NormalizedEventResponse(
                UUID.randomUUID(),
                EventType.FAILED_LOGIN,
                SourceType.AUTH_LOG,
                Instant.parse("2026-04-23T12:00:00Z"),
                Severity.HIGH,
                "10.0.0.5",
                "10.0.0.10",
                443,
                "admin",
                "auth-service",
                "/admin/login",
                "raw",
                "normalized",
                "event:failed_login",
                "user:admin",
                Instant.now()
        );

        when(actorRepository.findAll()).thenReturn(List.of());
        when(actorRepository.save(any(Actor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actorRiskHistoryRepository.findAll()).thenReturn(List.of());
        when(actorRiskHistoryRepository.save(any(ActorRiskHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = actorService.resolveAndScoreActor(event, List.of(
                new RuleEvaluationResult("ADMIN_LOGIN_FAILURE", Severity.HIGH, "title", "reason")
        ));

        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
        verify(actorRepository, org.mockito.Mockito.times(2)).save(actorCaptor.capture());

        assertThat(actorCaptor.getAllValues().getLast().getActorKey()).isEqualTo("user:admin");
        assertThat(result.updatedRiskScore()).isGreaterThan(0);
    }
}
