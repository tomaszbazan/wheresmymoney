package pl.btsoftware.wheresmymoney.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.domain.*;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNotFoundException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;

    public Account createAccount(AccountModuleFacade.CreateAccountCommand command) {
        var account = command.toDomain();
        accountRepository.store(account);
        return account;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Account getById(UUID id) {
        return accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Expense createExpense(AccountModuleFacade.CreateExpenseCommand command) {
        var expense = command.toDomain();

        var account = accountRepository.findById(expense.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        var updatedAccount = account.addExpense(expense);

        expenseRepository.store(expense);
        accountRepository.store(updatedAccount);

        return expense;
    }

    public Expense updateExpense(AccountModuleFacade.UpdateExpenseCommand command) {
        var expense = expenseRepository.findById(ExpenseId.from(command.expenseId()))
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        var updatedExpense = expense;

        if (command.amount() != null) {
            updatedExpense = updatedExpense.updateAmount(command.amount());
        }

        if (command.description() != null) {
            updatedExpense = updatedExpense.updateDescription(command.description());
        }

        if (command.date() != null) {
            updatedExpense = updatedExpense.updateDate(command.date());
        }

        expenseRepository.store(updatedExpense);
        return updatedExpense;
    }

    public void deleteExpense(UUID expenseId) {
        // Find the expense to get its accountId before deleting
        var expense = expenseRepository.findById(ExpenseId.from(expenseId))
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Remove expense from the account
        var account = accountRepository.findById(expense.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        var updatedAccount = account.removeExpense(expense);
        accountRepository.store(updatedAccount);

        // Delete the expense
        expenseRepository.deleteById(expenseId);
    }

    public Expense getExpenseById(UUID expenseId) {
        return expenseRepository.findById(ExpenseId.from(expenseId))
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getExpensesByAccountId(UUID accountId) {
        return expenseRepository.findByAccountId(AccountId.from(accountId));
    }

    public Account updateAccount(UUID id, String newName) {
        var account = accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));
        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }

    public void deleteAccount(UUID id) {
        // Find the account to ensure it exists
        var account = accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));

        // Find all expenses for the account
        var expenses = expenseRepository.findByAccountId(account.id());

        // Delete all expenses for the account
        for (Expense expense : expenses) {
            expenseRepository.deleteById(expense.id().value());
        }

        // Delete the account
        accountRepository.deleteById(id);
    }
}
