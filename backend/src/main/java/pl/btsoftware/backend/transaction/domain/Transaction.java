package pl.btsoftware.backend.transaction.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

public record Transaction(
        TransactionId id,
        AccountId accountId,
        TransactionType type,
        Bill bill,
        LocalDate transactionDate,
        TransactionHash transactionHash,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone) {
    public static Transaction create(
            AccountId accountId,
            TransactionType type,
            Bill bill,
            LocalDate transactionDate,
            TransactionHash transactionHash,
            AuditInfo createdInfo) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                type,
                bill,
                transactionDate,
                transactionHash,
                createdInfo,
                createdInfo,
                Tombstone.active());
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

    public Transaction updateBill(
            Bill newBill, AccountId newAccountId, LocalDate newTransactionDate, UserId updatedBy) {
        var finalAccountId = newAccountId != null ? newAccountId : accountId;
        var finalTransactionDate = newTransactionDate != null ? newTransactionDate : transactionDate;

        return new Transaction(
                id,
                finalAccountId,
                type,
                newBill,
                finalTransactionDate,
                transactionHash,
                createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(),
                tombstone);
    }

    public Transaction delete() {
        return new Transaction(
                id,
                accountId,
                type,
                bill,
                transactionDate,
                transactionHash,
                createdInfo,
                updatedInfo,
                Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }

    public String description() {
        if (bill.items().size() == 1) {
            return bill.items().getFirst().description();
        }
        return bill.items().stream()
                .map(item -> item.description() != null ? item.description() : "")
                .filter(desc -> !desc.isEmpty())
                .findFirst()
                .orElse(null);
    }

    public Money amount() {
        return bill.totalAmount();
    }
}
