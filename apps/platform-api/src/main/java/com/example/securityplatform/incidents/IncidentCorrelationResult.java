package com.example.securityplatform.incidents;

import com.example.securityplatform.incidents.domain.Incident;

public record IncidentCorrelationResult(
        Incident incident,
        boolean created
) {
}
