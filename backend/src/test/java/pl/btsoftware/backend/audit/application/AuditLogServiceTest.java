package pl.btsoftware.backend.audit.application;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import pl.btsoftware.backend.audit.domain.*;
import pl.btsoftware.backend.audit.infrastructure.persistence.InMemoryAuditLogRepository;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

public class AuditLogServiceTest {
    private AuditLogRepository auditLogRepository;
    private AuditLogService auditLogService;
    private GroupId groupId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        this.auditLogRepository = new InMemoryAuditLogRepository();
        this.auditLogService = new AuditLogService(auditLogRepository);
        this.groupId = new GroupId(randomUUID());
        this.userId = UserId.of("user123");
    }

    @Nested
    class LogOperations {
        @Test
        void shouldLogCreateOperation() {
            var entityType = AuditEntityType.ACCOUNT;
            var entityId = EntityId.from(randomUUID());
            var description = "Account created: Main Account";

            var auditLog =
                    auditLogService.logCreate(entityType, entityId, userId, groupId, description);

            assertThat(auditLog.id()).isNotNull();
            assertThat(auditLog.operation()).isEqualTo(AuditOperation.CREATE);
            assertThat(auditLog.entityType()).isEqualTo(entityType);
            assertThat(auditLog.entityId()).isEqualTo(entityId);
            assertThat(auditLog.performedBy()).isEqualTo(userId);
            assertThat(auditLog.groupId()).isEqualTo(groupId);
            assertThat(auditLog.changeDescription()).isEqualTo(description);

            var retrieved = auditLogRepository.findById(auditLog.id(), groupId);
            assertThat(retrieved).isPresent();
        }

        @Test
        void shouldLogUpdateOperation() {
            var entityType = AuditEntityType.TRANSACTION;
            var entityId = EntityId.from(randomUUID());
            var description = "Transaction updated: amount changed";

            var auditLog =
                    auditLogService.logUpdate(entityType, entityId, userId, groupId, description);

            assertThat(auditLog.operation()).isEqualTo(AuditOperation.UPDATE);
            assertThat(auditLog.entityType()).isEqualTo(entityType);
            assertThat(auditLog.changeDescription()).isEqualTo(description);
        }

        @Test
        void shouldLogDeleteOperation() {
            var entityType = AuditEntityType.CATEGORY;
            var entityId = EntityId.from(randomUUID());
            var description = "Category deleted: Food";

            var auditLog =
                    auditLogService.logDelete(entityType, entityId, userId, groupId, description);

            assertThat(auditLog.operation()).isEqualTo(AuditOperation.DELETE);
            assertThat(auditLog.entityType()).isEqualTo(entityType);
            assertThat(auditLog.changeDescription()).isEqualTo(description);
        }

        @Test
        void shouldAllowNullDescription() {
            var auditLog =
                    auditLogService.logCreate(
                            AuditEntityType.ACCOUNT,
                            EntityId.from(randomUUID()),
                            userId,
                            groupId,
                            null);

            assertThat(auditLog.changeDescription()).isNull();
        }
    }

    @Nested
    class QueryOperations {
        @Test
        void shouldFindAuditLogById() {
            var entityId = EntityId.from(randomUUID());
            var auditLog =
                    auditLogService.logCreate(
                            AuditEntityType.ACCOUNT, entityId, userId, groupId, "Created");

            var retrieved = auditLogService.findById(auditLog.id(), groupId);

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get()).isEqualTo(auditLog);
        }

        @Test
        void shouldReturnEmptyWhenAuditLogNotFound() {
            var result = auditLogService.findById(AuditLogId.generate(), groupId);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldEnforceGroupIsolationOnFindById() {
            var auditLog =
                    auditLogService.logCreate(
                            AuditEntityType.ACCOUNT,
                            EntityId.from(randomUUID()),
                            userId,
                            groupId,
                            "Created");

            var differentGroup = new GroupId(randomUUID());
            var result = auditLogService.findById(auditLog.id(), differentGroup);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldQueryAllAuditLogsForGroup() {
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT, EntityId.from(randomUUID()), userId, groupId, "Log 1");
            auditLogService.logUpdate(
                    AuditEntityType.TRANSACTION,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Log 2");
            auditLogService.logDelete(
                    AuditEntityType.CATEGORY,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Log 3");

            var query = AuditLogQuery.allForGroup(groupId);
            var result = auditLogService.findByQuery(query, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        void shouldFilterByEntityType() {
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Account");
            auditLogService.logCreate(
                    AuditEntityType.TRANSACTION,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Transaction");

            var query =
                    new AuditLogQuery(
                            groupId, AuditEntityType.ACCOUNT, null, null, null, null, null);
            var result = auditLogService.findByQuery(query, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).entityType()).isEqualTo(AuditEntityType.ACCOUNT);
        }

        @Test
        void shouldFilterByEntityId() {
            var targetEntityId = EntityId.from(randomUUID());
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT, targetEntityId, userId, groupId, "Target");
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT, EntityId.from(randomUUID()), userId, groupId, "Other");

            var query = new AuditLogQuery(groupId, null, targetEntityId, null, null, null, null);
            var result = auditLogService.findByQuery(query, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).entityId()).isEqualTo(targetEntityId);
        }

        @Test
        void shouldFilterByOperation() {
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Create");
            auditLogService.logUpdate(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Update");
            auditLogService.logDelete(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    groupId,
                    "Delete");

            var query =
                    new AuditLogQuery(groupId, null, null, AuditOperation.UPDATE, null, null, null);
            var result = auditLogService.findByQuery(query, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).operation()).isEqualTo(AuditOperation.UPDATE);
        }

        @Test
        void shouldFilterByPerformedBy() {
            var user1 = UserId.of("user1");
            var user2 = UserId.of("user2");
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT, EntityId.from(randomUUID()), user1, groupId, "User1");
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT, EntityId.from(randomUUID()), user2, groupId, "User2");

            var query = new AuditLogQuery(groupId, null, null, null, user1, null, null);
            var result = auditLogService.findByQuery(query, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).performedBy()).isEqualTo(user1);
        }

        @Test
        void shouldPaginateResults() {
            for (int i = 0; i < 25; i++) {
                auditLogService.logCreate(
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        userId,
                        groupId,
                        "Log " + i);
            }

            var query = AuditLogQuery.allForGroup(groupId);
            var page0 = auditLogService.findByQuery(query, PageRequest.of(0, 10));
            var page1 = auditLogService.findByQuery(query, PageRequest.of(1, 10));

            assertThat(page0.getContent()).hasSize(10);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page0.getTotalElements()).isEqualTo(25);
            assertThat(page0.getTotalPages()).isEqualTo(3);
        }

        @Test
        void shouldIsolateAuditLogsByGroup() {
            var group1 = new GroupId(randomUUID());
            var group2 = new GroupId(randomUUID());
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    group1,
                    "Group 1");
            auditLogService.logCreate(
                    AuditEntityType.ACCOUNT,
                    EntityId.from(randomUUID()),
                    userId,
                    group2,
                    "Group 2");

            var query1 = AuditLogQuery.allForGroup(group1);
            var result1 = auditLogService.findByQuery(query1, PageRequest.of(0, 10));

            assertThat(result1.getContent()).hasSize(1);
            assertThat(result1.getContent().get(0).groupId()).isEqualTo(group1);
        }
    }
}
