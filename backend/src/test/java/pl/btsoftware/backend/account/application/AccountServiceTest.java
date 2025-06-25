package pl.btsoftware.backend.account.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.btsoftware.backend.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.backend.account.AccountModuleFacade.CreateExpenseCommand;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.ExpenseRepository;
import pl.btsoftware.backend.account.domain.error.*;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryExpenseRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

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

    @Nested
    class CreateAccount {

        @ParameterizedTest
        @ValueSource(strings = {"PLN", "EUR", "USD", "GBP"})
        void shouldCreateAccountWithDifferentSupportedCurrencies(String currency) {
            // given
            var accountName = currency + " Account";
            var command = new CreateAccountCommand(accountName, currency);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(accountName);
            assertThat(account.balance().currency()).isEqualTo(currency);
            assertThat(account.balance().amount()).isZero();
        }

        @Test
        void shouldCreateAccountWithMinimalData() {
            // given
            var accountName = "Minimal Account";
            var command = new CreateAccountCommand(accountName);

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(accountName);
            assertThat(account.balance().amount()).isZero();
            assertThat(account.balance().currency()).isEqualTo("PLN"); // default currency
            assertThat(account.id()).isNotNull();
        }

        @Test
        void shouldRejectAccountCreationWithEmptyName() {
            // given
            var command = new CreateAccountCommand("", "PLN");

            // when & then
            assertThrows(AccountNameEmptyException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithNullName() {
            // given
            var command = new CreateAccountCommand(null, "PLN");

            // when & then
            assertThrows(AccountNameEmptyException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithBlankName() {
            // given
            var command = new CreateAccountCommand("   ", "PLN");

            // when & then
            assertThrows(AccountNameEmptyException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectAccountCreationWithTooLongName() {
            // given
            var longName = "a".repeat(101);
            var command = new CreateAccountCommand(longName, "PLN");

            // when & then
            assertThrows(AccountNameTooLongException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"Invalid\nName", "Invalid\tName"})
        void shouldRejectAccountCreationWithInvalidCharacters(String invalidName) {
            // given
            var command = new CreateAccountCommand(invalidName, "PLN");

            // when & then
            assertThrows(AccountNameInvalidCharactersException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldCreateAccountWithValidSpecialCharacters() {
            // given
            var validName = "Valid Name-123 O'Connor's";
            var command = new CreateAccountCommand(validName, "PLN");

            // when
            var account = accountService.createAccount(command);

            // then
            assertThat(account.name()).isEqualTo(validName);
            assertThat(accountRepository.findAll()).hasSize(1).containsOnly(account);
        }

        @Test
        void shouldRejectAccountCreationWithUnsupportedCurrency() {
            // given
            var command = new CreateAccountCommand("JPY Account", "JPY");

            // when & then
            assertThrows(InvalidCurrencyException.class, () -> accountService.createAccount(command));
            assertThat(accountRepository.findAll()).isEmpty();
        }

        @Test
        void shouldRejectDuplicateAccountNames() {
            // given
            var accountName = "Duplicate Account";
            var command1 = new CreateAccountCommand(accountName, "PLN");
            var command2 = new CreateAccountCommand(accountName, "EUR");

            // when
            accountService.createAccount(command1);

            // then
            assertThrows(AccountAlreadyExistsException.class, () -> accountService.createAccount(command2));
            assertThat(accountRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    class AccountRetrieval {

        @Test
        void shouldReturnAllActiveAccounts() {
            // given
            var account1 = accountService.createAccount(new CreateAccountCommand("Checking Account", "PLN"));
            var account2 = accountService.createAccount(new CreateAccountCommand("Savings Account", "EUR"));
            var account3 = accountService.createAccount(new CreateAccountCommand("Investment Account", "USD"));

            // when
            var result = accountService.getAccounts();

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(account1, account2, account3);

            // verify each account has required fields
            result.forEach(account -> {
                assertThat(account.id()).isNotNull();
                assertThat(account.name()).isNotEmpty();
                assertThat(account.balance().currency()).isNotEmpty();
                assertThat(account.balance().amount()).isNotNull();
                assertThat(account.createdAt()).isNotNull();
                assertThat(account.updatedAt()).isNotNull();
            });
        }

        @Test
        void shouldReturnEmptyListWhenNoAccountsExist() {
            // when
            var result = accountService.getAccounts();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnSpecificAccountById() {
            // given
            var accountName = "Test Account";
            var currency = "EUR";
            var account = accountService.createAccount(new CreateAccountCommand(accountName, currency));

            // when
            var result = accountService.getById(account.id().value());

            // then
            assertThat(result).isEqualTo(account);
            assertThat(result.id()).isEqualTo(account.id());
            assertThat(result.name()).isEqualTo(accountName);
            assertThat(result.balance().currency()).isEqualTo(currency);
            assertThat(result.balance().amount()).isZero();
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        void shouldThrowExceptionWhenAccountNotFound() {
            // given
            var nonExistentAccountId = UUID.randomUUID();

            // when & then
            var exception = assertThrows(AccountNotFoundException.class,
                    () -> accountService.getById(nonExistentAccountId));
            assertThat(exception.getMessage()).contains("Account not found");
        }

        @Test
        void shouldReturnAccountsWithCompleteFieldStructure() {
            // given
            var account = accountService.createAccount(new CreateAccountCommand("Complete Account", "GBP"));

            // when
            var retrievedAccount = accountService.getById(account.id().value());

            // then
            assertThat(retrievedAccount.id()).isNotNull();
            assertThat(retrievedAccount.name()).isEqualTo("Complete Account");
            assertThat(retrievedAccount.balance().currency()).isEqualTo("GBP");
            assertThat(retrievedAccount.balance().amount()).isZero();
            assertThat(retrievedAccount.createdAt()).isNotNull();
            assertThat(retrievedAccount.updatedAt()).isNotNull();
        }
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

    @Nested
    class AccountUpdate {

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
        void shouldThrowExceptionWhenUpdatingWithDuplicateName() {
            // given
            var account1 = accountService.createAccount(new CreateAccountCommand("Account One", "PLN"));
            var account2 = accountService.createAccount(new CreateAccountCommand("Account Two", "PLN"));

            // when & then
            assertThrows(AccountAlreadyExistsException.class,
                    () -> accountService.updateAccount(account2.id().value(), "Account One"));

            // verify original names are preserved
            var retrievedAccount1 = accountService.getById(account1.id().value());
            var retrievedAccount2 = accountService.getById(account2.id().value());
            assertThat(retrievedAccount1.name()).isEqualTo("Account One");
            assertThat(retrievedAccount2.name()).isEqualTo("Account Two");
        }
    }

    @Nested
    class AccountDeletion {

        @Test
        void shouldDeleteAccountWithZeroBalance() {
            // given
            var account = accountService.createAccount(new CreateAccountCommand("Test Account"));

            // when
            accountService.deleteAccount(account.id().value());

            // then
            assertThat(accountService.getAccounts()).isEmpty();
        }

        @Test
        void shouldRejectDeletionOfAccountWithTransactionHistory() {
            // given
            var account = accountService.createAccount(new CreateAccountCommand("Test Account"));
            accountService.createExpense(new CreateExpenseCommand(
                    account.id().value(), BigDecimal.valueOf(100), "Test Expense", OffsetDateTime.now()));

            // when & then
            assertThrows(CannotDeleteAccountWithTransactionsException.class, () -> accountService.deleteAccount(account.id().value()));
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistentAccount() {
            // given
            var nonExistentAccountId = UUID.randomUUID();

            // when & then
            assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(nonExistentAccountId));
        }
    }
}
