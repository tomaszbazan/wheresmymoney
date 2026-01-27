package pl.btsoftware.backend.audit.domain;

import java.time.OffsetDateTime;
import org.jetbrains.annotations.Nullable;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

public record AuditLogQuery(
        GroupId groupId,
        @Nullable AuditEntityType entityType,
        @Nullable EntityId entityId,
        @Nullable AuditOperation operation,
        @Nullable UserId performedBy,
        @Nullable OffsetDateTime fromDate,
        @Nullable OffsetDateTime toDate) {
    public static AuditLogQuery allForGroup(GroupId groupId) {
        return new AuditLogQuery(groupId, null, null, null, null, null, null);
    }
}
