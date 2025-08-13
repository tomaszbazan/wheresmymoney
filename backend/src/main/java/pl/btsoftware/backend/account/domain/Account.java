package pl.btsoftware.backend.account.domain;

import jakarta.annotation.Nullable;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.backend.shared.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static java.time.OffsetDateTime.now;
import static pl.btsoftware.backend.shared.Currency.DEFAULT;
import static pl.btsoftware.backend.shared.Money.zero;

public record Account(AccountId id, String name, Money balance, List<TransactionId> transactionIds,
                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public Account {
        validateAccountName(name);
    }

    public Account(AccountId id, String name, @Nullable Currency currency) {
        this(id, name, zero(currency == null ? DEFAULT : currency), new ArrayList<>(), now(ZoneOffset.UTC), now(ZoneOffset.UTC));
    }

    public Account(AccountId id, String name, Money balance, OffsetDateTime createdAt) {
        this(id, name, balance, new ArrayList<>(), createdAt, now(ZoneOffset.UTC));
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
        return new Account(id, newName, balance, transactionIds, createdAt, now(ZoneOffset.UTC));
    }

    public boolean hasAnyTransaction() {
        return !transactionIds.isEmpty();
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
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + transactionType);
        }
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
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + transactionType);
        }
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
            default -> throw new IllegalArgumentException("Unsupported transaction type: " + transactionType);
        }
    }

    private Account addTransactionId(TransactionId transactionId) {
        List<TransactionId> updatedTransactionIds = new ArrayList<>(transactionIds);
        updatedTransactionIds.add(transactionId);
        return new Account(id, name, balance, updatedTransactionIds, createdAt, now(ZoneOffset.UTC));
    }

    private Account removeTransactionId(TransactionId transactionId) {
        List<TransactionId> updatedTransactionIds = new ArrayList<>(transactionIds);
        updatedTransactionIds.remove(transactionId);
        return new Account(id, name, balance, updatedTransactionIds, createdAt, now(ZoneOffset.UTC));
    }

    private Account updateBalance(Money transactionAmount) {
        Money newBalance = balance.add(transactionAmount);
        return new Account(id, name, newBalance, transactionIds, createdAt, now(ZoneOffset.UTC));
    }
}
