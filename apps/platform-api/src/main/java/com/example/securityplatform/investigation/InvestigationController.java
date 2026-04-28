package com.example.securityplatform.investigation;

import com.example.securityplatform.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @PostMapping("/messages")
    public ApiResponse<InvestigationResponse> investigate(@Valid @RequestBody ChatMessageRequest request) {
        return ApiResponse.ok(investigationService.investigate(request));
    }
}
