package pl.btsoftware.backend.account.domain;

import jakarta.annotation.Nullable;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.infrastructure.api.UserView;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static pl.btsoftware.backend.shared.Currency.DEFAULT;
import static pl.btsoftware.backend.shared.Money.zero;

public record Account(AccountId id, String name, Money balance, List<TransactionId> transactionIds,
                      AuditInfo createdInfo, AuditInfo updatedInfo) {
    public Account {
        validateAccountName(name);
    }

    public Account(AccountId id, String name, @Nullable Currency currency, UserView createdBy) {
        this(id, name, zero(currency == null ? DEFAULT : currency), new ArrayList<>(),
                AuditInfo.create(createdBy.id(), createdBy.groupId()), AuditInfo.create(createdBy.id(), createdBy.groupId()));
    }

    public Account(AccountId id, String name, Money balance, AuditInfo createBy) {
        this(id, name, balance, new ArrayList<>(), createBy, createBy.updateTimestamp());
    }

    private static void validateAccountName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new AccountNameEmptyException();
        }
        if (newName.length() > 100) {
            throw new AccountNameTooLongException();
        }
        if (!newName.matches("^[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$")) {
            throw new AccountNameInvalidCharactersException();
        }
    }

    public Account changeName(String newName) {
        validateAccountName(newName);
        return new Account(id, newName, balance, transactionIds, createdInfo, updatedInfo.updateTimestamp());
    }

    public boolean hasAnyTransaction() {
        return !transactionIds.isEmpty();
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

    public Account addTransaction(TransactionId transactionId, Money amount, TransactionType transactionType) {
        if (balance().currency() != amount.currency()) {
            throw new IllegalArgumentException("Transaction currency must match account currency");
        }
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(amount).addTransactionId(transactionId);
            }
            case EXPENSE -> {
                return updateBalance(amount.negate()).addTransactionId(transactionId);
            }
        }
        return this;
    }

    public Account removeTransaction(TransactionId transactionId, Money amount, TransactionType transactionType) {
        if (balance().currency() != amount.currency()) {
            throw new IllegalArgumentException("Transaction currency must match account currency");
        }
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(amount.negate()).removeTransactionId(transactionId);
            }
            case EXPENSE -> {
                return updateBalance(amount).removeTransactionId(transactionId);
            }
        }
        return this;
    }

    public Account changeTransaction(TransactionId transactionId, Money oldAmount, Money newAmount, TransactionType transactionType) {
        if (balance().currency() != oldAmount.currency() && balance().currency() != newAmount.currency()) {
            throw new IllegalArgumentException("Transaction currency must match account currency");
        }
        if (!transactionIds.contains(transactionId)) {
            throw new IllegalArgumentException("Transaction ID not found in account");
        }
        var balanceChange = newAmount.subtract(oldAmount);
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(balanceChange).removeTransactionId(transactionId).addTransactionId(transactionId);
            }
            case EXPENSE -> {
                return updateBalance(balanceChange.negate()).removeTransactionId(transactionId).addTransactionId(transactionId);
            }
        }
        return this;
    }

    private Account addTransactionId(TransactionId transactionId) {
        List<TransactionId> updatedTransactionIds = new ArrayList<>(transactionIds);
        updatedTransactionIds.add(transactionId);
        return new Account(id, name, balance, updatedTransactionIds, createdInfo, updatedInfo.updateTimestamp());
    }

    private Account removeTransactionId(TransactionId transactionId) {
        List<TransactionId> updatedTransactionIds = new ArrayList<>(transactionIds);
        updatedTransactionIds.remove(transactionId);
        return new Account(id, name, balance, updatedTransactionIds, createdInfo, updatedInfo.updateTimestamp());
    }

    private Account updateBalance(Money transactionAmount) {
        Money newBalance = balance.add(transactionAmount);
        return new Account(id, name, newBalance, transactionIds, createdInfo, updatedInfo.updateTimestamp());
    }
}
