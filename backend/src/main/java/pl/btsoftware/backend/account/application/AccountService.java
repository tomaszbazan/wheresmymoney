package pl.btsoftware.backend.account.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.*;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.domain.error.CannotDeleteAccountWithTransactionsException;
import pl.btsoftware.backend.account.domain.error.ExpenseNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;

    public Account createAccount(AccountModuleFacade.CreateAccountCommand command) {
        // Check for duplicate name
        if (accountRepository.findByName(command.name()).isPresent()) {
            throw new AccountAlreadyExistsException(command.name());
        }
        
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
        
        // Check for duplicate name (but allow same name for the same account)
        var existingAccount = accountRepository.findByName(newName);
        if (existingAccount.isPresent() && !existingAccount.get().id().equals(account.id())) {
            throw new AccountAlreadyExistsException(newName);
        }
        
        var updatedAccount = account.changeName(newName);
        accountRepository.store(updatedAccount);
        return updatedAccount;
    }

    public void deleteAccount(UUID id) {
        var account = accountRepository.findById(AccountId.from(id))
                .orElseThrow(() -> new AccountNotFoundException(id));

        var expenses = expenseRepository.findByAccountId(account.id());

        if (!expenses.isEmpty()) {
            throw new CannotDeleteAccountWithTransactionsException();
        }

        // Delete the account
        accountRepository.deleteById(id);
    }
}
