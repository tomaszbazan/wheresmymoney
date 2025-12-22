package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionTooLongException;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Transaction(
        TransactionId id,
        AccountId accountId,
        Money amount,
        TransactionType type,
        String description,
        CategoryId categoryId,
        LocalDate transactionDate,
        TransactionHash transactionHash,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone
) {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    public Transaction(TransactionId id, AccountId accountId, Money amount, TransactionType type, String description,
                       CategoryId categoryId, LocalDate transactionDate, TransactionHash transactionHash,
                       AuditInfo createdInfo, AuditInfo updatedInfo, Tombstone tombstone) {
        validateDescriptionLength(description);
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.description = description != null ? description.trim() : null;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
        this.transactionHash = transactionHash;
        this.createdInfo = createdInfo;
        this.updatedInfo = updatedInfo;
        this.tombstone = tombstone;
    }

    public static Transaction create(
            AccountId accountId,
            Money amount,
            String description,
            TransactionType type,
            CategoryId categoryId,
            LocalDate transactionDate,
            TransactionHash transactionHash,
            AuditInfo createdInfo
    ) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                amount,
                type,
                description,
                categoryId,
                transactionDate,
                transactionHash,
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
        return new Transaction(id, accountId, newAmount, type, description, categoryId, transactionDate, transactionHash, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction updateDescription(String newDescription, UserId updatedBy) {
        return new Transaction(id, accountId, amount, type, newDescription, categoryId, transactionDate, transactionHash, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction updateCategory(CategoryId newCategoryId, UserId updatedBy) {
        return new Transaction(id, accountId, amount, type, description, newCategoryId, transactionDate, transactionHash, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Transaction delete() {
        return new Transaction(id, accountId, amount, type, description, categoryId, transactionDate, transactionHash, createdInfo, updatedInfo, Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }

    private void validateDescriptionLength(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new TransactionDescriptionTooLongException();
        }
    }
}
