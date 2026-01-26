package pl.btsoftware.backend.migration.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "migration_entity_mapping")
@Getter
@Setter
@NoArgsConstructor
public class MigrationEntityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "old_id")
    private Integer oldId;

    @Column(name = "old_name", length = 200)
    private String oldName;

    @Column(name = "new_id", nullable = false)
    private UUID newId;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public MigrationEntityMapping(String entityType, Integer oldId, String oldName, UUID newId) {
        this.entityType = entityType;
        this.oldId = oldId;
        this.oldName = oldName;
        this.newId = newId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
