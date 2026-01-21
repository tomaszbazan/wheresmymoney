package pl.btsoftware.backend.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.*;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

@SystemTest
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UsersModuleFacade usersModuleFacade;

    @Test
    void shouldCreateAccountWithSpecificCurrency() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("EUR Account", EUR, userId);

        // when
        var account = accountService.createAccount(command);

        // then
        assertThat(account.name()).isEqualTo("EUR Account");
        assertThat(account.balance().currency()).isEqualTo(EUR);
        assertThat(account.balance().value()).isEqualByComparingTo(ZERO);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateAccount() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("Duplicate Account", PLN, userId);
        accountService.createAccount(command);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountCommand("Duplicate Account", PLN, userId)))
                .isInstanceOf(AccountAlreadyExistsException.class);
    }

    @Test
    void shouldRetrieveAllAccounts() {
        // given
        var userId = createTestUser();
        accountService.createAccount(new CreateAccountCommand("Account 1", PLN, userId));
        accountService.createAccount(new CreateAccountCommand("Account 2", EUR, userId));

        // when
        var accounts = accountService.getAccounts(userId);

        // then
        assertThat(accounts).hasSize(2);
        assertThat(accounts.getFirst().name()).isEqualTo("Account 1");
        assertThat(accounts.get(1).name()).isEqualTo("Account 2");
    }

    @Test
    void shouldGetAccountById() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("Find Me", USD, userId);
        var createdAccount = accountService.createAccount(command);

        // when
        var foundAccount = accountService.getById(createdAccount.id(), userId);

        // then
        assertThat(foundAccount.id()).isEqualTo(createdAccount.id());
        assertThat(foundAccount.name()).isEqualTo("Find Me");
        assertThat(foundAccount.balance().currency()).isEqualTo(USD);
    }

    @Test
    void shouldUpdateAccountName() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("Original Name", PLN, userId);
        var account = accountService.createAccount(command);

        // when
        var groupId = usersModuleFacade.findUserOrThrow(userId).groupId();
        var updatedAccount = accountService.updateAccount(account.id(), "New Name", groupId);

        // then
        assertThat(updatedAccount.name()).isEqualTo("New Name");
        assertThat(updatedAccount.id()).isEqualTo(account.id());
        assertThat(updatedAccount.balance().currency()).isEqualTo(PLN);
    }

    @Test
    void shouldDeleteAccount() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("To Delete", PLN, userId);
        var account = accountService.createAccount(command);

        // when
        accountService.deleteAccount(account.id(), userId);

        // then
        assertThatThrownBy(() -> accountService.getById(account.id(), userId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldAddIncomeTransaction() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("Income Account", PLN, userId);
        var account = accountService.createAccount(command);

        // when
        accountService.addTransaction(account.id(), Money.of(new BigDecimal("500"), PLN), INCOME, userId);

        // then
        var updatedAccount = accountService.getById(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void shouldAddExpenseTransaction() {
        // given
        var userId = createTestUser();
        var command = new CreateAccountCommand("Expense Account", PLN, userId);
        var account = accountService.createAccount(command);

        // when
        accountService.addTransaction(account.id(), Money.of(new BigDecimal("500"), PLN), EXPENSE, userId);

        // then
        var updatedAccount = accountService.getById(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("-500"));
    }

    private UserId createTestUser() {
        var timestamp = System.currentTimeMillis();
        var command = new RegisterUserCommand(
                "test-auth-id-" + timestamp,
                "test" + timestamp + "@example.com",
                "Test User",
                "Test Group " + timestamp,
                null
        );
        var user = usersModuleFacade.registerUser(command);
        return user.id();
    }
}
