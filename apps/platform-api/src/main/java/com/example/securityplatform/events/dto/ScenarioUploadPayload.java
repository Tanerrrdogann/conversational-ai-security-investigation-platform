package com.example.securityplatform.events.dto;

import java.util.List;

public record ScenarioUploadPayload(
        String name,
        String description,
        List<RawEventIngestionRequest> events
) {
}
