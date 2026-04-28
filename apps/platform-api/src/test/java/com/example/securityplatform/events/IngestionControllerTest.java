package com.example.securityplatform.events;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.controller.IngestionController;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.ProcessingStatus;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.IngestionResponse;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.service.EventIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventIngestionService eventIngestionService;

    @Test
    void postIngestsRawEventAndReturnsNormalizedResult() throws Exception {
        when(eventIngestionService.ingest(any())).thenReturn(
                new IngestionResponse(
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        ProcessingStatus.NORMALIZED,
                        java.util.List.of(
                                "Log analysis extracted failed-authentication behavior from the raw message.",
                                "Log analysis noticed activity involving an admin or privileged identity."
                        ),
                        new NormalizedEventResponse(
                                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                                EventType.FAILED_LOGIN,
                                SourceType.AUTH_LOG,
                                Instant.parse("2026-04-23T14:00:00Z"),
                                Severity.HIGH,
                                "10.0.0.5",
                                "10.0.0.10",
                                443,
                                "admin",
                                "auth-service",
                                "/login",
                                "raw",
                                "Detected failed authentication activity from source 10.0.0.5",
                                "event:failed_login",
                                "user:admin",
                                Instant.parse("2026-04-23T14:01:00Z")
                        ),
                        java.util.List.of("ADMIN_LOGIN_FAILURE"),
                        UUID.fromString("88888888-8888-8888-8888-888888888888"),
                        60,
                        UUID.fromString("99999999-9999-9999-9999-999999999999"),
                        70,
                        0.91
                )
        );

        var payload = """
                {
                  "sourceType": "AUTH_LOG",
                  "occurredAt": "2026-04-23T14:00:00Z",
                  "sourceIp": "10.0.0.5",
                  "destinationIp": "10.0.0.10",
                  "destinationPort": 443,
                  "username": "admin",
                  "serviceName": "auth-service",
                  "endpoint": "/login",
                  "rawMessage": "failed login for admin account"
                }
                """;

        mockMvc.perform(post("/api/ingestion/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.data.processingStatus").value("NORMALIZED"))
                .andExpect(jsonPath("$.data.analysisFindings[0]").exists())
                .andExpect(jsonPath("$.data.normalizedEvent.eventType").value("FAILED_LOGIN"))
                .andExpect(jsonPath("$.data.triggeredRules[0]").value("ADMIN_LOGIN_FAILURE"))
                .andExpect(jsonPath("$.data.actorRiskScore").value(60))
                .andExpect(jsonPath("$.data.incidentScore").value(70))
                .andExpect(jsonPath("$.data.anomalyScore").value(0.91));
    }
}
