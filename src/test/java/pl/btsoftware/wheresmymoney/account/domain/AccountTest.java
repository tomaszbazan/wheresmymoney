package pl.btsoftware.wheresmymoney.account.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameInvalidCharactersException;
import pl.btsoftware.wheresmymoney.account.domain.error.AccountNameTooLongException;
import pl.btsoftware.wheresmymoney.account.domain.error.ExpenseIdNullException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    void shouldAddExpenseId() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");
        var expenseId = ExpenseId.generate();

        // when
        var updatedAccount = account.addExpense(expenseId);

        // then
        assertThat(updatedAccount.getExpenseIds()).hasSize(1);
        assertThat(updatedAccount.getExpenseIds().getFirst()).isEqualTo(expenseId);
        assertThat(account.getExpenseIds()).isEmpty();
    }

    @Test
    void shouldAddMultipleExpenseIds() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");
        var expenseId1 = ExpenseId.generate();
        var expenseId2 = ExpenseId.generate();
        var expenseId3 = ExpenseId.generate();

        // when
        var updatedAccount = account
                .addExpense(expenseId1)
                .addExpense(expenseId2)
                .addExpense(expenseId3);

        // then
        assertThat(updatedAccount.getExpenseIds()).hasSize(3);
        assertThat(updatedAccount.getExpenseIds()).containsExactly(expenseId1, expenseId2, expenseId3);
    }

    @Test
    void shouldThrowExceptionWhenAddingNullExpenseId() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");

        // when & then
        assertThrows(ExpenseIdNullException.class, () -> account.addExpense(null));
    }

    @Test
    void shouldRemoveExpenseId() {
        // given
        var accountId = AccountId.generate();
        var expenseId = ExpenseId.generate();
        var account = new Account(accountId, "Test Account")
                .addExpense(expenseId);

        // when
        var updatedAccount = account.removeExpense(expenseId);

        // then
        assertThat(updatedAccount.getExpenseIds()).isEmpty();
        // Original account should be unchanged (immutability check)
        assertThat(account.getExpenseIds()).hasSize(1);
    }

    @Test
    void shouldRemoveOnlySpecifiedExpenseId() {
        // given
        var accountId = AccountId.generate();
        var expenseId1 = ExpenseId.generate();
        var expenseId2 = ExpenseId.generate();
        var expenseId3 = ExpenseId.generate();
        var account = new Account(accountId, "Test Account")
                .addExpense(expenseId1)
                .addExpense(expenseId2)
                .addExpense(expenseId3);

        // when
        var updatedAccount = account.removeExpense(expenseId2);

        // then
        assertThat(updatedAccount.getExpenseIds()).hasSize(2);
        assertThat(updatedAccount.getExpenseIds()).containsExactly(expenseId1, expenseId3);
    }

    @Test
    void shouldDoNothingWhenRemovingNonExistentExpenseId() {
        // given
        var accountId = AccountId.generate();
        var existingExpenseId = ExpenseId.generate();
        var nonExistentExpenseId = ExpenseId.generate();
        var account = new Account(accountId, "Test Account")
                .addExpense(existingExpenseId);

        // when
        var updatedAccount = account.removeExpense(nonExistentExpenseId);

        // then
        assertThat(updatedAccount.getExpenseIds()).hasSize(1);
        assertThat(updatedAccount.getExpenseIds()).containsExactly(existingExpenseId);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNullExpenseId() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");

        // when & then
        assertThrows(ExpenseIdNullException.class, () -> account.removeExpense(null));
    }

    @Test
    void shouldReturnUnmodifiableListOfExpenseIds() {
        // given
        var accountId = AccountId.generate();
        var expenseId = ExpenseId.generate();
        var account = new Account(accountId, "Test Account")
                .addExpense(expenseId);

        // when & then
        assertThrows(UnsupportedOperationException.class, () -> account.getExpenseIds().add(ExpenseId.generate()));
    }

    @Test
    void shouldChangeName() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Original Name");

        // when
        var updatedAccount = account.changeName("New Name");

        // then
        assertThat(updatedAccount.name()).isEqualTo("New Name");
        assertThat(updatedAccount.id()).isEqualTo(accountId);
        assertThat(updatedAccount.getExpenseIds()).isEmpty();
        // Original account should be unchanged (immutability check)
        assertThat(account.name()).isEqualTo("Original Name");
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToNull() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");

        // when & then
        assertThrows(AccountNameEmptyException.class, () -> account.changeName(null));
    }

    @Test
    void shouldThrowExceptionWhenChangingNameToBlank() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");

        // when & then
        assertThrows(AccountNameEmptyException.class, () -> account.changeName(""));
        assertThrows(AccountNameEmptyException.class, () -> account.changeName("   "));
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");
        var tooLongName = "a".repeat(256);

        // when & then
        assertThrows(AccountNameTooLongException.class, () -> account.changeName(tooLongName));
    }

    @Test
    void shouldAcceptNameWithMaximumLength() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");
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
        var account = new Account(accountId, "Test Account");

        // when & then
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\nName"));
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\tName"));
        assertThrows(AccountNameInvalidCharactersException.class, () -> account.changeName("Invalid\\Name"));
    }

    @Test
    void shouldAcceptNameWithValidSpecialCharacters() {
        // given
        var accountId = AccountId.generate();
        var account = new Account(accountId, "Test Account");
        var nameWithSpecialChars = "Valid Name 123 !@#$%^&*()_+-=[]{}|;:'\",.<>/?";

        // when
        var updatedAccount = account.changeName(nameWithSpecialChars);

        // then
        assertThat(updatedAccount.name()).isEqualTo(nameWithSpecialChars);
    }
}
