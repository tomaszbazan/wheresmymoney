package pl.btsoftware.backend.account.domain;

import pl.btsoftware.backend.account.domain.error.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.OffsetDateTime.now;
import static pl.btsoftware.backend.account.domain.Money.zero;

public record Account(AccountId id, String name, Money balance, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public Account {
        validateAccountName(name);
    }

    public Account(AccountId id, String name, String currency) {
        this(id, name, zero(currency), now(ZoneOffset.UTC), now(ZoneOffset.UTC));
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

    public Account addExpense(Expense expense) {
        if (expense == null) {
            throw new ExpenseIdNullException();
        }

        if (!expense.amount().currency().equals(balance.currency())) {
            throw new CurrencyMismatchException(expense.amount().currency(), balance.currency());
        }

        return new Account(id, name, balance.subtract(expense.amount()), createdAt, now(ZoneOffset.UTC));
    }

    public Account removeExpense(Expense expense) {
        if (expense == null) {
            throw new ExpenseIdNullException();
        }

        if (!expense.amount().currency().equals(balance.currency())) {
            throw new CurrencyMismatchException(expense.amount().currency(), balance.currency());
        }

        return new Account(id, name, balance.add(expense.amount()), createdAt, now(ZoneOffset.UTC));
    }
}
