package pl.btsoftware.backend.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.error.AccountAlreadyExistsException;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;

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
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.findAll().forEach(
                account -> accountRepository.deleteById(account.id())
        );
    }

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
        var foundAccount = accountService.getById(createdAccount.id());

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
        var updatedAccount = accountService.updateAccount(account.id(), "New Name");

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
        accountService.deleteAccount(account.id());

        // then
        assertThatThrownBy(() -> accountService.getById(account.id()))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldAddIncomeTransaction() {
        // given
        var command = new CreateAccountCommand("Income Account", PLN);
        var account = accountService.createAccount(command);

        // when
        accountService.addTransaction(account.id(), TransactionId.generate(), Money.of(new BigDecimal("500"), PLN), INCOME);

        // then
        var updatedAccount = accountService.getById(account.id());
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void shouldAddExpenseTransaction() {
        // given
        var command = new CreateAccountCommand("Expense Account", PLN);
        var account = accountService.createAccount(command);

        // when
        accountService.addTransaction(account.id(), TransactionId.generate(), Money.of(new BigDecimal("500"), PLN), EXPENSE);

        // then
        var updatedAccount = accountService.getById(account.id());
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(new BigDecimal("-500"));
    }
}
