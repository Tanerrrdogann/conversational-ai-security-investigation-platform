package com.example.securityplatform.events.controller;

import com.example.securityplatform.common.ApiResponse;
import com.example.securityplatform.events.dto.IngestionResponse;
import com.example.securityplatform.events.dto.RawEventIngestionRequest;
import com.example.securityplatform.events.dto.ScenarioUploadPayload;
import com.example.securityplatform.events.service.EventIngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final EventIngestionService eventIngestionService;
    private final ObjectMapper objectMapper;

    public IngestionController(EventIngestionService eventIngestionService, ObjectMapper objectMapper) {
        this.eventIngestionService = eventIngestionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/events")
    public ApiResponse<IngestionResponse> ingestEvent(@Valid @RequestBody RawEventIngestionRequest request) {
        return ApiResponse.ok(eventIngestionService.ingest(request));
    }

    @PostMapping("/events/queue")
    public ApiResponse<List<IngestionResponse>> ingestQueuedEvents(@Valid @RequestBody List<RawEventIngestionRequest> requests) {
        return ApiResponse.ok(eventIngestionService.ingestBatch(requests));
    }

    @PostMapping(path = "/events/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<IngestionResponse>> uploadScenarioFile(@RequestParam("file") MultipartFile file) throws IOException {
        ScenarioUploadPayload payload = objectMapper.readValue(file.getInputStream(), ScenarioUploadPayload.class);
        return ApiResponse.ok(eventIngestionService.ingestBatch(payload.events()));
    }
}
