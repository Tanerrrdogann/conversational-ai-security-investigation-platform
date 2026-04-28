package com.example.securityplatform.rules;

import com.example.securityplatform.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
public class RuleManagementController {

    private final RuleManagementService ruleManagementService;

    public RuleManagementController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }

    @GetMapping
    public ApiResponse<List<RuleManagementResponse>> listRules() {
        return ApiResponse.ok(ruleManagementService.list());
    }

    @PostMapping
    public ApiResponse<RuleManagementResponse> createRule(@Valid @RequestBody RuleManagementRequest request) {
        return ApiResponse.ok(ruleManagementService.create(request));
    }

    @PutMapping("/{ruleId}")
    public ApiResponse<RuleManagementResponse> updateRule(@PathVariable UUID ruleId, @Valid @RequestBody RuleManagementRequest request) {
        return ApiResponse.ok(ruleManagementService.update(ruleId, request));
    }
}
