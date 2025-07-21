package pl.btsoftware.backend.account.domain;

import jakarta.annotation.Nullable;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.OffsetDateTime.now;
import static pl.btsoftware.backend.account.domain.Currency.DEFAULT;
import static pl.btsoftware.backend.account.domain.Money.zero;

public record Account(AccountId id, String name, Money balance, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public Account {
        validateAccountName(name);
    }

    public Account(AccountId id, String name, @Nullable Currency currency) {
        this(id, name, zero(currency == null ? DEFAULT : currency), now(ZoneOffset.UTC), now(ZoneOffset.UTC));
    }

    public Account(AccountId id, String name, Money balance, OffsetDateTime createdAt) {
        this(id, name, balance, createdAt, now(ZoneOffset.UTC));
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
        return new Account(id, newName, balance, createdAt, now(ZoneOffset.UTC));
    }

    public Account updateBalance(Money transactionAmount) {
        Money newBalance = balance.add(transactionAmount);
        return new Account(id, name, newBalance, createdAt, now(ZoneOffset.UTC));
    }

}
