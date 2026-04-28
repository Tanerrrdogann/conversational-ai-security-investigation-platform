package com.example.securityplatform.events.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("raw_ingested_events")
public class RawIngestedEvent implements Persistable<UUID> {

    @Id
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

    @Column("username")
    private String username;

    @Column("service_name")
    private String serviceName;

    @Column("endpoint")
    private String endpoint;

    @Column("raw_message")
    private String rawMessage;

    @Column("processing_status")
    private ProcessingStatus processingStatus;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public RawIngestedEvent() {
        this.newAggregate = false;
    }

    public RawIngestedEvent(
            UUID ingestionId,
            SourceType sourceType,
            Instant occurredAt,
            String sourceIp,
            String destinationIp,
            Integer destinationPort,
            String username,
            String serviceName,
            String endpoint,
            String rawMessage,
            ProcessingStatus processingStatus,
            Instant createdAt
    ) {
        this.ingestionId = ingestionId;
        this.sourceType = sourceType;
        this.occurredAt = occurredAt;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.destinationPort = destinationPort;
        this.username = username;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.rawMessage = rawMessage;
        this.processingStatus = processingStatus;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getIngestionId() {
        return ingestionId;
    }

    @Override
    public UUID getId() {
        return ingestionId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public void markPersisted() {
        this.newAggregate = false;
    }

    public void setIngestionId(UUID ingestionId) {
        this.ingestionId = ingestionId;
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

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
