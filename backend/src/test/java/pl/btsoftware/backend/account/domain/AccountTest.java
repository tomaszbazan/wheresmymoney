package pl.btsoftware.backend.account.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.backend.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

class AccountTest {

    private static Stream<Arguments> accountNameValidationTestCases() {
        return Stream.of(
                arguments(null, AccountNameEmptyException.class),
                arguments("", AccountNameEmptyException.class),
                arguments("   ", AccountNameEmptyException.class),
                arguments("a".repeat(101), AccountNameTooLongException.class),
                arguments("Invalid\nName", AccountNameInvalidCharactersException.class)
        );
    }

    @Test
    void shouldChangeName() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Original Name", PLN);

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
        var account = new Account(accountId, "Test Account", PLN);

        // when & then
        assertThatThrownBy(() -> account.changeName(null))
                .isInstanceOf(AccountNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToBlank() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", PLN);

        // when & then
        assertThatThrownBy(() -> account.changeName(""))
                .isInstanceOf(AccountNameEmptyException.class);
        assertThatThrownBy(() -> account.changeName("   "))
                .isInstanceOf(AccountNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", PLN);
        var tooLongName = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> account.changeName(tooLongName))
                .isInstanceOf(AccountNameTooLongException.class);
    }

    @Test
    void shouldAcceptNameWithMaximumLength() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", PLN);
        var maxLengthName = "a".repeat(100);

        // when
        var updatedAccount = account.changeName(maxLengthName);

