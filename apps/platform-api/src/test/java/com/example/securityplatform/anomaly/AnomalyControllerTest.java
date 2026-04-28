package com.example.securityplatform.anomaly;

import com.example.securityplatform.anomaly.service.AnomalyQueryService;
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

@WebMvcTest(AnomalyController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnomalyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnomalyQueryService anomalyQueryService;

    @Test
    void getReturnsAnomalies() throws Exception {
        when(anomalyQueryService.list()).thenReturn(List.of(
                new AnomalyRecordResponse(
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                        0.92,
                        "isolation-forest-baseline",
                        "rule_match_count=3",
                        Instant.parse("2026-04-24T07:00:00Z")
                )
        ));

        mockMvc.perform(get("/api/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.data[0].anomalyScore").value(0.92))
                .andExpect(jsonPath("$.data[0].modelName").value("isolation-forest-baseline"));
    }
}
