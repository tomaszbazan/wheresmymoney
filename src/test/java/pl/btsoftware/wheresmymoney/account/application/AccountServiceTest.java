package pl.btsoftware.wheresmymoney.account.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade.CreateExpenseCommand;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseRepository;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNotFoundException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseNotFoundException;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryExpenseRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTest {
    private AccountRepository accountRepository;
    private ExpenseRepository expenseRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = new InMemoryAccountRepository();
        this.expenseRepository = new InMemoryExpenseRepository();
        this.accountService = new AccountService(accountRepository, expenseRepository);
    }

    @Test
    void shouldCreateAccount() {
        // given
        var accountName = "test";
        var command = new CreateAccountCommand(accountName);

        // when
        var account = accountService.createAccount(command);

        // then
        assertThat(accountRepository.findAll()).hasSize(1).containsOnly(account);
    }

    @Test
    void shouldReturnAllAccounts() {
        // given
        IntStream.range(0, 5).forEach(i -> accountService.createAccount(new CreateAccountCommand("test" + i)));

        // when
        var result = accountService.getAccounts();

        // then
        assertThat(result).hasSize(5);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsFound() {
        // when
        var result = accountService.getAccounts();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAccountById() {
        // given
        var accountName = "test";
        var command = new CreateAccountCommand(accountName);
        var account = accountService.createAccount(command);

        // when
        var result = accountService.getById(account.id().value());

        // then
        assertThat(result).isEqualTo(account);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        // given
        var accountId = UUID.randomUUID();

        // when & then
        Assertions.assertThrows(AccountNotFoundException.class, () -> accountService.getById(accountId));
    }

    @Test
    void shouldThrowExceptionWhenCreatingExpenseForNonExistentAccount() {
        // given
        var nonExistentAccountId = UUID.randomUUID();
        var command = new CreateExpenseCommand(
                nonExistentAccountId,
                BigDecimal.valueOf(100),
                "Test Expense",
                OffsetDateTime.now());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> accountService.createExpense(command));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentExpense() {
        // given
        var nonExistentExpenseId = UUID.randomUUID();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> accountService.deleteExpense(nonExistentExpenseId));
    }

    @Test
    void shouldReturnExpensesByAccountId() {
        // given
        var account1 = accountService.createAccount(new CreateAccountCommand("Account 1"));
        var account2 = accountService.createAccount(new CreateAccountCommand("Account 2"));

        var expense1 = accountService.createExpense(new CreateExpenseCommand(
                account1.id().value(), BigDecimal.valueOf(100), "Expense 1 for Account 1", OffsetDateTime.now()));
        var expense2 = accountService.createExpense(new CreateExpenseCommand(
                account1.id().value(), BigDecimal.valueOf(200), "Expense 2 for Account 1", OffsetDateTime.now()));
        var expense3 = accountService.createExpense(new CreateExpenseCommand(
                account2.id().value(), BigDecimal.valueOf(300), "Expense 1 for Account 2", OffsetDateTime.now()));

        // when
        var expensesForAccount1 = accountService.getExpensesByAccountId(account1.id().value());
        var expensesForAccount2 = accountService.getExpensesByAccountId(account2.id().value());

        // then
        assertThat(expensesForAccount1).hasSize(2)
                .containsExactlyInAnyOrder(expense1, expense2);
        assertThat(expensesForAccount2).hasSize(1)
                .containsExactly(expense3);
    }

    @Test
    void shouldReturnEmptyListWhenNoExpensesForAccount() {
        // given
        var account = accountService.createAccount(new CreateAccountCommand("Test Account"));

        // when
        var expenses = accountService.getExpensesByAccountId(account.id().value());

        // then
        assertThat(expenses).isEmpty();
    }

    @Test
    void shouldGetExpenseById() {
        // given
        var account = accountService.createAccount(new CreateAccountCommand("Test Account"));
        var expense = accountService.createExpense(new CreateExpenseCommand(
                account.id().value(), BigDecimal.valueOf(100), "Test Expense", OffsetDateTime.now()));

        // when
        var retrievedExpense = accountService.getExpenseById(expense.id().value());

        // then
        assertThat(retrievedExpense).isEqualTo(expense);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentExpense() {
        // given
        var nonExistentExpenseId = UUID.randomUUID();

        // when & then
        assertThrows(ExpenseNotFoundException.class, () -> accountService.getExpenseById(nonExistentExpenseId));
    }

    @Test
    void shouldGetAllExpenses() {
        // given
        var account1 = accountService.createAccount(new CreateAccountCommand("Account 1"));
        var account2 = accountService.createAccount(new CreateAccountCommand("Account 2"));

        var expense1 = accountService.createExpense(new CreateExpenseCommand(
                account1.id().value(), BigDecimal.valueOf(100), "Expense 1", OffsetDateTime.now()));
        var expense2 = accountService.createExpense(new CreateExpenseCommand(
                account1.id().value(), BigDecimal.valueOf(200), "Expense 2", OffsetDateTime.now()));
        var expense3 = accountService.createExpense(new CreateExpenseCommand(
                account2.id().value(), BigDecimal.valueOf(300), "Expense 3", OffsetDateTime.now()));

        // when
        var allExpenses = accountService.getAllExpenses();

        // then
        assertThat(allExpenses).hasSize(3)
                .containsExactlyInAnyOrder(expense1, expense2, expense3);
    }

    @Test
    void shouldUpdateAccountName() {
        // given
        var account = accountService.createAccount(new CreateAccountCommand("Original Name"));
        var newName = "Updated Name";

        // when
        var updatedAccount = accountService.updateAccount(account.id().value(), newName);

        // then
        assertThat(updatedAccount.name()).isEqualTo(newName);
        var retrievedAccount = accountService.getById(account.id().value());
        assertThat(retrievedAccount.name()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAccount() {
        // given
        var nonExistentAccountId = UUID.randomUUID();
        var newName = "Updated Name";

        // when & then
        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(nonExistentAccountId, newName));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingAccountWithInvalidName() {
        // given
        var account = accountService.createAccount(new CreateAccountCommand("Original Name"));
        var emptyName = "";

        // when & then
        assertThrows(AccountNameEmptyException.class, () -> accountService.updateAccount(account.id().value(), emptyName));

        // and
        var retrievedAccount = accountService.getById(account.id().value());
        assertThat(retrievedAccount.name()).isEqualTo("Original Name");
    }

    @Test
    void shouldDeleteAccountAndAllItsExpenses() {
        // given
        var account = accountService.createAccount(new CreateAccountCommand("Test Account"));
        var expense1 = accountService.createExpense(new CreateExpenseCommand(
                account.id().value(), BigDecimal.valueOf(100), "Expense 1", OffsetDateTime.now()));
        var expense2 = accountService.createExpense(new CreateExpenseCommand(
                account.id().value(), BigDecimal.valueOf(200), "Expense 2", OffsetDateTime.now()));

        // when
        accountService.deleteAccount(account.id().value());

        // then
        assertThat(accountService.getAccounts()).isEmpty();
        assertThrows(ExpenseNotFoundException.class, () -> accountService.getExpenseById(expense1.id().value()));
        assertThrows(ExpenseNotFoundException.class, () -> accountService.getExpenseById(expense2.id().value()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentAccount() {
        // given
        var nonExistentAccountId = UUID.randomUUID();

        // when & then
        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(nonExistentAccountId));
    }
}
