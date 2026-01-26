package pl.btsoftware.backend.audit.domain;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

import java.time.OffsetDateTime;
import org.jetbrains.annotations.Nullable;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

public record AuditLog(
        AuditLogId id,
        AuditOperation operation,
        AuditEntityType entityType,
        EntityId entityId,
        UserId performedBy,
        GroupId groupId,
        OffsetDateTime performedAt,
        @Nullable String changeDescription) {
    public static AuditLog create(
            AuditOperation operation,
            AuditEntityType entityType,
            EntityId entityId,
            UserId performedBy,
            GroupId groupId,
            String changeDescription) {
        return new AuditLog(
                AuditLogId.generate(),
                operation,
                entityType,
                entityId,
                performedBy,
                groupId,
                now(UTC),
                changeDescription);
    }
}
