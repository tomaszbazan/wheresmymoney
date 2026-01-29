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
        Money amount,
        TransactionType type,
        Bill bill,
        LocalDate transactionDate,
        TransactionHash transactionHash,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone) {
    public Transaction(
            TransactionId id,
            AccountId accountId,
            Money amount,
            TransactionType type,
            Bill bill,
            LocalDate transactionDate,
            TransactionHash transactionHash,
            AuditInfo createdInfo,
            AuditInfo updatedInfo,
            Tombstone tombstone) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.bill = bill;
        this.transactionDate = transactionDate;
        this.transactionHash = transactionHash;
        this.createdInfo = createdInfo;
        this.updatedInfo = updatedInfo;
        this.tombstone = tombstone;
    }

    public static Transaction create(
            AccountId accountId,
            Money amount,
            TransactionType type,
            Bill bill,
            LocalDate transactionDate,
            TransactionHash transactionHash,
            AuditInfo createdInfo) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                amount,
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

    public Transaction updateAmount(Money newAmount, UserId updatedBy) {
        return new Transaction(
                id,
                accountId,
                newAmount,
                type,
                bill,
                transactionDate,
                transactionHash,
                createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when())
                        .updateTimestamp(),
                tombstone);
    }

    public Transaction updateBill(Bill newBill, UserId updatedBy) {
        return new Transaction(
                id,
                accountId,
                amount,
                type,
                newBill,
                transactionDate,
                transactionHash,
                createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when())
                        .updateTimestamp(),
                tombstone);
    }

    public Transaction delete() {
        return new Transaction(
                id,
                accountId,
                amount,
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
}
