package com.example.securityplatform.events.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("traffic_events")
public class TrafficEvent implements Persistable<UUID> {

    @Id
    @Column("traffic_event_id")
    private UUID trafficEventId;

    @Column("ingestion_id")
    private UUID ingestionId;

    @Column("source_type")
    private SourceType sourceType;

    @Column("occurred_at")
    private Instant occurredAt;

    @Column("source_ip")
    private String sourceIp;

    @Column("destination_ip")
    private String destinationIp;

    @Column("destination_port")
    private Integer destinationPort;

    @Column("service_name")
    private String serviceName;

    @Column("endpoint")
    private String endpoint;

    @Column("raw_message")
    private String rawMessage;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public TrafficEvent() {
        this.newAggregate = false;
    }

    public TrafficEvent(UUID trafficEventId, UUID ingestionId, SourceType sourceType, Instant occurredAt, String sourceIp,
                        String destinationIp, Integer destinationPort, String serviceName, String endpoint,
                        String rawMessage, Instant createdAt) {
        this.trafficEventId = trafficEventId;
        this.ingestionId = ingestionId;
        this.sourceType = sourceType;
        this.occurredAt = occurredAt;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.rawMessage = rawMessage;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getTrafficEventId() {
        return trafficEventId;
    }

    @Override
    public UUID getId() {
        return trafficEventId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public UUID getIngestionId() {
        return ingestionId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
