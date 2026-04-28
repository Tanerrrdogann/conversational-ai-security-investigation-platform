package com.example.securityplatform.events.service;

import com.example.securityplatform.events.domain.NormalizedEvent;
import com.example.securityplatform.events.dto.EventFilterRequest;
import com.example.securityplatform.events.dto.NormalizedEventRequest;
import com.example.securityplatform.events.dto.NormalizedEventResponse;
import com.example.securityplatform.events.repository.NormalizedEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final NormalizedEventRepository normalizedEventRepository;

    public EventService(NormalizedEventRepository normalizedEventRepository) {
        this.normalizedEventRepository = normalizedEventRepository;
    }

    public NormalizedEventResponse create(NormalizedEventRequest request) {
        var event = new NormalizedEvent(
                UUID.randomUUID(),
                request.eventType(),
                request.sourceType(),
                request.occurredAt(),
                request.severity(),
                request.sourceIp(),
                request.destinationIp(),
                request.destinationPort(),
                request.username(),
                request.serviceName(),
                request.endpoint(),
                request.rawMessage(),
                request.normalizedMessage(),
                "",
                request.correlationKey(),
                Instant.now()
        );

        return toResponse(normalizedEventRepository.save(event));
    }

    public List<NormalizedEventResponse> list(EventFilterRequest filter) {
        return normalizedEventRepository.findAll()
                .stream()
                .filter(event -> matchesFilter(event, filter))
                .map(this::toResponse)
                .toList();
    }

    private boolean matchesFilter(NormalizedEvent event, EventFilterRequest filter) {
        if (filter == null) {
            return true;
        }
        if (filter.eventType() != null && filter.eventType() != event.getEventType()) {
            return false;
        }
        if (filter.sourceType() != null && filter.sourceType() != event.getSourceType()) {
            return false;
        }
        if (filter.severity() != null && filter.severity() != event.getSeverity()) {
            return false;
        }
        if (filter.sourceIp() != null && !filter.sourceIp().equals(event.getSourceIp())) {
            return false;
        }
        if (filter.username() != null && !filter.username().equals(event.getUsername())) {
            return false;
        }
        if (filter.correlationKey() != null && !filter.correlationKey().equals(event.getCorrelationKey())) {
            return false;
        }
        if (filter.occurredAfter() != null && event.getOccurredAt().isBefore(filter.occurredAfter())) {
            return false;
        }
        if (filter.occurredBefore() != null && event.getOccurredAt().isAfter(filter.occurredBefore())) {
            return false;
        }
        return true;
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
