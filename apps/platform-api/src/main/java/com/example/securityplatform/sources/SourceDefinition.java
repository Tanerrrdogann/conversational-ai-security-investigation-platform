package com.example.securityplatform.sources;

import com.example.securityplatform.events.domain.SourceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("sources")
public class SourceDefinition {
    @Id
    @Column("source_id")
    private UUID sourceId;
    @Column("name")
    private String name;
    @Column("source_type")
    private SourceType sourceType;
    @Column("ingestion_method")
    private String ingestionMethod;
    @Column("description")
    private String description;
    @Column("enabled")
    private Boolean enabled;
    @Column("created_at")
    private Instant createdAt;

    public SourceDefinition() {}

    public SourceDefinition(UUID sourceId, String name, SourceType sourceType, String ingestionMethod, String description, Boolean enabled, Instant createdAt) {
        this.sourceId = sourceId;
        this.name = name;
        this.sourceType = sourceType;
        this.ingestionMethod = ingestionMethod;
        this.description = description;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getIngestionMethod() { return ingestionMethod; }
    public void setIngestionMethod(String ingestionMethod) { this.ingestionMethod = ingestionMethod; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
