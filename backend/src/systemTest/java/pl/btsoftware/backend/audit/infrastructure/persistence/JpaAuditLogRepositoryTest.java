package pl.btsoftware.backend.audit.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import pl.btsoftware.backend.audit.domain.*;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SystemTest
public class JpaAuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogJpaRepository auditLogJpaRepository;

    @Test
    void shouldStoreAndRetrieveAuditLog() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");
        var entityId = EntityId.from(randomUUID());
        var auditLog = AuditLog.create(
                AuditOperation.CREATE,
                AuditEntityType.ACCOUNT,
                entityId,
                userId,
                groupId,
                "Account created: Test Account"
        );

        auditLogRepository.store(auditLog);
        var retrieved = auditLogRepository.findById(auditLog.id(), groupId);

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().id()).isEqualTo(auditLog.id());
        assertThat(retrieved.get().operation()).isEqualTo(AuditOperation.CREATE);
        assertThat(retrieved.get().entityType()).isEqualTo(AuditEntityType.ACCOUNT);
        assertThat(retrieved.get().entityId()).isEqualTo(entityId);
        assertThat(retrieved.get().performedBy()).isEqualTo(userId);
        assertThat(retrieved.get().groupId()).isEqualTo(groupId);
        assertThat(retrieved.get().changeDescription()).isEqualTo("Account created: Test Account");
    }

    @Test
    void shouldReturnEmptyWhenAuditLogNotFound() {
        var nonExistingId = AuditLogId.generate();
        var nonExistingGroup = new GroupId(randomUUID());

        var result = auditLogRepository.findById(nonExistingId, nonExistingGroup);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldEnforceGroupIsolation() {
        var group1 = new GroupId(randomUUID());
        var group2 = new GroupId(randomUUID());
        var userId = UserId.of("user123");
        var auditLog = AuditLog.create(
                AuditOperation.CREATE,
                AuditEntityType.ACCOUNT,
                EntityId.from(randomUUID()),
                userId,
                group1,
                "Test"
        );

        auditLogRepository.store(auditLog);
        var resultGroup1 = auditLogRepository.findById(auditLog.id(), group1);
        var resultGroup2 = auditLogRepository.findById(auditLog.id(), group2);

        assertThat(resultGroup1).isPresent();
        assertThat(resultGroup2).isEmpty();
    }

    @Test
    void shouldFindAllAuditLogsForGroup() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.UPDATE, AuditEntityType.TRANSACTION, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.DELETE, AuditEntityType.CATEGORY, userId, groupId));

        var query = AuditLogQuery.allForGroup(groupId);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldFilterByEntityType() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.TRANSACTION, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));

        var query = new AuditLogQuery(groupId, AuditEntityType.ACCOUNT, null, null, null, null, null);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(log -> log.entityType() == AuditEntityType.ACCOUNT);
    }

    @Test
    void shouldFilterByEntityId() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");
        var targetEntityId = EntityId.from(randomUUID());

        auditLogRepository.store(AuditLog.create(AuditOperation.CREATE, AuditEntityType.ACCOUNT, targetEntityId, userId, groupId, "Target"));
        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));

        var query = new AuditLogQuery(groupId, null, targetEntityId, null, null, null, null);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).entityId()).isEqualTo(targetEntityId);
    }

    @Test
    void shouldFilterByOperation() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.UPDATE, AuditEntityType.ACCOUNT, userId, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.DELETE, AuditEntityType.ACCOUNT, userId, groupId));

        var query = new AuditLogQuery(groupId, null, null, AuditOperation.UPDATE, null, null, null);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).operation()).isEqualTo(AuditOperation.UPDATE);
    }

    @Test
    void shouldFilterByPerformedBy() {
        var groupId = new GroupId(randomUUID());
        var user1 = UserId.of("user1");
        var user2 = UserId.of("user2");

        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, user1, groupId));
        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, user2, groupId));

        var query = new AuditLogQuery(groupId, null, null, null, user1, null, null);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).performedBy()).isEqualTo(user1);
    }

    @Test
    void shouldFilterByDateRange() throws InterruptedException {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        var beforeTime = OffsetDateTime.now().minusMinutes(1);
        auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));
        Thread.sleep(10);
        var afterTime = OffsetDateTime.now().plusMinutes(1);

        var query = new AuditLogQuery(groupId, null, null, null, null, beforeTime, afterTime);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldPaginateResults() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        for (int i = 0; i < 25; i++) {
            auditLogRepository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId));
        }

        var query = AuditLogQuery.allForGroup(groupId);
        var page0 = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));
        var page1 = auditLogRepository.findByQuery(query, PageRequest.of(1, 10));
        var page2 = auditLogRepository.findByQuery(query, PageRequest.of(2, 10));

        assertThat(page0.getContent()).hasSize(10);
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page0.getTotalElements()).isEqualTo(25);
        assertThat(page0.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldOrderByPerformedAtDescending() {
        var groupId = new GroupId(randomUUID());
        var userId = UserId.of("user123");

        var log1 = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT, userId, groupId);
        var log2 = createAuditLog(AuditOperation.UPDATE, AuditEntityType.ACCOUNT, userId, groupId);
        var log3 = createAuditLog(AuditOperation.DELETE, AuditEntityType.ACCOUNT, userId, groupId);

        auditLogRepository.store(log1);
        auditLogRepository.store(log2);
        auditLogRepository.store(log3);

        var query = AuditLogQuery.allForGroup(groupId);
        var result = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        var logs = result.getContent();
        assertThat(logs.get(0).performedAt()).isAfterOrEqualTo(logs.get(1).performedAt());
        assertThat(logs.get(1).performedAt()).isAfterOrEqualTo(logs.get(2).performedAt());
    }

    private AuditLog createAuditLog(AuditOperation operation, AuditEntityType entityType, UserId userId, GroupId groupId) {
        return AuditLog.create(
                operation,
                entityType,
                EntityId.from(randomUUID()),
                userId,
                groupId,
                "Test description"
        );
    }
}
