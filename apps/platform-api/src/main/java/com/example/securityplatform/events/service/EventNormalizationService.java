package com.example.securityplatform.events.service;

import com.example.securityplatform.analysis.EventAnalysisContext;
import com.example.securityplatform.common.Severity;
import com.example.securityplatform.events.domain.EventType;
import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventNormalizationService {

    private final NormalizedEventRepository normalizedEventRepository;

    public EventNormalizationService(NormalizedEventRepository normalizedEventRepository) {
        this.normalizedEventRepository = normalizedEventRepository;
    }

    public NormalizedEventResponse normalize(RawIngestedEvent rawEvent) {
        return normalize(rawEvent, EventAnalysisContext.empty());
    }

    public NormalizedEventResponse normalize(RawIngestedEvent rawEvent, EventAnalysisContext analysisContext) {
        var eventType = detectEventType(rawEvent, analysisContext);
        var severity = detectSeverity(eventType, analysisContext);
        var normalizedMessage = buildNormalizedMessage(rawEvent, eventType, analysisContext);
        var tags = buildTags(rawEvent, eventType, analysisContext);
        var correlationKey = buildCorrelationKey(rawEvent);

        var normalizedEvent = new NormalizedEvent(
                UUID.randomUUID(),
                eventType,
                rawEvent.getSourceType(),
                rawEvent.getOccurredAt(),
                severity,
                rawEvent.getSourceIp(),
                rawEvent.getDestinationIp(),
                rawEvent.getDestinationPort(),
                rawEvent.getUsername(),
                rawEvent.getServiceName(),
                rawEvent.getEndpoint(),
                rawEvent.getRawMessage(),
                normalizedMessage,
                tags,
                correlationKey,
                Instant.now()
        );

        return toResponse(normalizedEventRepository.save(normalizedEvent));
    }

    private EventType detectEventType(RawIngestedEvent rawEvent, EventAnalysisContext analysisContext) {
        var message = rawEvent.getRawMessage().toLowerCase(Locale.ROOT);

        if (analysisContext.blacklistIndicator()) {
            return EventType.BLACKLIST_IP_HIT;
        }
        if (analysisContext.privilegedAction()) {
            return EventType.PRIVILEGED_ACTION;
        }
        if (analysisContext.scanLikeTraffic()) {
            return EventType.PORT_SCAN_ATTEMPT;
        }
        if (analysisContext.bruteForceLike()) {
            return EventType.FAILED_LOGIN;
        }
        if (analysisContext.adminTargeting()) {
            return EventType.SUSPICIOUS_ADMIN_ACCESS;
        }
        if (analysisContext.deniedAccessPattern()) {
            return EventType.ACCESS_DENIED_STORM;
        }
        if (analysisContext.high404Pattern()) {
            return EventType.HIGH_404_RATE;
        }
        if (analysisContext.errorSpikePattern()) {
            return EventType.ERROR_SPIKE;
        }

        if (message.contains("blacklist")) {
            return EventType.BLACKLIST_IP_HIT;
        }
        if (message.contains("port scan") || message.contains("scan")) {
            return EventType.PORT_SCAN_ATTEMPT;
        }
        if (message.contains("failed login") || message.contains("login failed") || message.contains("brute")) {
            return EventType.FAILED_LOGIN;
        }
        if (message.contains("admin") || message.contains("privileged")) {
            return EventType.SUSPICIOUS_ADMIN_ACCESS;
        }
        if (message.contains("404")) {
            return EventType.HIGH_404_RATE;
        }
        if (message.contains("denied")) {
            return EventType.ACCESS_DENIED_STORM;
        }
        if (message.contains("error")) {
            return EventType.ERROR_SPIKE;
        }
        return switch (rawEvent.getSourceType()) {
            case TRAFFIC -> EventType.PORT_SCAN_ATTEMPT;
            case AUTH_LOG -> EventType.FAILED_LOGIN;
            case ACCESS_LOG -> EventType.HIGH_404_RATE;
            case APP_LOG -> EventType.ERROR_SPIKE;
            case SYSTEM_EVENT -> EventType.PRIVILEGED_ACTION;
            case SIMULATED_ATTACK -> EventType.SUSPICIOUS_ADMIN_ACCESS;
        };
    }

    private Severity detectSeverity(EventType eventType, EventAnalysisContext analysisContext) {
        if (analysisContext.blacklistIndicator()) {
            return Severity.CRITICAL;
        }
        if (analysisContext.privilegedAction() || analysisContext.scanLikeTraffic() || analysisContext.bruteForceLike()) {
            return Severity.HIGH;
        }
        return switch (eventType) {
            case BLACKLIST_IP_HIT -> Severity.CRITICAL;
            case PORT_SCAN_ATTEMPT, FAILED_LOGIN, SUSPICIOUS_ADMIN_ACCESS -> Severity.HIGH;
            case ERROR_SPIKE, ACCESS_DENIED_STORM -> Severity.MEDIUM;
            case PRIVILEGED_ACTION, HIGH_404_RATE -> Severity.MEDIUM;
        };
    }

    private String buildNormalizedMessage(RawIngestedEvent rawEvent, EventType eventType, EventAnalysisContext analysisContext) {
        String baseMessage = switch (eventType) {
            case FAILED_LOGIN -> "Detected failed authentication activity from source " + defaultValue(rawEvent.getSourceIp(), "unknown-source");
            case PORT_SCAN_ATTEMPT -> "Detected scanning-like traffic targeting port " + defaultValue(rawEvent.getDestinationPort(), "unknown-port");
            case BLACKLIST_IP_HIT -> "Detected access from a blacklisted actor " + defaultValue(rawEvent.getSourceIp(), "unknown-source");
            case ACCESS_DENIED_STORM -> "Detected repeated denied access behavior in incoming raw event";
            case SUSPICIOUS_ADMIN_ACCESS -> "Detected suspicious admin or privileged access activity";
            case ERROR_SPIKE -> "Detected application or service error spike pattern";
            case PRIVILEGED_ACTION -> "Detected privileged action that should be investigated";
            case HIGH_404_RATE -> "Detected elevated 404 or missing resource activity";
        };

        if (analysisContext.findings().isEmpty()) {
            return baseMessage;
        }

        return baseMessage + ". Analysis findings: " + String.join(" ", analysisContext.findings());
    }

    private String buildCorrelationKey(RawIngestedEvent rawEvent) {
        if (rawEvent.getUsername() != null && !rawEvent.getUsername().isBlank()) {
            return "user:" + rawEvent.getUsername();
        }
        if (rawEvent.getSourceIp() != null && !rawEvent.getSourceIp().isBlank()) {
            return "ip:" + rawEvent.getSourceIp();
        }
        return "source:" + rawEvent.getSourceType().name().toLowerCase(Locale.ROOT);
    }

    private String buildTags(RawIngestedEvent rawEvent, EventType eventType, EventAnalysisContext analysisContext) {
        return java.util.stream.Stream.of(
                        "event:" + eventType.name().toLowerCase(Locale.ROOT),
                        "source:" + rawEvent.getSourceType().name().toLowerCase(Locale.ROOT),
                        analysisContext.scanLikeTraffic() ? "scan-like" : null,
                        analysisContext.bruteForceLike() ? "brute-force" : null,
                        analysisContext.blacklistIndicator() ? "blacklist" : null,
                        analysisContext.adminTargeting() ? "admin-targeting" : null,
                        analysisContext.deniedAccessPattern() ? "denied-access" : null,
                        analysisContext.errorSpikePattern() ? "error-spike" : null,
                        analysisContext.high404Pattern() ? "404-probing" : null,
                        analysisContext.privilegedAction() ? "privileged-action" : null
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(","));
    }

    private String defaultValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private NormalizedEventResponse toResponse(NormalizedEvent event) {
        return new NormalizedEventResponse(
                event.getEventId(),
                event.getEventType(),
                event.getSourceType(),
                event.getOccurredAt(),
                event.getSeverity(),
                event.getSourceIp(),
                event.getDestinationIp(),
                event.getDestinationPort(),
                event.getUsername(),
                event.getServiceName(),
                event.getEndpoint(),
                event.getRawMessage(),
                event.getNormalizedMessage(),
                event.getTags(),
                event.getCorrelationKey(),
                event.getCreatedAt()
        );
    }
}
