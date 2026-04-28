package com.example.securityplatform.sources;

import com.example.securityplatform.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sources")
public class SourceManagementController {

    private final SourceManagementService sourceManagementService;

    public SourceManagementController(SourceManagementService sourceManagementService) {
        this.sourceManagementService = sourceManagementService;
    }

    @GetMapping
    public ApiResponse<List<SourceManagementResponse>> listSources() {
        return ApiResponse.ok(sourceManagementService.list());
    }

    @PostMapping
    public ApiResponse<SourceManagementResponse> createSource(@Valid @RequestBody SourceManagementRequest request) {
        return ApiResponse.ok(sourceManagementService.create(request));
    }

    @PutMapping("/{sourceId}")
    public ApiResponse<SourceManagementResponse> updateSource(@PathVariable UUID sourceId, @Valid @RequestBody SourceManagementRequest request) {
        return ApiResponse.ok(sourceManagementService.update(sourceId, request));
    }
}
