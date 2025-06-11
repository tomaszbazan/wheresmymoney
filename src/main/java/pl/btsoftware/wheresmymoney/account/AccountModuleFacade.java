package pl.btsoftware.wheresmymoney.account;

import lombok.AllArgsConstructor;
import pl.btsoftware.wheresmymoney.account.application.AccountService;
import pl.btsoftware.wheresmymoney.account.domain.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class AccountModuleFacade {
    private final AccountService accountService;

    public Account createAccount(CreateAccountCommand command) {
        return accountService.createAccount(command);
    }

    public List<Account> getAccounts() {
        return accountService.getAccounts();
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

    public record CreateAccountCommand(String name, String currency) {
        public CreateAccountCommand(String name) {
            this(name, Money.DEFAULT_CURRENCY);
        }

        public Account toDomain() {
            return new Account(AccountId.generate(), name, currency);
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
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be null or blank");
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
                throw new IllegalArgumentException("Account name cannot be null or blank");
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
