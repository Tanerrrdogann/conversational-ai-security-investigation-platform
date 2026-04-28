package com.example.securityplatform.anomaly;

import com.example.securityplatform.anomaly.service.AnomalyQueryService;
import com.example.securityplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyQueryService anomalyQueryService;

    public AnomalyController(AnomalyQueryService anomalyQueryService) {
        this.anomalyQueryService = anomalyQueryService;
    }

    @GetMapping
    public ApiResponse<List<AnomalyRecordResponse>> listAnomalies() {
        return ApiResponse.ok(anomalyQueryService.list());
    }
}
