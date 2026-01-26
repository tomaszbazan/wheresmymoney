package pl.btsoftware.backend.audit.infrastructure.persistence;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import pl.btsoftware.backend.audit.domain.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class InMemoryAuditLogRepositoryTest {

    private InMemoryAuditLogRepository repository;
    private GroupId groupId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAuditLogRepository();
        groupId = new GroupId(randomUUID());
        userId = UserId.of("user123");
    }

    @Test
    void shouldStoreAndRetrieveAuditLog() {
        var auditLog =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        userId,
                        groupId,
                        "Account created");

        repository.store(auditLog);
        var retrieved = repository.findById(auditLog.id(), groupId);

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(auditLog);
    }

    @Test
    void shouldReturnEmptyWhenAuditLogNotFound() {
        var result = repository.findById(AuditLogId.generate(), groupId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenGroupIdDoesNotMatch() {
        var auditLog =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        userId,
                        groupId,
                        "Account created");
        repository.store(auditLog);

        var differentGroupId = new GroupId(randomUUID());
        var result = repository.findById(auditLog.id(), differentGroupId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllAuditLogsForGroup() {
        var auditLog1 = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT);
        var auditLog2 = createAuditLog(AuditOperation.UPDATE, AuditEntityType.TRANSACTION);
        repository.store(auditLog1);
        repository.store(auditLog2);

        var query = AuditLogQuery.allForGroup(groupId);
        var pageable = PageRequest.of(0, 10);
        var result = repository.findByQuery(query, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(auditLog1, auditLog2);
    }

    @Test
    void shouldFilterByEntityType() {
        var accountLog = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT);
        var transactionLog = createAuditLog(AuditOperation.CREATE, AuditEntityType.TRANSACTION);
        repository.store(accountLog);
        repository.store(transactionLog);

        var query =
                new AuditLogQuery(groupId, AuditEntityType.ACCOUNT, null, null, null, null, null);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(accountLog);
    }

    @Test
    void shouldFilterByEntityId() {
        var entityId = EntityId.from(randomUUID());
        var targetLog =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        entityId,
                        userId,
                        groupId,
                        "Target");
        var otherLog = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT);
        repository.store(targetLog);
        repository.store(otherLog);

        var query = new AuditLogQuery(groupId, null, entityId, null, null, null, null);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(targetLog);
    }

    @Test
    void shouldFilterByOperation() {
        var createLog = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT);
        var updateLog = createAuditLog(AuditOperation.UPDATE, AuditEntityType.ACCOUNT);
        var deleteLog = createAuditLog(AuditOperation.DELETE, AuditEntityType.ACCOUNT);
        repository.store(createLog);
        repository.store(updateLog);
        repository.store(deleteLog);

        var query = new AuditLogQuery(groupId, null, null, AuditOperation.UPDATE, null, null, null);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(updateLog);
    }

    @Test
    void shouldFilterByPerformedBy() {
        var user1 = UserId.of("user1");
        var user2 = UserId.of("user2");
        var log1 =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        user1,
                        groupId,
                        "Log 1");
        var log2 =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        user2,
                        groupId,
                        "Log 2");
        repository.store(log1);
        repository.store(log2);

        var query = new AuditLogQuery(groupId, null, null, null, user1, null, null);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(log1);
    }

    @Test
    void shouldFilterByDateRange() throws InterruptedException {
        var beforeTime = OffsetDateTime.now();
        Thread.sleep(10);
        var log1 = createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT);
        repository.store(log1);
        Thread.sleep(10);
        var afterTime = OffsetDateTime.now();

        var query = new AuditLogQuery(groupId, null, null, null, null, beforeTime, afterTime);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(log1);
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 25; i++) {
            repository.store(createAuditLog(AuditOperation.CREATE, AuditEntityType.ACCOUNT));
        }

        var query = AuditLogQuery.allForGroup(groupId);
        var page0 = repository.findByQuery(query, PageRequest.of(0, 10));
        var page1 = repository.findByQuery(query, PageRequest.of(1, 10));
        var page2 = repository.findByQuery(query, PageRequest.of(2, 10));

        assertThat(page0.getContent()).hasSize(10);
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page0.getTotalElements()).isEqualTo(25);
        assertThat(page0.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyPageWhenNoResults() {
        var query = AuditLogQuery.allForGroup(groupId);
        var result = repository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldIsolateByGroup() {
        var group1 = new GroupId(randomUUID());
        var group2 = new GroupId(randomUUID());
        var log1 =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        userId,
                        group1,
                        "Group 1");
        var log2 =
                AuditLog.create(
                        AuditOperation.CREATE,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(randomUUID()),
                        userId,
                        group2,
                        "Group 2");
        repository.store(log1);
        repository.store(log2);

        var query1 = AuditLogQuery.allForGroup(group1);
        var result1 = repository.findByQuery(query1, PageRequest.of(0, 10));

        assertThat(result1.getContent()).hasSize(1);
        assertThat(result1.getContent().get(0)).isEqualTo(log1);
    }

    private AuditLog createAuditLog(AuditOperation operation, AuditEntityType entityType) {
        return AuditLog.create(
                operation,
                entityType,
                EntityId.from(randomUUID()),
                userId,
                groupId,
                "Test description");
    }
}
