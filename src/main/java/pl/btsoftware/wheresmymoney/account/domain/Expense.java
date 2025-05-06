package pl.btsoftware.wheresmymoney.account.domain;

import pl.btsoftware.wheresmymoney.account.domain.error.AccountIdNullException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseAmountNullException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseDateNullException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseDescriptionEmptyException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseIdNullException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Expense(
    ExpenseId id,
    AccountId accountId,
    BigDecimal amount,
    String description,
    LocalDateTime date
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
        if (date == null) {
            throw new ExpenseDateNullException();
        }
    }

    public Expense updateAmount(BigDecimal newAmount) {
        if (newAmount == null) {
            throw new ExpenseAmountNullException();
        }
        return new Expense(id, accountId, newAmount, description, date);
    }

    public Expense updateDescription(String newDescription) {
        if (newDescription == null || newDescription.isBlank()) {
            throw new ExpenseDescriptionEmptyException();
        }
        return new Expense(id, accountId, amount, newDescription, date);
    }

    public Expense updateDate(LocalDateTime newDate) {
        if (newDate == null) {
            throw new ExpenseDateNullException();
        }
        return new Expense(id, accountId, amount, description, newDate);
    }
}
