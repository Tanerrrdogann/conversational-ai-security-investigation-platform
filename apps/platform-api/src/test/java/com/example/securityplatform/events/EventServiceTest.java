package com.example.securityplatform.events;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.EventFilterRequest;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import com.example.securityplatform.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private NormalizedEventRepository normalizedEventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void listFiltersByEventTypeAndSourceIp() {
        var failedLogin = event(
                EventType.FAILED_LOGIN,
                SourceType.AUTH_LOG,
                Severity.HIGH,
                "10.0.0.5",
                "admin",
                "user:admin",
                "2026-04-23T12:00:00Z"
        );
        var portScan = event(
                EventType.PORT_SCAN_ATTEMPT,
                SourceType.TRAFFIC,
                Severity.HIGH,
                "185.24.91.18",
                null,
                "ip:185.24.91.18",
                "2026-04-23T13:00:00Z"
        );

        when(normalizedEventRepository.findAll()).thenReturn(List.of(failedLogin, portScan));

        var results = eventService.list(new EventFilterRequest(
                EventType.FAILED_LOGIN,
                null,
                null,
                "10.0.0.5",
                null,
                null,
                null,
                null
        ));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().eventType()).isEqualTo(EventType.FAILED_LOGIN);
        assertThat(results.getFirst().sourceIp()).isEqualTo("10.0.0.5");
    }

    @Test
    void listFiltersByOccurredRange() {
        var older = event(
                EventType.ERROR_SPIKE,
                SourceType.APP_LOG,
                Severity.MEDIUM,
                null,
                null,
                "source:app_log",
                "2026-04-23T10:00:00Z"
        );
        var newer = event(
                EventType.ERROR_SPIKE,
                SourceType.APP_LOG,
                Severity.MEDIUM,
                null,
                null,
                "source:app_log",
                "2026-04-23T15:00:00Z"
        );

        when(normalizedEventRepository.findAll()).thenReturn(List.of(older, newer));

        var results = eventService.list(new EventFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-04-23T12:00:00Z"),
                Instant.parse("2026-04-23T16:00:00Z")
        ));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().occurredAt()).isEqualTo(Instant.parse("2026-04-23T15:00:00Z"));
    }

    private NormalizedEvent event(
            EventType eventType,
            SourceType sourceType,
            Severity severity,
            String sourceIp,
            String username,
            String correlationKey,
            String occurredAt
    ) {
        return new NormalizedEvent(
                UUID.randomUUID(),
                eventType,
                sourceType,
                Instant.parse(occurredAt),
                severity,
                sourceIp,
                "10.0.0.10",
                443,
                username,
                "service",
                "/endpoint",
                "raw",
                "normalized",
                "event:" + eventType.name().toLowerCase(),
                correlationKey,
                Instant.now()
        );
    }
}
