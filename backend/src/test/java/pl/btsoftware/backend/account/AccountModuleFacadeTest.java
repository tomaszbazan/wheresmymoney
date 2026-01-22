package pl.btsoftware.backend.account;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

class AccountModuleFacadeTest {

    private AccountModuleFacade accountModuleFacade;
    private AccountService accountService;
    private UsersModuleFacade usersModuleFacade;

    @BeforeEach
    void setUp() {
        var accountRepository = new InMemoryAccountRepository();
        var transactionQueryFacade = Mockito.mock(TransactionQueryFacade.class);
        usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        accountService = new AccountService(accountRepository, usersModuleFacade, transactionQueryFacade);
        accountModuleFacade = new AccountModuleFacade(accountService, usersModuleFacade);
    }

    @Test
    void shouldCreateAccount() {
        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::currency), Currency.PLN)
                .create();
        var user = Instancio.of(User.class)
                .set(field(User::id), command.userId())
                .create();
        when(usersModuleFacade.findUserOrThrow(command.userId())).thenReturn(user);

        var account = accountModuleFacade.createAccount(command);

        assertThat(account.name()).isEqualTo(command.name());
        assertThat(account.balance().currency()).isEqualTo(Currency.PLN);
    }

    @Test
    void shouldGetAccounts() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .create();
        accountModuleFacade.createAccount(command);

        var accounts = accountModuleFacade.getAccounts(userId);

        assertThat(accounts).hasSize(1);
    }

    @Test
    void shouldGetAccountByUserIdAndAccountId() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .create();
        var account = accountModuleFacade.createAccount(command);

        var retrievedAccount = accountModuleFacade.getAccount(account.id(), userId);

        assertThat(retrievedAccount.id()).isEqualTo(account.id());
    }

    @Test
    void shouldGetAccountByGroupIdAndAccountId() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .create();
        var account = accountModuleFacade.createAccount(command);

        var retrievedAccount = accountModuleFacade.getAccount(account.id(), groupId);

        assertThat(retrievedAccount.id()).isEqualTo(account.id());
    }

    @Test
    void shouldUpdateAccount() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var createCommand = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .create();
        var account = accountModuleFacade.createAccount(createCommand);

        var updateCommand = new UpdateAccountCommand(account.id(), "Updated Name");
        var updatedAccount = accountModuleFacade.updateAccount(updateCommand, userId);

        assertThat(updatedAccount.name()).isEqualTo("Updated Name");
    }

    @Test
    void shouldDeleteAccount() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .create();
        var account = accountModuleFacade.createAccount(command);

        accountModuleFacade.deleteAccount(account.id(), userId);

        var accounts = accountModuleFacade.getAccounts(userId);
        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldAddTransaction() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .set(field(CreateAccountCommand::currency), Currency.PLN)
                .create();
        var account = accountModuleFacade.createAccount(command);

        var amount = Money.of(BigDecimal.valueOf(100), Currency.PLN);
        accountModuleFacade.addTransaction(account.id(), amount, TransactionType.INCOME, userId);

        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldRemoveTransaction() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .set(field(CreateAccountCommand::currency), Currency.PLN)
                .create();
        var account = accountModuleFacade.createAccount(command);
        var amount = Money.of(BigDecimal.valueOf(100), Currency.PLN);
        accountModuleFacade.addTransaction(account.id(), amount, TransactionType.INCOME, userId);

        accountModuleFacade.removeTransaction(account.id(), amount, TransactionType.INCOME, userId);

        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldChangeTransaction() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateAccountCommand.class)
                .set(field(CreateAccountCommand::userId), userId)
                .set(field(CreateAccountCommand::currency), Currency.PLN)
                .create();
        var account = accountModuleFacade.createAccount(command);
        var oldAmount = Money.of(BigDecimal.valueOf(100), Currency.PLN);
        accountModuleFacade.addTransaction(account.id(), oldAmount, TransactionType.INCOME, userId);

        var newAmount = Money.of(BigDecimal.valueOf(200), Currency.PLN);
        accountModuleFacade.changeTransaction(account.id(), oldAmount, newAmount, TransactionType.INCOME, userId);

        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }
}
