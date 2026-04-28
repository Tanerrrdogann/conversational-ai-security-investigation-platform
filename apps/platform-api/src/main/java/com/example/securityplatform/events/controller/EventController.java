package com.example.securityplatform.events.controller;

import com.example.securityplatform.common.ApiResponse;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.dto.EventFilterRequest;
import com.example.securityplatform.events.dto.NormalizedEventRequest;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ApiResponse<NormalizedEventResponse> ingestEvent(@Valid @RequestBody NormalizedEventRequest request) {
        return ApiResponse.ok(eventService.create(request));
    }

    @GetMapping
    public ApiResponse<List<NormalizedEventResponse>> listEvents(
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) SourceType sourceType,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String sourceIp,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String correlationKey,
            @RequestParam(required = false) Instant occurredAfter,
            @RequestParam(required = false) Instant occurredBefore
    ) {
        return ApiResponse.ok(eventService.list(new EventFilterRequest(
                eventType,
                sourceType,
                severity,
                sourceIp,
                username,
                correlationKey,
                occurredAfter,
                occurredBefore
        )));
    }
}
