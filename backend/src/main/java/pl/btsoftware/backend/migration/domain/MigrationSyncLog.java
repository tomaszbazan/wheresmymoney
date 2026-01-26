package pl.btsoftware.backend.migration.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "migration_sync_log")
@Getter
@Setter
@NoArgsConstructor
public class MigrationSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "old_id")
    private Integer oldId;

    @Column(name = "new_id")
    private UUID newId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processing_time")
    private Integer processingTime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public MigrationSyncLog(String entityType, String operation, Integer oldId, UUID newId, String status) {
        this.entityType = entityType;
        this.operation = operation;
        this.oldId = oldId;
        this.newId = newId;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
