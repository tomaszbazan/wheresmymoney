package pl.btsoftware.backend.audit.domain;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class AuditLogTest {

    @Test
    void shouldCreateAuditLogWithAllFields() {
        var operation = AuditOperation.CREATE;
        var entityType = AuditEntityType.ACCOUNT;
        var entityId = EntityId.from(randomUUID());
        var performedBy = UserId.of("user123");
        var groupId = new GroupId(randomUUID());
        var description = "Account created: Main Account";

        var auditLog = AuditLog.create(operation, entityType, entityId, performedBy, groupId, description);

        assertThat(auditLog.id()).isNotNull();
        assertThat(auditLog.operation()).isEqualTo(operation);
        assertThat(auditLog.entityType()).isEqualTo(entityType);
        assertThat(auditLog.entityId()).isEqualTo(entityId);
        assertThat(auditLog.performedBy()).isEqualTo(performedBy);
        assertThat(auditLog.groupId()).isEqualTo(groupId);
        assertThat(auditLog.performedAt()).isNotNull();
        assertThat(auditLog.performedAt().getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(auditLog.changeDescription()).isEqualTo(description);
    }

    @Test
    void shouldGenerateUniqueIds() {
        var operation = AuditOperation.UPDATE;
        var entityType = AuditEntityType.TRANSACTION;
        var entityId = EntityId.from(randomUUID());
        var performedBy = UserId.of("user456");
        var groupId = new GroupId(randomUUID());

        var auditLog1 = AuditLog.create(operation, entityType, entityId, performedBy, groupId, "First log");
        var auditLog2 = AuditLog.create(operation, entityType, entityId, performedBy, groupId, "Second log");

        assertThat(auditLog1.id()).isNotEqualTo(auditLog2.id());
    }

    @Test
    void shouldCreateAuditLogForDeleteOperation() {
        var operation = AuditOperation.DELETE;
        var entityType = AuditEntityType.CATEGORY;
        var entityId = EntityId.from(randomUUID());
        var performedBy = UserId.of("user789");
        var groupId = new GroupId(randomUUID());
        var description = "Category deleted: Food";

        var auditLog = AuditLog.create(operation, entityType, entityId, performedBy, groupId, description);

        assertThat(auditLog.operation()).isEqualTo(AuditOperation.DELETE);
        assertThat(auditLog.entityType()).isEqualTo(AuditEntityType.CATEGORY);
        assertThat(auditLog.changeDescription()).isEqualTo(description);
    }

    @Test
    void shouldAllowNullChangeDescription() {
        var auditLog = AuditLog.create(
                AuditOperation.CREATE,
                AuditEntityType.ACCOUNT,
                EntityId.from(randomUUID()),
                UserId.of("user123"),
                new GroupId(randomUUID()),
                null);

        assertThat(auditLog.changeDescription()).isNull();
    }
}
