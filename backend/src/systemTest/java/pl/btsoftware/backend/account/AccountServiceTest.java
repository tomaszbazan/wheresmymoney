package pl.btsoftware.backend.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.configuration.SystemTest;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.account.domain.Currency.*;

@SystemTest
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    void shouldCreateAccountWithSpecificCurrency() {
        // given
        var command = new CreateAccountCommand("EUR Account", EUR);

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
        var command = new CreateAccountCommand("Duplicate Account", PLN);
        accountService.createAccount(command);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(command))
                .isInstanceOf(AccountAlreadyExistsException.class);
    }

    @Test
    void shouldRetrieveAllAccounts() {
        // given
        accountService.createAccount(new CreateAccountCommand("Account 1", PLN));
        accountService.createAccount(new CreateAccountCommand("Account 2", EUR));

        // when
        var accounts = accountService.getAccounts();

        // then
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).name()).isEqualTo("Account 1");
        assertThat(accounts.get(1).name()).isEqualTo("Account 2");
    }

    @Test
    void shouldGetAccountById() {
        // given
        var command = new CreateAccountCommand("Find Me", USD);
        var createdAccount = accountService.createAccount(command);

        // when
        var foundAccount = accountService.getById(createdAccount.id().value());

        // then
        assertThat(foundAccount.id()).isEqualTo(createdAccount.id());
        assertThat(foundAccount.name()).isEqualTo("Find Me");
        assertThat(foundAccount.balance().currency()).isEqualTo(USD);
    }

    @Test
    void shouldUpdateAccountName() {
        // given
        var command = new CreateAccountCommand("Original Name", PLN);
        var account = accountService.createAccount(command);

        // when
        var updatedAccount = accountService.updateAccount(account.id().value(), "New Name");

        // then
        assertThat(updatedAccount.name()).isEqualTo("New Name");
        assertThat(updatedAccount.id()).isEqualTo(account.id());
        assertThat(updatedAccount.balance().currency()).isEqualTo(PLN);
    }

    @Test
    void shouldDeleteAccount() {
        // given
        var command = new CreateAccountCommand("To Delete", PLN);
        var account = accountService.createAccount(command);

        // when
        accountService.deleteAccount(account.id().value());

        // then
        assertThatThrownBy(() -> accountService.getById(account.id().value()))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldAddIncomeTransaction() {
        // given
        var command = new CreateAccountCommand("Income Account", PLN);
        var account = accountService.createAccount(command);

        // when
        var updatedAccount = accountService.addTransaction(account.id().value(), new BigDecimal("500"), "INCOME");

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void shouldAddExpenseTransaction() {
        // given
        var command = new CreateAccountCommand("Expense Account", PLN);
        var account = accountService.createAccount(command);

        // when
        var updatedAccount = accountService.addTransaction(account.id().value(), new BigDecimal("500"), "EXPENSE");

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("-500"));
    }
}
