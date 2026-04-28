package com.example.securityplatform.events.service;

import com.example.securityplatform.events.domain.LogEvent;
import com.example.securityplatform.events.domain.RawIngestedEvent;
import com.example.securityplatform.events.domain.SourceType;
import com.example.securityplatform.events.domain.TrafficEvent;
import com.example.securityplatform.events.repository.LogEventRepository;
import com.example.securityplatform.events.repository.TrafficEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventMirrorService {

    private final TrafficEventRepository trafficEventRepository;
    private final LogEventRepository logEventRepository;

    public EventMirrorService(TrafficEventRepository trafficEventRepository, LogEventRepository logEventRepository) {
        this.trafficEventRepository = trafficEventRepository;
        this.logEventRepository = logEventRepository;
    }

    public void mirror(RawIngestedEvent rawEvent) {
        if (rawEvent.getSourceType() == SourceType.TRAFFIC) {
            trafficEventRepository.save(new TrafficEvent(
                    UUID.randomUUID(),
                    rawEvent.getIngestionId(),
                    rawEvent.getSourceType(),
                    rawEvent.getOccurredAt(),
                    rawEvent.getSourceIp(),
                    rawEvent.getDestinationIp(),
                    rawEvent.getDestinationPort(),
                    rawEvent.getServiceName(),
                    rawEvent.getEndpoint(),
                    rawEvent.getRawMessage(),
                    Instant.now()
            ));
            return;
        }

        logEventRepository.save(new LogEvent(
                UUID.randomUUID(),
                rawEvent.getIngestionId(),
                rawEvent.getSourceType(),
                rawEvent.getOccurredAt(),
                rawEvent.getSourceIp(),
                rawEvent.getDestinationIp(),
                rawEvent.getDestinationPort(),
                rawEvent.getUsername(),
                rawEvent.getServiceName(),
                rawEvent.getEndpoint(),
                rawEvent.getRawMessage(),
                Instant.now()
        ));
    }
}
