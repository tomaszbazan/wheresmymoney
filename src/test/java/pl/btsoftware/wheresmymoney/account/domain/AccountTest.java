package pl.btsoftware.wheresmymoney.account.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.btsoftware.wheresmymoney.account.domain.error.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AccountTest {

    private static Stream<Arguments> accountNameValidationTestCases() {
        return Stream.of(
                arguments(null, AccountNameEmptyException.class),
                arguments("", AccountNameEmptyException.class),
                arguments("   ", AccountNameEmptyException.class),
                arguments("a".repeat(256), AccountNameTooLongException.class),
                arguments("Invalid\nName", AccountNameInvalidCharactersException.class)
        );
    }

    @Test
    void shouldChangeName() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Original Name", "PLN");

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
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");

        // when & then
        assertThrows(AccountNameEmptyException.class, () -> account.changeName(null));
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToBlank() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");

        // when & then
        assertThrows(AccountNameEmptyException.class, () -> account.changeName(""));
        assertThrows(AccountNameEmptyException.class, () -> account.changeName("   "));
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var tooLongName = "a".repeat(256);

        // when & then
        assertThrows(AccountNameTooLongException.class, () -> account.changeName(tooLongName));
    }

    @Test
    void shouldAcceptNameWithMaximumLength() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var maxLengthName = "a".repeat(255);

        // when
        var updatedAccount = account.changeName(maxLengthName);

        // then
        assertThat(updatedAccount.name()).isEqualTo(maxLengthName);
    }

    @Test
    void shouldThrowExceptionWhenNameContainsInvalidCharacters() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");

        // when & then
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\nName"));
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\tName"));
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\\Name"));
    }

    @Test
    void shouldAcceptNameWithValidSpecialCharacters() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var nameWithSpecialChars = "Valid Name 123 !@#$%^&*()_+-=[]{}|;:'\",.<>/?";

        // when
        var updatedAccount = account.changeName(nameWithSpecialChars);

        // then
        assertThat(updatedAccount.name()).isEqualTo(nameWithSpecialChars);
    }

    @Test
    void shouldHaveZeroBalanceByDefault() {
        // given
        var accountId = AccountId.generate();

        // when
        var account = new Account(accountId, "Test Account", "PLN");

        // then
        assertThat(account.balance().amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.balance().currency()).isEqualTo("PLN");
    }

    @Test
    void shouldCreateAccountWithValidCurrency() {
        // given
        var accountId = AccountId.generate();
        var validCurrencies = Money.SUPPORTED_CURRENCIES;

        // when & then
        for (String currency : validCurrencies) {
            var account = new Account(accountId, "Test Account", currency);
            assertThat(account.balance().currency()).isEqualTo(currency);
        }
    }

    @Test
    void shouldAddExpense() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var expense = new Expense(
                ExpenseId.generate(),
                accountId,
                new Money(new BigDecimal("50.00"), "PLN"),
                "Test Expense",
                OffsetDateTime.now()
        );

        // when
        var updatedAccount = account.addExpense(expense);

        // then
        assertThat(updatedAccount.id()).isEqualTo(accountId);
        assertThat(updatedAccount.name()).isEqualTo("Test Account");
        assertThat(updatedAccount.balance().amount()).isEqualByComparingTo(new BigDecimal("-50.00"));
        assertThat(updatedAccount.balance().currency()).isEqualTo("PLN");
        // Original account should be unchanged (immutability check)
        assertThat(account).isNotSameAs(updatedAccount);
    }

    @Test
    void shouldThrowExceptionWhenAddingNullExpense() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");

        // when & then
        assertThrows(ExpenseIdNullException.class, () -> account.addExpense(null));
    }

    @Test
    void shouldRemoveExpense() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var expense = new Expense(
                ExpenseId.generate(),
                accountId,
                new Money(new BigDecimal("50.00"), "PLN"),
                "Test Expense",
                OffsetDateTime.now()
        );

        // when
        var updatedAccount = account.removeExpense(expense);

        // then
        assertThat(updatedAccount.id()).isEqualTo(accountId);
        assertThat(updatedAccount.name()).isEqualTo("Test Account");
        assertThat(updatedAccount.balance().amount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(updatedAccount.balance().currency()).isEqualTo("PLN");
        // Original account should be unchanged (immutability check)
        assertThat(account).isNotSameAs(updatedAccount);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNullExpense() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");

        // when & then
        assertThrows(ExpenseIdNullException.class, () -> account.removeExpense(null));
    }

    @Test
    void shouldThrowExceptionWhenAddingExpenseWithDifferentCurrency() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var expense = new Expense(
                ExpenseId.generate(),
                accountId,
                new Money(new BigDecimal("50.00"), "EUR"),
                "Test Expense",
                OffsetDateTime.now()
        );

        // when & then
        assertThrows(CurrencyMismatchException.class, () -> account.addExpense(expense));
    }

    @Test
    void shouldThrowExceptionWhenRemovingExpenseWithDifferentCurrency() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", "PLN");
        var expense = new Expense(
                ExpenseId.generate(),
                accountId,
                new Money(new BigDecimal("50.00"), "USD"),
                "Test Expense",
                OffsetDateTime.now()
        );

        // when & then
        assertThrows(CurrencyMismatchException.class, () -> account.removeExpense(expense));
    }

    @ParameterizedTest
    @MethodSource("accountNameValidationTestCases")
    void shouldValidateNameInPrimaryConstructor(String name, Class<? extends Exception> expectedException) {
        // given
        var accountId = AccountId.generate();
        var balance = Money.of(TEN, "PLN");
        var createdAt = OffsetDateTime.now();

        // when & then
        assertThrows(expectedException, () -> new Account(accountId, name, balance, createdAt));
    }
}
