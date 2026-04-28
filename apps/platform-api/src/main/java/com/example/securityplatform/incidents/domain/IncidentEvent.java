package com.example.securityplatform.incidents.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("incident_events")
public class IncidentEvent implements Persistable<UUID> {

    @Id
    @Column("incident_event_id")
    private UUID incidentEventId;

    @Column("incident_id")
    private UUID incidentId;

    @Column("event_id")
    private UUID eventId;

    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean newAggregate;

    public IncidentEvent() {
        this.newAggregate = false;
    }

    public IncidentEvent(UUID incidentEventId, UUID incidentId, UUID eventId, Instant createdAt) {
        this.incidentEventId = incidentEventId;
        this.incidentId = incidentId;
        this.eventId = eventId;
        this.createdAt = createdAt;
        this.newAggregate = true;
    }

    public UUID getIncidentEventId() {
        return incidentEventId;
    }

    @Override
    public UUID getId() {
        return incidentEventId;
    }

    @Override
    public boolean isNew() {
        return newAggregate;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
