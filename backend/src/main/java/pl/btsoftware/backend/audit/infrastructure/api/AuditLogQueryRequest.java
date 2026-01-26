package pl.btsoftware.backend.audit.infrastructure.api;

import pl.btsoftware.backend.audit.domain.AuditEntityType;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditOperation;
import pl.btsoftware.backend.audit.domain.EntityId;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogQueryRequest(
        String entityType,
        UUID entityId,
        String operation,
        String performedBy,
        OffsetDateTime fromDate,
        OffsetDateTime toDate
) {
    public AuditLogQuery toDomain(GroupId groupId) {
        return new AuditLogQuery(
                groupId,
                entityType != null ? AuditEntityType.valueOf(entityType) : null,
                entityId != null ? EntityId.from(entityId) : null,
                operation != null ? AuditOperation.valueOf(operation) : null,
                performedBy != null ? new UserId(performedBy) : null,
                fromDate,
                toDate
        );
    }
}
