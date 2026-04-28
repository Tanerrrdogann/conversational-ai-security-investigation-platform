package com.example.securityplatform.risk;

import java.util.List;

public record RiskScoreSummaryResponse(
        List<HostRiskScoreResponse> hosts,
        List<UserRiskScoreResponse> users
) {
}
