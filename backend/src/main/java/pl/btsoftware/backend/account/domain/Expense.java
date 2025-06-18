package pl.btsoftware.backend.account.domain;

import pl.btsoftware.backend.account.domain.error.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Expense(
    ExpenseId id,
    AccountId accountId,
    Money amount,
    String description,
    OffsetDateTime createdAt
) {
    public Expense {
        if (id == null) {
            throw new ExpenseIdNullException();
        }
        if (accountId == null) {
            throw new AccountIdNullException();
        }
        if (amount == null) {
            throw new ExpenseAmountNullException();
        }
        if (description == null || description.isBlank()) {
            throw new ExpenseDescriptionEmptyException();
        }
        if (createdAt == null) {
            throw new ExpenseDateNullException();
        }
    }

    public Expense updateAmount(BigDecimal newAmount) {
        if (newAmount == null) {
            throw new ExpenseAmountNullException();
        }
        return new Expense(id, accountId, amount.withAmount(newAmount), description, createdAt);
    }

    public Expense updateDescription(String newDescription) {
        if (newDescription == null || newDescription.isBlank()) {
            throw new ExpenseDescriptionEmptyException();
        }
        return new Expense(id, accountId, amount, newDescription, createdAt);
    }

    public Expense updateDate(OffsetDateTime newDate) {
        if (newDate == null) {
            throw new ExpenseDateNullException();
        }
        return new Expense(id, accountId, amount, description, newDate);
    }

    public Expense updateCurrency(String newCurrency) {
        return new Expense(id, accountId, amount.withCurrency(newCurrency), description, createdAt);
    }
}
