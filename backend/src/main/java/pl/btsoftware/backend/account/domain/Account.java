package pl.btsoftware.backend.account.domain;

import jakarta.annotation.Nullable;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.shared.validation.NameValidationRules;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.infrastructure.api.UserView;

import java.time.OffsetDateTime;

import static pl.btsoftware.backend.shared.Currency.DEFAULT;
import static pl.btsoftware.backend.shared.Money.zero;

public record Account(AccountId id, String name, Money balance,
                      AuditInfo createdInfo, AuditInfo updatedInfo, Tombstone tombstone) {
    public Account(AccountId id, String name, Money balance, AuditInfo createdInfo,
                   AuditInfo updatedInfo, Tombstone tombstone) {
        validateAccountName(name);
        this.id = id;
        this.name = name.trim();
        this.balance = balance;
        this.createdInfo = createdInfo;
        this.updatedInfo = updatedInfo;
        this.tombstone = tombstone;
    }

    public Account(AccountId id, String name, @Nullable Currency currency, UserView createdBy) {
        this(id, name, zero(currency == null ? DEFAULT : currency),
                AuditInfo.create(createdBy.id(), createdBy.groupId()), AuditInfo.create(createdBy.id(), createdBy.groupId()), Tombstone.active());
    }

    public Account(AccountId id, String name, Money balance, AuditInfo createBy) {
        this(id, name, balance, createBy, createBy.updateTimestamp(), Tombstone.active());
    }

    private static void validateAccountName(String newName) {
        NameValidationRules.validate(
                newName,
                AccountNameEmptyException::new,
                AccountNameTooLongException::new,
                AccountNameInvalidCharactersException::new
        );
    }

    public Account changeName(String newName) {
        validateAccountName(newName);
        return new Account(id, newName, balance, createdInfo, updatedInfo.updateTimestamp(), tombstone);
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

    public Account addTransaction(Money amount, TransactionType transactionType) {
        if (balance().currency() != amount.currency()) {
            throw new TransactionCurrencyMismatchException(amount.currency(), balance().currency());
        }
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(amount);
            }
            case EXPENSE -> {
                return updateBalance(amount.negate());
            }
            default -> {
                return this;
            }
        }
    }

    public Account removeTransaction(Money amount, TransactionType transactionType) {
        if (balance().currency() != amount.currency()) {
            throw new TransactionCurrencyMismatchException(amount.currency(), balance().currency());
        }
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(amount.negate());
            }
            case EXPENSE -> {
                return updateBalance(amount);
            }
            default -> {
                return this;
            }
        }
    }

    public Account changeTransaction(Money oldAmount, Money newAmount, TransactionType transactionType) {
        if (balance().currency() != oldAmount.currency() && balance().currency() != newAmount.currency()) {
            throw new TransactionCurrencyMismatchException(newAmount.currency(), balance().currency());
        }
        var balanceChange = newAmount.subtract(oldAmount);
        switch (transactionType) {
            case INCOME -> {
                return updateBalance(balanceChange);
            }
            case EXPENSE -> {
                return updateBalance(balanceChange.negate());
            }
            default -> {
                return this;
            }
        }
    }

    private Account updateBalance(Money transactionAmount) {
        Money newBalance = balance.add(transactionAmount);
        return new Account(id, name, newBalance, createdInfo, updatedInfo.updateTimestamp(), tombstone);
    }

    public Account delete() {
        return new Account(id, name, balance, createdInfo, updatedInfo, Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }
}
