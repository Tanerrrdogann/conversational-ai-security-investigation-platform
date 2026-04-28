package com.example.securityplatform.events;

import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.events.service.EventNormalizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventNormalizationServiceTest {

    @Mock
    private NormalizedEventRepository normalizedEventRepository;

    @InjectMocks
    private EventNormalizationService eventNormalizationService;

    @Test
    void normalizeTurnsRawAuthEventIntoNormalizedSecurityEvent() {
        var rawEvent = new RawIngestedEvent(
                UUID.randomUUID(),
                SourceType.AUTH_LOG,
                Instant.parse("2026-04-23T12:00:00Z"),
                "10.0.0.5",
                "10.0.0.10",
                443,
                "admin",
                "auth-service",
                "/login",
                "failed login for admin account",
                null,
                Instant.now()
        );

        when(normalizedEventRepository.save(any(NormalizedEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var normalized = eventNormalizationService.normalize(rawEvent);

        assertThat(normalized.eventType().name()).isEqualTo("FAILED_LOGIN");
        assertThat(normalized.severity().name()).isEqualTo("HIGH");
        assertThat(normalized.tags()).contains("event:failed_login");
        assertThat(normalized.correlationKey()).isEqualTo("user:admin");
    }
}
