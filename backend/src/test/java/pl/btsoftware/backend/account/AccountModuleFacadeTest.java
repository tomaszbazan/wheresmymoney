package pl.btsoftware.backend.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

class AccountModuleFacadeTest {

    private AccountModuleFacade accountModuleFacade;
    private UsersModuleFacade usersModuleFacade;

    @BeforeEach
    void setUp() {
        var accountRepository = new InMemoryAccountRepository();
        var transactionQueryFacade = Mockito.mock(TransactionQueryFacade.class);
        var auditModuleFacade = Mockito.mock(AuditModuleFacade.class);
        usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        var accountService =
                new AccountService(
                        accountRepository,
                        usersModuleFacade,
                        transactionQueryFacade,
                        auditModuleFacade);
        accountModuleFacade = new AccountModuleFacade(accountService, usersModuleFacade);
    }

    @Test
    void shouldCreateAccount() {
        // given
        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::currency), Currency.PLN)
                        .create();
        var user = Instancio.of(User.class).set(field(User::id), command.userId()).create();
        when(usersModuleFacade.findUserOrThrow(command.userId())).thenReturn(user);

        // when
        var account = accountModuleFacade.createAccount(command);

        // then
        assertThat(account.name()).isEqualTo(command.name());
        assertThat(account.balance().currency()).isEqualTo(Currency.PLN);
    }

    @Test
    void shouldGetAccounts() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .create();
        accountModuleFacade.createAccount(command);

        // when
        var accounts = accountModuleFacade.getAccounts(userId);

        // then
        assertThat(accounts).hasSize(1);
    }

    @Test
    void shouldGetAccountByUserIdAndAccountId() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .create();
        var account = accountModuleFacade.createAccount(command);

        // when
        var retrievedAccount = accountModuleFacade.getAccount(account.id(), userId);

        // then
        assertThat(retrievedAccount.id()).isEqualTo(account.id());
    }

    @Test
    void shouldGetAccountByGroupIdAndAccountId() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .create();
        var account = accountModuleFacade.createAccount(command);

        // when
        var retrievedAccount = accountModuleFacade.getAccount(account.id(), groupId);

        // then
        assertThat(retrievedAccount.id()).isEqualTo(account.id());
    }

    @Test
    void shouldUpdateAccount() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var createCommand =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .create();
        var account = accountModuleFacade.createAccount(createCommand);

        var updateCommand = new UpdateAccountCommand(account.id(), "Updated Name");

        // when
        var updatedAccount = accountModuleFacade.updateAccount(updateCommand, userId);

        // then
        assertThat(updatedAccount.name()).isEqualTo("Updated Name");
    }

    @Test
    void shouldDeleteAccount() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .create();
        var account = accountModuleFacade.createAccount(command);

        // when
        accountModuleFacade.deleteAccount(account.id(), userId);

        // then
        var accounts = accountModuleFacade.getAccounts(userId);
        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldDeposit() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .set(field(CreateAccountCommand::currency), Currency.PLN)
                        .create();
        var account = accountModuleFacade.createAccount(command);

        var amount = Money.of(BigDecimal.valueOf(100), Currency.PLN);

        // when
        accountModuleFacade.deposit(account.id(), amount, userId);

        // then
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldWithdraw() {
        // given
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user =
                Instancio.of(User.class)
                        .set(field(User::id), userId)
                        .set(field(User::groupId), groupId)
                        .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command =
                Instancio.of(CreateAccountCommand.class)
                        .set(field(CreateAccountCommand::userId), userId)
                        .set(field(CreateAccountCommand::currency), Currency.PLN)
                        .create();
        var account = accountModuleFacade.createAccount(command);

        var amount = Money.of(BigDecimal.valueOf(50), Currency.PLN);

        // when
        accountModuleFacade.withdraw(account.id(), amount, userId);

        // then
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(-50));
    }
}
