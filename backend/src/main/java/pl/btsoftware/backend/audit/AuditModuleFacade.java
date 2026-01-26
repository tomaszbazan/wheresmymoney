package pl.btsoftware.backend.audit;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.audit.application.AuditLogService;
import pl.btsoftware.backend.audit.domain.EntityId;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import static pl.btsoftware.backend.audit.domain.AuditEntityType.*;

@Service
@AllArgsConstructor
public class AuditModuleFacade {
    private final AuditLogService auditLogService;

    public void logAccountCreated(AccountId accountId, String accountName, UserId userId, GroupId groupId) {
        var description = "Account created: " + accountName;
        auditLogService.logCreate(
                ACCOUNT,
                EntityId.from(accountId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logAccountUpdated(AccountId accountId, String oldName, String newName, UserId userId, GroupId groupId) {
        var description = "Account renamed: " + oldName + " → " + newName;
        auditLogService.logUpdate(
                ACCOUNT,
                EntityId.from(accountId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logAccountDeleted(AccountId accountId, String accountName, UserId userId, GroupId groupId) {
        var description = "Account deleted: " + accountName;
        auditLogService.logDelete(
                ACCOUNT,
                EntityId.from(accountId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logAccountWithdraw(AccountId accountId, String accountName, UserId userId, GroupId groupId, Money amount) {
        var description = "Account withdraw: " + accountName + ", amount: " + amount;
        auditLogService.logUpdate(
                ACCOUNT,
                EntityId.from(accountId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logAccountDeposit(AccountId accountId, String accountName, UserId userId, GroupId groupId, Money amount) {
        var description = "Account deposit: " + accountName + ", amount: " + amount;
        auditLogService.logUpdate(
                ACCOUNT,
                EntityId.from(accountId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logTransactionCreated(TransactionId transactionId, String description, UserId userId, GroupId groupId) {
        var logDescription = "Transaction created: " + description;
        auditLogService.logCreate(
                TRANSACTION,
                EntityId.from(transactionId.value()),
                userId,
                groupId,
                logDescription
        );
    }

    public void logTransactionUpdated(TransactionId transactionId, String description, UserId userId, GroupId groupId) {
        var logDescription = "Transaction updated: " + description;
        auditLogService.logUpdate(
                TRANSACTION,
                EntityId.from(transactionId.value()),
                userId,
                groupId,
                logDescription
        );
    }

    public void logTransactionDeleted(TransactionId transactionId, String description, UserId userId, GroupId groupId) {
        var logDescription = "Transaction deleted: " + description;
        auditLogService.logDelete(
                TRANSACTION,
                EntityId.from(transactionId.value()),
                userId,
                groupId,
                logDescription
        );
    }

    public void logCategoryCreated(CategoryId categoryId, String categoryName, String categoryType, UserId userId, GroupId groupId) {
        var description = "Category created: " + categoryName + " (" + categoryType + ")";
        auditLogService.logCreate(
                CATEGORY,
                EntityId.from(categoryId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logCategoryUpdated(CategoryId categoryId, String oldName, String newName, UserId userId, GroupId groupId) {
        var description = "Category updated: " + oldName + " → " + newName;
        auditLogService.logUpdate(
                CATEGORY,
                EntityId.from(categoryId.value()),
                userId,
                groupId,
                description
        );
    }

    public void logCategoryDeleted(CategoryId categoryId, String categoryName, UserId userId, GroupId groupId) {
        var description = "Category deleted: " + categoryName;
        auditLogService.logDelete(
                CATEGORY,
                EntityId.from(categoryId.value()),
                userId,
                groupId,
                description
        );
    }
}
