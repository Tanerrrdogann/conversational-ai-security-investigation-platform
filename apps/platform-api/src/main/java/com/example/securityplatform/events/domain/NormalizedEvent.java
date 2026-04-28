package com.example.securityplatform.events.domain;

import com.example.securityplatform.common.Severity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("normalized_events")
public class NormalizedEvent implements Persistable<UUID> {

    @Id
    @Column("event_id")
    private UUID eventId;

    @Column("event_type")
    private EventType eventType;

    @Column("source_type")
    private SourceType sourceType;

    @Column("occurred_at")
    private Instant occurredAt;

    @Column("severity")
    private Severity severity;

    @Column("source_ip")
    private String sourceIp;

    @Column("destination_ip")
    private String destinationIp;

    @Column("destination_port")
    private Integer destinationPort;

    @Column("username")
    private String username;

    @Column("service_name")
    private String serviceName;

    @Column("endpoint")
    private String endpoint;

    @Column("raw_message")
    private String rawMessage;

    @Column("normalized_message")
    private String normalizedMessage;

    @Column("tags")
    private String tags;

    @Column("correlation_key")
    private String correlationKey;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public NormalizedEvent() {
        this.newAggregate = false;
    }

    public NormalizedEvent(
            UUID eventId,
            EventType eventType,
            SourceType sourceType,
            Instant occurredAt,
            Severity severity,
            String sourceIp,
            String destinationIp,
            Integer destinationPort,
            String username,
            String serviceName,
            String endpoint,
            String rawMessage,
            String normalizedMessage,
            String tags,
            String correlationKey,
            Instant createdAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.occurredAt = occurredAt;
        this.severity = severity;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.username = username;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.rawMessage = rawMessage;
        this.normalizedMessage = normalizedMessage;
        this.tags = tags;
        this.correlationKey = correlationKey;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public UUID getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getNormalizedMessage() {
        return normalizedMessage;
    }

    public void setNormalizedMessage(String normalizedMessage) {
        this.normalizedMessage = normalizedMessage;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
