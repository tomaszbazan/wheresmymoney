package pl.btsoftware.backend.audit.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.audit.domain.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@Entity
@Table(name = "audit_log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuditLogEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private AuditOperation operation;

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private AuditEntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "performed_at")
    private OffsetDateTime performedAt;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    public static AuditLogEntity fromDomain(AuditLog auditLog) {
        return new AuditLogEntity(
                auditLog.id().value(),
                auditLog.operation(),
                auditLog.entityType(),
                auditLog.entityId().value(),
                auditLog.performedBy().value(),
                auditLog.groupId().value(),
                auditLog.performedAt(),
                auditLog.changeDescription());
    }

    public AuditLog toDomain() {
        return new AuditLog(
                AuditLogId.of(id),
                operation,
                entityType,
                EntityId.from(entityId),
                UserId.of(performedBy),
                new GroupId(groupId),
                performedAt,
                changeDescription);
    }
}
