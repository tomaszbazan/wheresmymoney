package pl.btsoftware.backend.account.domain;

import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;
import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

public record AuditInfo(UserId who, GroupId fromGroup, OffsetDateTime when) {

    public static AuditInfo create(String userId, UUID groupId) {
        return new AuditInfo(new UserId(userId), new GroupId(groupId), now(UTC));
    }

    public static AuditInfo create(String userId, UUID groupId, OffsetDateTime when) {
        return new AuditInfo(new UserId(userId), new GroupId(groupId), when);
    }

    public AuditInfo updateTimestamp() {
        return new AuditInfo(who, fromGroup, now(UTC));
    }
}