package pl.btsoftware.backend.account.domain;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pl.btsoftware.backend.shared.Currency.PLN;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.users.infrastructure.api.UserView;

class AccountTest {

    private static Stream<Arguments> accountNameValidationTestCases() {
        return Stream.of(
                arguments(null, AccountNameEmptyException.class),
                arguments("", AccountNameEmptyException.class),
                arguments("   ", AccountNameEmptyException.class),
                arguments("a".repeat(101), AccountNameTooLongException.class),
                arguments("Invalid\nName", AccountNameInvalidCharactersException.class));
    }

    @Test
    void shouldChangeName() {
        // given
        var accountId = AccountId.generate();
        var account =
                new Account(accountId, "Original Name", PLN, Instancio.create(UserView.class));

        // when
        var updatedAccount = account.changeName("New Name");

        // then
        assertThat(updatedAccount.name()).isEqualTo("New Name");
        assertThat(updatedAccount.id()).isEqualTo(accountId);
        // Original account should be unchanged (immutability check)
        assertThat(account.name()).isEqualTo("Original Name");
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToNull() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();

        // when & then
        assertThatThrownBy(() -> account.changeName(null))
                .isInstanceOf(AccountNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToBlank() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();

        // when & then
        assertThatThrownBy(() -> account.changeName(""))
                .isInstanceOf(AccountNameEmptyException.class);
        assertThatThrownBy(() -> account.changeName("   "))
                .isInstanceOf(AccountNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();
        var tooLongName = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> account.changeName(tooLongName))
                .isInstanceOf(AccountNameTooLongException.class);
    }

    @Test
    void shouldAcceptNameWithMaximumLength() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();
        var maxLengthName = "a".repeat(100);

        // when
        var updatedAccount = account.changeName(maxLengthName);

        // then
        assertThat(updatedAccount.name()).isEqualTo(maxLengthName);
    }

    @Test
    void shouldThrowExceptionWhenNameContainsInvalidCharacters() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();

        // when & then
        assertThatThrownBy(() -> account.changeName("Invalid\nName"))
                .isInstanceOf(AccountNameInvalidCharactersException.class);
        assertThatThrownBy(() -> account.changeName("Invalid\tName"))
                .isInstanceOf(AccountNameInvalidCharactersException.class);
        assertThatThrownBy(() -> account.changeName("Invalid\\Name"))
                .isInstanceOf(AccountNameInvalidCharactersException.class);
    }

    @Test
    void shouldAcceptNameWithValidSpecialCharacters() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();
        var nameWithSpecialChars = "Valid Name 123 ĄŚŻŹĆÓŃĘ -_@?!.#";

        // when
        var updatedAccount = account.changeName(nameWithSpecialChars);

        // then
        assertThat(updatedAccount.name()).isEqualTo(nameWithSpecialChars);
    }

    @Test
    void shouldHaveZeroBalanceByDefault() {
        // when
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        PLN,
                        Instancio.create(UserView.class));

        // then
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.balance().currency()).isEqualTo(PLN);
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void shouldCreateAccountWithValidCurrency(Currency currency) {
        // when
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        currency,
                        Instancio.create(UserView.class));

        // then
        assertThat(account.balance().currency()).isEqualTo(currency);
    }

    @ParameterizedTest
    @MethodSource("accountNameValidationTestCases")
    void shouldValidateNameInPrimaryConstructor(
            String name, Class<? extends Exception> expectedException) {
        // given
        var accountId = AccountId.generate();
        var balance = Money.of(TEN, PLN);

        // when & then
        assertThatThrownBy(
                        () ->
                                new Account(
                                        accountId,
                                        name,
                                        balance,
                                        Instancio.create(AuditInfo.class)))
                .isInstanceOf(expectedException);
    }

    @Test
    void shouldDepositAndUpdateBalance() {
        // given
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        PLN,
                        Instancio.create(UserView.class));
        var amount = Money.of(BigDecimal.valueOf(100), PLN);

        // when
        var updatedAccount = account.deposit(amount);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldWithdrawAndUpdateBalance() {
        // given
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        PLN,
                        Instancio.create(UserView.class));
        var amount = Money.of(BigDecimal.valueOf(50), PLN);

        // when
        var updatedAccount = account.withdraw(amount);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(-50));
    }

    @Test
    void shouldPerformMultipleOperations() {
        // given
        var account =
                new Account(
                        AccountId.generate(),
                        "Test Account",
                        PLN,
                        Instancio.create(UserView.class));
        var depositAmount = Money.of(BigDecimal.valueOf(200), PLN);
        var withdrawAmount = Money.of(BigDecimal.valueOf(75), PLN);

        // when
        var accountAfterDeposit = account.deposit(depositAmount);
        var accountAfterBoth = accountAfterDeposit.withdraw(withdrawAmount);

        // then
        assertThat(accountAfterBoth.balance().value())
                .isEqualByComparingTo(BigDecimal.valueOf(125));
    }

    @Test
    void shouldThrowExceptionWhenDepositingWithMismatchedCurrency() {
        // given
        var account =
                Instancio.of(Account.class).set(field(Account::balance), Money.zero()).create();
        var amount = Money.of(BigDecimal.valueOf(100), Currency.EUR);

        // when & then
        assertThatThrownBy(() -> account.deposit(amount))
                .isInstanceOf(TransactionCurrencyMismatchException.class);
    }
}
