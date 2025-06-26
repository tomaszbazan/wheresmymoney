package pl.btsoftware.backend.account;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.*;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static pl.btsoftware.backend.account.domain.AccountId.generate;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.createAccount(command);
    }

    public List<Account> getAccounts() {
        return accountService.getAccounts();
    }

    public Account getAccount(UUID accountId) {
        return accountService.getById(accountId);
    }

    public Expense createExpense(CreateExpenseCommand command) {
        return accountService.createExpense(command);
    }

    public Expense updateExpense(UpdateExpenseCommand command) {
        return accountService.updateExpense(command);
    }

    public void deleteExpense(UUID expenseId) {
        accountService.deleteExpense(expenseId);
    }

    public Expense getExpense(UUID expenseId) {
        return accountService.getExpenseById(expenseId);
    }

    public List<Expense> getExpenses() {
        return accountService.getAllExpenses();
    }

    public List<Expense> getExpensesByAccountId(UUID accountId) {
        return accountService.getExpensesByAccountId(accountId);
    }

    public record CreateAccountCommand(String name, @Nullable String currency) {
        public Account toDomain() {
            return new Account(generate(), name, currency);
        }
    }

    public record CreateExpenseCommand(UUID accountId, BigDecimal amount, String description, OffsetDateTime date,
                                       String currency) {
        public CreateExpenseCommand {
            if (accountId == null) {
                throw new IllegalArgumentException("Account id cannot be null");
            }
            if (amount == null) {
                throw new IllegalArgumentException("Amount cannot be null");
            }
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }
            if (currency == null || currency.isBlank()) {
                currency = Money.DEFAULT_CURRENCY;
            }
        }

        public CreateExpenseCommand(UUID accountId, BigDecimal amount, String description, OffsetDateTime date) {
            this(accountId, amount, description, date, Money.DEFAULT_CURRENCY);
        }

        public Expense toDomain() {
            return new Expense(
                    ExpenseId.generate(),
                    AccountId.from(accountId),
                    new Money(amount, currency),
                    description,
                    date
            );
        }
    }

    public record UpdateExpenseCommand(UUID expenseId, BigDecimal amount, String description, OffsetDateTime date) {
        public UpdateExpenseCommand {
            if (expenseId == null) {
                throw new IllegalArgumentException("Expense id cannot be null");
            }
        }
    }

    public record UpdateAccountCommand(UUID accountId, String name) {
        public UpdateAccountCommand {
            if (accountId == null) {
                throw new IllegalArgumentException("Account id cannot be null");
            }
            if (name == null || name.isBlank()) {
                throw new AccountNameEmptyException();
            }
        }
    }

    public Account updateAccount(UpdateAccountCommand command) {
        return accountService.updateAccount(command.accountId(), command.name());
    }

    public void deleteAccount(UUID accountId) {
        accountService.deleteAccount(accountId);
    }
}
