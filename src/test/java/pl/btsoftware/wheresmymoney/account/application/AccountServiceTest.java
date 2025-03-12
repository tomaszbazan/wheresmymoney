package pl.btsoftware.wheresmymoney.account.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade.CreateAccountCommand;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryAccountRepository;

import javax.security.auth.login.AccountNotFoundException;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountServiceTest {
    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        this.accountRepository = new InMemoryAccountRepository();
        this.accountService = new AccountService(accountRepository);
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
}