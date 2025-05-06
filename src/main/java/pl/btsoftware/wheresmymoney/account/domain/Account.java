package pl.btsoftware.wheresmymoney.account.domain;

import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseIdNullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Account(AccountId id, String name, List<ExpenseId> expenses) {
    public Account(AccountId id, String name) {
        this(id, name, new ArrayList<>());
    }

    public Account changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new AccountNameEmptyException();
        }
        if (newName.length() > 255) {
            throw new AccountNameTooLongException();
        }
        if (!newName.matches("^[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$")) {
            throw new AccountNameInvalidCharactersException();
        }
        return new Account(id, newName, expenses);
    }

    public Account addExpense(ExpenseId expenseId) {
        if (expenseId == null) {
            throw new ExpenseIdNullException();
        }
        List<ExpenseId> newExpenseIds = new ArrayList<>(expenses);
        newExpenseIds.add(expenseId);
        return new Account(id, name, newExpenseIds);
    }

    public Account removeExpense(ExpenseId expenseId) {
        if (expenseId == null) {
            throw new ExpenseIdNullException();
        }
        List<ExpenseId> newExpenseIds = new ArrayList<>(expenses);
        newExpenseIds.remove(expenseId);
        return new Account(id, name, newExpenseIds);
    }

    public List<ExpenseId> getExpenseIds() {
        return Collections.unmodifiableList(expenses);
    }
}
