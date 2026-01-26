package pl.btsoftware.backend.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import pl.btsoftware.backend.audit.application.AuditLogService;
import pl.btsoftware.backend.audit.domain.AuditEntityType;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditLogRepository;
import pl.btsoftware.backend.audit.domain.AuditOperation;
import pl.btsoftware.backend.audit.infrastructure.persistence.InMemoryAuditLogRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class AuditModuleFacadeTest {

    private AuditLogRepository auditLogRepository;
    private AuditLogService auditLogService;
    private AuditModuleFacade auditModuleFacade;
    private GroupId groupId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        this.auditLogRepository = new InMemoryAuditLogRepository();
        this.auditLogService = new AuditLogService(auditLogRepository);
        this.auditModuleFacade = new AuditModuleFacade(auditLogService);
        this.groupId = new GroupId(randomUUID());
        this.userId = UserId.of("user123");
    }

    @Nested
    class AccountAuditLogs {
        @Test
        void shouldLogAccountCreated() {
            var accountId = AccountId.generate();
            var accountName = "Main Account";

            auditModuleFacade.logAccountCreated(accountId, accountName, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.CREATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.ACCOUNT);
            assertThat(log.entityId().value()).isEqualTo(accountId.value());
            assertThat(log.performedBy()).isEqualTo(userId);
            assertThat(log.groupId()).isEqualTo(groupId);
            assertThat(log.changeDescription()).isEqualTo("Account created: Main Account");
        }

        @Test
        void shouldLogAccountUpdated() {
            var accountId = AccountId.generate();
            var oldName = "Old Name";
            var newName = "New Name";

            auditModuleFacade.logAccountUpdated(accountId, oldName, newName, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.UPDATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.ACCOUNT);
            assertThat(log.changeDescription()).isEqualTo("Account renamed: Old Name → New Name");
        }

        @Test
        void shouldLogAccountDeleted() {
            var accountId = AccountId.generate();
            var accountName = "Deleted Account";

            auditModuleFacade.logAccountDeleted(accountId, accountName, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.DELETE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.ACCOUNT);
            assertThat(log.changeDescription()).isEqualTo("Account deleted: Deleted Account");
        }
    }

    @Nested
    class TransactionAuditLogs {
        @Test
        void shouldLogTransactionCreated() {
            var transactionId = TransactionId.generate();
            var description = "Grocery shopping";

            auditModuleFacade.logTransactionCreated(transactionId, description, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.CREATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
            assertThat(log.entityId().value()).isEqualTo(transactionId.value());
            assertThat(log.changeDescription()).isEqualTo("Transaction created: Grocery shopping");
        }

        @Test
        void shouldLogTransactionUpdated() {
            var transactionId = TransactionId.generate();
            var description = "Transaction updated";

            auditModuleFacade.logTransactionUpdated(transactionId, description, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.UPDATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
            assertThat(log.changeDescription()).isEqualTo("Transaction updated: Transaction updated");
        }

        @Test
        void shouldLogTransactionDeleted() {
            var transactionId = TransactionId.generate();
            var description = "Deleted transaction";

            auditModuleFacade.logTransactionDeleted(transactionId, description, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.DELETE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
            assertThat(log.changeDescription()).isEqualTo("Transaction deleted: Deleted transaction");
        }
    }

    @Nested
    class CategoryAuditLogs {
        @Test
        void shouldLogCategoryCreated() {
            var categoryId = CategoryId.generate();
            var categoryName = "Food";
            var categoryType = "EXPENSE";

            auditModuleFacade.logCategoryCreated(categoryId, categoryName, categoryType, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.CREATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.CATEGORY);
            assertThat(log.entityId().value()).isEqualTo(categoryId.value());
            assertThat(log.changeDescription()).isEqualTo("Category created: Food (EXPENSE)");
        }

        @Test
        void shouldLogCategoryUpdated() {
            var categoryId = CategoryId.generate();
            var oldName = "Food";
            var newName = "Groceries";

            auditModuleFacade.logCategoryUpdated(categoryId, oldName, newName, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.UPDATE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.CATEGORY);
            assertThat(log.changeDescription()).isEqualTo("Category updated: Food → Groceries");
        }

        @Test
        void shouldLogCategoryDeleted() {
            var categoryId = CategoryId.generate();
            var categoryName = "Utilities";

            auditModuleFacade.logCategoryDeleted(categoryId, categoryName, userId, groupId);

            var query = AuditLogQuery.allForGroup(groupId);
            var logs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

            assertThat(logs.getContent()).hasSize(1);
            var log = logs.getContent().get(0);
            assertThat(log.operation()).isEqualTo(AuditOperation.DELETE);
            assertThat(log.entityType()).isEqualTo(AuditEntityType.CATEGORY);
            assertThat(log.changeDescription()).isEqualTo("Category deleted: Utilities");
        }
    }
}
