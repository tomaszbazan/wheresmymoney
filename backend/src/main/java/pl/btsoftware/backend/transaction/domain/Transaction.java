package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

public record Transaction(
        TransactionId id,
        AccountId accountId,
        Money amount,
        TransactionType type,
        String description,
        String category,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone
) {
    public static Transaction create(
            AccountId accountId,
            Money amount,
            String description,
            TransactionType type,
            String category,
            AuditInfo createdInfo
    ) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                amount,
                type,
                description,
                category,
                createdInfo,
                createdInfo,
                Tombstone.active()
        );
    }

    public UserId createdBy() {
        return createdInfo.who();
    }

    public UserId lastUpdatedBy() {
        return updatedInfo.who();
    }

    public GroupId ownedBy() {
        return createdInfo.fromGroup();
    }

    public OffsetDateTime createdAt() {
        return createdInfo.when();
    }

    public OffsetDateTime lastUpdatedAt() {
        return updatedInfo.when();
    }

    public Transaction updateAmount(Money newAmount, UserId updatedBy) {
        return new Transaction(id, accountId, newAmount, type, description, category, createdInfo, new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction updateDescription(String newDescription, UserId updatedBy) {
        return new Transaction(id, accountId, amount, type, newDescription, category, createdInfo, new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction updateCategory(String newCategory, UserId updatedBy) {
        return new Transaction(id, accountId, amount, type, description, newCategory, createdInfo, new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction delete() {
        return new Transaction(id, accountId, amount, type, description, category, createdInfo, updatedInfo, Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }
}