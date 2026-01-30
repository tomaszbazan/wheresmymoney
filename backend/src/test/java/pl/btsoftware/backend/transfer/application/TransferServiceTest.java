package pl.btsoftware.backend.transfer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transfer.domain.TransferRepository;
import pl.btsoftware.backend.transfer.domain.error.TransferToSameAccountException;
import pl.btsoftware.backend.transfer.infrastructure.persistance.InMemoryTransferRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

class TransferServiceTest {

    private TransferRepository transferRepository;
    private AccountModuleFacade accountModuleFacade;
    private UsersModuleFacade usersModuleFacade;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        this.transferRepository = new InMemoryTransferRepository();
        this.accountModuleFacade = Mockito.mock(AccountModuleFacade.class);
        this.usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        this.transferService = new TransferService(transferRepository, accountModuleFacade, usersModuleFacade);
    }

    private void mockUser(UserId userId, GroupId groupId) {
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);
    }

    private void mockAccount(AccountId accountId, GroupId groupId, Currency currency) {
        var account = Instancio.of(Account.class)
                .set(field(Account::id), accountId)
                .set(field(Account::balance), Money.zero(currency))
                .create();
        when(accountModuleFacade.getAccount(accountId, groupId)).thenReturn(account);
    }

    @Test
    void shouldExecuteTransferBetweenSameCurrencyAccounts() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        mockAccount(targetAccountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when
        var transfer = transferService.createTransfer(command);

        // then
        assertThat(transfer).isNotNull();
        assertThat(transfer.sourceAccountId()).isEqualTo(sourceAccountId);
        assertThat(transfer.targetAccountId()).isEqualTo(targetAccountId);
        assertThat(transfer.sourceAmount().value()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(transfer.targetAmount().value()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(transfer.exchangeRate().rate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(transfer.description()).isEqualTo("Test transfer");

        verify(accountModuleFacade)
                .withdraw(eq(sourceAccountId), eq(Money.of(new BigDecimal("100.00"), Currency.PLN)), eq(userId));
        verify(accountModuleFacade)
                .deposit(eq(targetAccountId), eq(Money.of(new BigDecimal("100.00"), Currency.PLN)), eq(userId));
    }

    @Test
    void shouldExecuteTransferBetweenDifferentCurrencyAccounts() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        mockAccount(targetAccountId, groupId, Currency.EUR);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("85.50"),
                "Cross-currency transfer",
                userId);

        // when
        var transfer = transferService.createTransfer(command);

        // then
        assertThat(transfer).isNotNull();
        assertThat(transfer.sourceAmount().currency()).isEqualTo(Currency.PLN);
        assertThat(transfer.targetAmount().currency()).isEqualTo(Currency.EUR);
        assertThat(transfer.sourceAmount().value()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(transfer.targetAmount().value()).isEqualByComparingTo(new BigDecimal("85.50"));
        assertThat(transfer.exchangeRate().rate()).isEqualByComparingTo(new BigDecimal("0.855000"));

        verify(accountModuleFacade)
                .withdraw(eq(sourceAccountId), eq(Money.of(new BigDecimal("100.00"), Currency.PLN)), eq(userId));
        verify(accountModuleFacade)
                .deposit(eq(targetAccountId), eq(Money.of(new BigDecimal("85.50"), Currency.EUR)), eq(userId));
    }

    @Test
    void shouldStoreTransferInRepository() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        mockAccount(targetAccountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when
        var transfer = transferService.createTransfer(command);

        // then
        var foundTransfer = transferRepository.findById(transfer.id(), groupId);
        assertThat(foundTransfer).isPresent();
        assertThat(foundTransfer.get()).isEqualTo(transfer);
    }

    @Test
    void shouldRejectTransferToSameAccount() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var accountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(accountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                accountId, accountId, new BigDecimal("100.00"), new BigDecimal("100.00"), "Invalid transfer", userId);

        // when & then
        assertThatThrownBy(() -> transferService.createTransfer(command))
                .isInstanceOf(TransferToSameAccountException.class);

        verify(accountModuleFacade, never()).withdraw(any(), any(), any());
        verify(accountModuleFacade, never()).deposit(any(), any(), any());
    }

    @Test
    void shouldRejectTransferWhenSourceAccountNotFound() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        when(accountModuleFacade.getAccount(sourceAccountId, groupId))
                .thenThrow(new AccountNotFoundException(sourceAccountId));
        mockAccount(targetAccountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when & then
        assertThatThrownBy(() -> transferService.createTransfer(command)).isInstanceOf(AccountNotFoundException.class);

        verify(accountModuleFacade, never()).withdraw(any(), any(), any());
        verify(accountModuleFacade, never()).deposit(any(), any(), any());
    }

    @Test
    void shouldRejectTransferWhenTargetAccountNotFound() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        when(accountModuleFacade.getAccount(targetAccountId, groupId))
                .thenThrow(new AccountNotFoundException(targetAccountId));

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when & then
        assertThatThrownBy(() -> transferService.createTransfer(command)).isInstanceOf(AccountNotFoundException.class);

        verify(accountModuleFacade, never()).withdraw(any(), any(), any());
        verify(accountModuleFacade, never()).deposit(any(), any(), any());
    }

    @Test
    void shouldUpdateBothAccountBalances() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        mockAccount(targetAccountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when
        transferService.createTransfer(command);

        // then
        var debitCaptor = ArgumentCaptor.forClass(Money.class);
        verify(accountModuleFacade).withdraw(eq(sourceAccountId), debitCaptor.capture(), eq(userId));
        assertThat(debitCaptor.getValue().value()).isEqualByComparingTo(new BigDecimal("100.00"));

        var creditCaptor = ArgumentCaptor.forClass(Money.class);
        verify(accountModuleFacade).deposit(eq(targetAccountId), creditCaptor.capture(), eq(userId));
        assertThat(creditCaptor.getValue().value()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldEnforceGroupIdSecurity() {
        // given
        var userId = new UserId("user1");
        var groupId = new GroupId(java.util.UUID.randomUUID());
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();

        mockUser(userId, groupId);
        mockAccount(sourceAccountId, groupId, Currency.PLN);
        mockAccount(targetAccountId, groupId, Currency.PLN);

        var command = new CreateTransferCommand(
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "Test transfer",
                userId);

        // when
        transferService.createTransfer(command);

        // then
        verify(accountModuleFacade).getAccount(sourceAccountId, groupId);
        verify(accountModuleFacade).getAccount(targetAccountId, groupId);
    }

    @Test
    void shouldRejectNullTargetAmount() {
        // given
        var sourceAccountId = AccountId.generate();
        var targetAccountId = AccountId.generate();
        var userId = new UserId("user1");

        // when & then
        assertThatThrownBy(() -> new CreateTransferCommand(
                        sourceAccountId, targetAccountId, new BigDecimal("100.00"), null, "Test transfer", userId))
                .isInstanceOf(NullPointerException.class);
    }
}
