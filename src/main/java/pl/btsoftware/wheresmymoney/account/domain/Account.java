package pl.btsoftware.wheresmymoney.account.domain;

import pl.btsoftware.wheresmymoney.account.domain.error.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.time.OffsetDateTime.now;
import static pl.btsoftware.wheresmymoney.account.domain.Money.zero;

public record Account(AccountId id, String name, Money balance, OffsetDateTime createdAt) {
    public Account {
        validateAccountName(name);
    }

    public Account(AccountId id, String name, String currency) {
        this(id, name, zero(currency), now(ZoneOffset.UTC));
    }

    private static void validateAccountName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new AccountNameEmptyException();
        }
        if (newName.length() > 255) {
            throw new AccountNameTooLongException();
        }
        if (!newName.matches("^[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$")) {
            throw new AccountNameInvalidCharactersException();
        }
    }

    public Account changeName(String newName) {
        validateAccountName(newName);
        return new Account(id, newName, balance, createdAt);
    }

    public Account addExpense(Expense expense) {
        if (expense == null) {
            throw new ExpenseIdNullException();
        }

        if (!expense.amount().currency().equals(balance.currency())) {
            throw new CurrencyMismatchException(expense.amount().currency(), balance.currency());
        }

        return new Account(id, name, balance.subtract(expense.amount()), createdAt);
    }

    public Account removeExpense(Expense expense) {
        if (expense == null) {
            throw new ExpenseIdNullException();
        }

        if (!expense.amount().currency().equals(balance.currency())) {
            throw new CurrencyMismatchException(expense.amount().currency(), balance.currency());
        }

        return new Account(id, name, balance.add(expense.amount()), createdAt);
    }
}