        // then
        assertThat(updatedAccount.name()).isEqualTo(maxLengthName);
    }

    @Test
    void shouldThrowExceptionWhenNameContainsInvalidCharacters() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", PLN);

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
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account", PLN);
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
        var account = new Account(accountId, "Test Account", PLN);

        // then
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.balance().currency()).isEqualTo(PLN);
    }

    @Test
    void shouldCreateAccountWithValidCurrency() {
        // given
        var accountId = AccountId.generate();
        var validCurrencies = Currency.values();

        // when & then
        for (Currency currency : validCurrencies) {
            var account = new Account(accountId, "Test Account", currency);
            assertThat(account.balance().currency()).isEqualTo(currency);
        }
    }


    @ParameterizedTest
    @MethodSource("accountNameValidationTestCases")
    void shouldValidateNameInPrimaryConstructor(String name, Class<? extends Exception> expectedException) {
        // given
        var accountId = AccountId.generate();
        var balance = Money.of(TEN, PLN);
        var createdAt = OffsetDateTime.now();

        // when & then
        assertThatThrownBy(() -> new Account(accountId, name, balance, createdAt))
                .isInstanceOf(expectedException);
    }

    @Test
    void shouldAddIncomeTransactionAndUpdateBalance() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);

        // when
        var updatedAccount = account.addTransaction(transactionId, amount, INCOME);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(updatedAccount.transactionIds()).contains(transactionId);
        assertThat(updatedAccount.transactionIds()).hasSize(1);
    }

    @Test
    void shouldAddExpenseTransactionAndUpdateBalance() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(50), PLN);

        // when
        var updatedAccount = account.addTransaction(transactionId, amount, EXPENSE);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(-50));
        assertThat(updatedAccount.transactionIds()).contains(transactionId);
        assertThat(updatedAccount.transactionIds()).hasSize(1);
    }

    @Test
    void shouldAddMultipleTransactions() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId1 = TransactionId.generate();
        var transactionId2 = TransactionId.generate();
        var incomeAmount = Money.of(BigDecimal.valueOf(200), PLN);
        var expenseAmount = Money.of(BigDecimal.valueOf(75), PLN);

        // when
        var accountAfterIncome = account.addTransaction(transactionId1, incomeAmount, INCOME);
        var accountAfterBoth = accountAfterIncome.addTransaction(transactionId2, expenseAmount, EXPENSE);

        // then
        assertThat(accountAfterBoth.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(125));
        assertThat(accountAfterBoth.transactionIds()).contains(transactionId1, transactionId2);
        assertThat(accountAfterBoth.transactionIds()).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenAddingTransactionWithMismatchedCurrency() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), Currency.EUR);

        // when & then
        assertThatThrownBy(() -> account.addTransaction(transactionId, amount, INCOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction currency must match account currency");
    }

    @Test
    void shouldThrowExceptionForUnsupportedTransactionTypeInAddTransaction() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);

        // when & then
        assertThatThrownBy(() -> account.addTransaction(transactionId, amount, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRemoveIncomeTransactionAndUpdateBalance() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var accountWithTransaction = account.addTransaction(transactionId, amount, INCOME);

        // when
        var updatedAccount = accountWithTransaction.removeTransaction(transactionId, amount, INCOME);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedAccount.transactionIds()).doesNotContain(transactionId);
        assertThat(updatedAccount.transactionIds()).isEmpty();
    }

    @Test
    void shouldRemoveExpenseTransactionAndUpdateBalance() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(50), PLN);
        var accountWithTransaction = account.addTransaction(transactionId, amount, EXPENSE);

        // when
        var updatedAccount = accountWithTransaction.removeTransaction(transactionId, amount, EXPENSE);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updatedAccount.transactionIds()).doesNotContain(transactionId);
        assertThat(updatedAccount.transactionIds()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenRemovingTransactionWithMismatchedCurrency() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), Currency.EUR);

        // when & then
        assertThatThrownBy(() -> account.removeTransaction(transactionId, amount, INCOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction currency must match account currency");
    }

    @Test
    void shouldThrowExceptionForUnsupportedTransactionTypeInRemoveTransaction() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);

        // when & then
        assertThatThrownBy(() -> account.removeTransaction(transactionId, amount, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldChangeIncomeTransactionAmount() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var oldAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(150), PLN);
        var accountWithTransaction = account.addTransaction(transactionId, oldAmount, INCOME);

        // when
        var updatedAccount = accountWithTransaction.changeTransaction(transactionId, oldAmount, newAmount, INCOME);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(updatedAccount.transactionIds()).contains(transactionId);
        assertThat(updatedAccount.transactionIds()).hasSize(1);
    }

    @Test
    void shouldChangeExpenseTransactionAmount() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var oldAmount = Money.of(BigDecimal.valueOf(75), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var accountWithTransaction = account.addTransaction(transactionId, oldAmount, EXPENSE);

        // when
        var updatedAccount = accountWithTransaction.changeTransaction(transactionId, oldAmount, newAmount, EXPENSE);

        // then
        assertThat(updatedAccount.balance().value()).isEqualByComparingTo(BigDecimal.valueOf(-100));
        assertThat(updatedAccount.transactionIds()).contains(transactionId);
        assertThat(updatedAccount.transactionIds()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenChangingNonExistentTransaction() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var oldAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(150), PLN);

        // when & then
        assertThatThrownBy(() -> account.changeTransaction(transactionId, oldAmount, newAmount, INCOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction ID not found in account");
    }

    @Test
    void shouldThrowExceptionWhenChangingTransactionWithMismatchedCurrency() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var oldAmount = Money.of(BigDecimal.valueOf(100), Currency.EUR);
        var newAmount = Money.of(BigDecimal.valueOf(150), Currency.EUR);

        // when & then
        assertThatThrownBy(() -> account.changeTransaction(transactionId, oldAmount, newAmount, INCOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction currency must match account currency");
    }

    @Test
    void shouldThrowExceptionForUnsupportedTransactionTypeInChangeTransaction() {
        // given
        var account = new Account(AccountId.generate(), "Test Account", PLN);
        var transactionId = TransactionId.generate();
        var oldAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(150), PLN);
        var accountWithTransaction = account.addTransaction(transactionId, oldAmount, INCOME);

        // when & then
        assertThatThrownBy(() -> accountWithTransaction.changeTransaction(transactionId, oldAmount, newAmount, null))
                .isInstanceOf(NullPointerException.class);
    }
}
