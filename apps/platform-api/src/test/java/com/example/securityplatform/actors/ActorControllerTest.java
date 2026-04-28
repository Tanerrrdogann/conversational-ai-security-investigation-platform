package com.example.securityplatform.actors;

import com.example.securityplatform.actors.service.ActorQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActorQueryService actorQueryService;

    @Test
    void getReturnsActors() throws Exception {
        when(actorQueryService.list()).thenReturn(List.of(
                new ActorResponse(
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        "ip:185.24.91.18",
                        "IP",
                        "185.24.91.18",
                        81,
                        Instant.parse("2026-04-24T07:00:00Z"),
                        Instant.parse("2026-04-23T07:00:00Z")
                )
        ));

        mockMvc.perform(get("/api/actors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.data[0].displayName").value("185.24.91.18"))
                .andExpect(jsonPath("$.data[0].riskScore").value(81));
    }
}
