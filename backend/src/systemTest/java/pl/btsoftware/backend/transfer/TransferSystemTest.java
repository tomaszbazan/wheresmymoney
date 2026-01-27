package pl.btsoftware.backend.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.transfer.application.CreateTransferCommand;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class TransferSystemTest {

    @Autowired private UsersModuleFacade usersModuleFacade;

    @Autowired private AccountModuleFacade accountModuleFacade;

    @Autowired private TransferModuleFacade transferModuleFacade;

    @Test
    void shouldPerformTransferBetweenSameCurrencyAccounts() {
        // Given
        var user = registerUser();
        var sourceAccount = createAccount(user.id(), "Source PLN", PLN);
        var targetAccount = createAccount(user.id(), "Target PLN", PLN);

        // When
        var command =
                new CreateTransferCommand(
                        sourceAccount.id(),
                        targetAccount.id(),
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        "Transfer PLN-PLN",
                        user.id());

        var transfer = transferModuleFacade.createTransfer(command);

        // Then
        assertThat(transfer.id()).isNotNull();
        assertThat(transfer.exchangeRate().rate()).isEqualByComparingTo(BigDecimal.ONE);

        verifyAccountBalance(sourceAccount.id(), user.id(), "-100.00");
        verifyAccountBalance(targetAccount.id(), user.id(), "100.00");
    }

    @Test
    void shouldPerformCrossCurrencyTransfer() {
        // Given
        var user = registerUser();
        var sourceAccount = createAccount(user.id(), "Source PLN", PLN);
        var targetAccount = createAccount(user.id(), "Target EUR", EUR);

        // When
        var command =
                new CreateTransferCommand(
                        sourceAccount.id(),
                        targetAccount.id(),
                        new BigDecimal("100.00"), // 100 PLN
                        new BigDecimal("25.00"), // 25 EUR
                        "Transfer PLN-EUR",
                        user.id());

        var transfer = transferModuleFacade.createTransfer(command);

        // Then
        assertThat(transfer.id()).isNotNull();
        assertThat(transfer.exchangeRate().rate())
                .isEqualByComparingTo(new BigDecimal("0.25")); // 25 / 100 = 0.25

        verifyAccountBalance(sourceAccount.id(), user.id(), "-100.00");
        verifyAccountBalance(targetAccount.id(), user.id(), "25.00");
    }

    private User registerUser() {
        var timestamp = System.currentTimeMillis();
        var command =
                new RegisterUserCommand(
                        "transfer-test-" + timestamp,
                        "transfer" + timestamp + "@example.com",
                        "Transfer User",
                        "Transfer Group " + timestamp,
                        null);
        return usersModuleFacade.registerUser(command);
    }

    private Account createAccount(UserId userId, String name, Currency currency) {
        var command = new CreateAccountCommand(name, currency, userId);
        return accountModuleFacade.createAccount(command);
    }

    private void verifyAccountBalance(AccountId accountId, UserId userId, String expectedBalance) {
        var account = accountModuleFacade.getAccount(accountId, userId);
        assertThat(account.balance().value()).isEqualByComparingTo(new BigDecimal(expectedBalance));
    }
}
