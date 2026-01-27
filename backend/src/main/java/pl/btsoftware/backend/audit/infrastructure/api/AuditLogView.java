package pl.btsoftware.backend.audit.infrastructure.api;

import java.time.OffsetDateTime;
import java.util.UUID;
import pl.btsoftware.backend.audit.domain.AuditLog;

public record AuditLogView(
        UUID id,
        String operation,
        String entityType,
        UUID entityId,
        String performedBy,
        UUID groupId,
        OffsetDateTime performedAt,
        String changeDescription) {
    public static AuditLogView from(AuditLog auditLog) {
        return new AuditLogView(
                auditLog.id().value(),
                auditLog.operation().name(),
                auditLog.entityType().name(),
                auditLog.entityId().value(),
                auditLog.performedBy().value(),
                auditLog.groupId().value(),
                auditLog.performedAt(),
                auditLog.changeDescription());
    }
}
