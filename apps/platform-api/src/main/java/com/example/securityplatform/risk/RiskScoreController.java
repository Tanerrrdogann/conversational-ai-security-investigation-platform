package com.example.securityplatform.risk;

import com.example.securityplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk-scores")
public class RiskScoreController {

    private final RiskScoreService riskScoreService;

    public RiskScoreController(RiskScoreService riskScoreService) {
        this.riskScoreService = riskScoreService;
    }

    @GetMapping
    public ApiResponse<RiskScoreSummaryResponse> list() {
        return ApiResponse.ok(riskScoreService.list());
    }
}
