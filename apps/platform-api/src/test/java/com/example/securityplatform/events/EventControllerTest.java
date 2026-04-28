package com.example.securityplatform.events;

import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.controller.EventController;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.EventFilterRequest;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    void getReturnsFilteredEvents() throws Exception {
        when(eventService.list(any(EventFilterRequest.class))).thenReturn(List.of(
                new NormalizedEventResponse(
                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        EventType.FAILED_LOGIN,
                        SourceType.AUTH_LOG,
                        Instant.parse("2026-04-23T12:00:00Z"),
                        Severity.HIGH,
                        "10.0.0.5",
                        "10.0.0.10",
                        443,
                        "admin",
                        "auth-service",
                        "/login",
                        "raw",
                        "normalized",
                        "event:failed_login",
                        "user:admin",
                        Instant.parse("2026-04-23T12:01:00Z")
                )
        ));

        mockMvc.perform(get("/api/events")
                        .param("eventType", "FAILED_LOGIN")
                        .param("sourceIp", "10.0.0.5")
                        .param("occurredAfter", "2026-04-23T11:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.data[0].eventType").value("FAILED_LOGIN"))
                .andExpect(jsonPath("$.data[0].sourceIp").value("10.0.0.5"));
    }
}
