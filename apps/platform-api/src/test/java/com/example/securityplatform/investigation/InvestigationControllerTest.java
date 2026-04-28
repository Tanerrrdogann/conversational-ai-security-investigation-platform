package com.example.securityplatform.investigation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvestigationController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvestigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvestigationService investigationService;

    @Test
    void postReturnsInvestigationResponseWithSessionAndIntent() throws Exception {
        when(investigationService.investigate(any())).thenReturn(new InvestigationResponse(
                "The top actor is 185.24.91.18.",
                "technical",
                "prompt",
                "session-123",
                "TOP_RISKY_ACTOR",
                List.of("Review latest events"),
                "llm:openai"
        ));

        var payload = """
                {
                  "message": "Which IP is the riskiest right now?",
                  "mode": "technical"
                }
                """;

        mockMvc.perform(post("/api/investigations/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.data.sessionId").value("session-123"))
                .andExpect(jsonPath("$.data.intent").value("TOP_RISKY_ACTOR"))
                .andExpect(jsonPath("$.data.engine").value("llm:openai"))
                .andExpect(jsonPath("$.data.recommendations[0]").value("Review latest events"));
    }
}
