package com.example.securityplatform.alerts;

import com.example.securityplatform.common.ApiResponse;
import com.example.securityplatform.investigation.InvestigationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AlertExplanationService alertExplanationService;

    public AlertController(AlertService alertService, AlertExplanationService alertExplanationService) {
        this.alertService = alertService;
        this.alertExplanationService = alertExplanationService;
    }

    @GetMapping
    public ApiResponse<List<AlertResponse>> listAlerts() {
        return ApiResponse.ok(alertService.list());
    }

    @GetMapping("/{alertId}/explanation")
    public ApiResponse<InvestigationResponse> explainAlert(
            @PathVariable String alertId,
            @RequestParam(defaultValue = "technical") String mode
    ) {
        return ApiResponse.ok(alertExplanationService.explainAlert(alertId, mode));
    }
}
