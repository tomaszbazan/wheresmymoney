package pl.btsoftware.backend.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

@SystemTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountModuleFacade accountModuleFacade;

    private String uniqueAccountName() {
        return "Account-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void shouldCreateIncomeTransactionAndUpdateAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Salary payment",
                now(),
                INCOME,
                "Salary"
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(accountId.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("100.00"));
        assertThat(transaction.type()).isEqualTo(INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.category()).isEqualTo("Salary");
        assertThat(transaction.amount().currency()).isEqualTo(PLN);
        assertThat(transaction.tombstone().isDeleted()).isFalse();

        var account = accountModuleFacade.getAccount(accountId.id());
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldCreateExpenseTransactionAndUpdateAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), Money.of(new BigDecimal("200.00"), PLN), INCOME);

        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Grocery shopping",
                now(),
                TransactionType.EXPENSE,
                "Food"
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");

        var account = accountModuleFacade.getAccount(accountId.id());
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldAllowTransactionWithEmptyDescription() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "",
                now(),
                INCOME,
                "Category"
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isEqualTo("");
    }

    @Test
    void shouldAllowTransactionWithNullDescription() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                null,
                now(),
                INCOME,
                "Category"
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var longDescription = "A".repeat(201);
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                longDescription,
                now(),
                INCOME,
                "Category"
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description cannot exceed 200 characters");
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesDontMatch() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), EUR),
                "Payment",
                now(),
                INCOME,
                "Category"
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction currency must match account currency");
    }

    @Test
    void shouldRetrieveTransactionById() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                now(),
                INCOME,
                "Test"
        );
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var retrievedTransaction = transactionService.getTransactionById(createdTransaction.id());

        // Then
        assertThat(retrievedTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(retrievedTransaction.description()).isEqualTo(createdTransaction.description());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        var nonExistentId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    void shouldRetrieveAllTransactions() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command1 = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction 1",
                now(),
                INCOME,
                "Test"
        );
        var command2 = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Transaction 2",
                now(),
                TransactionType.EXPENSE,
                "Test"
        );

        var transaction1 = transactionService.createTransaction(command1);
        var transaction2 = transactionService.createTransaction(command2);

        // When
        var allTransactions = transactionService.getAllTransactions();

        // Then
        assertThat(allTransactions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction1.id()));
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction2.id()));
    }

    @Test
    void shouldRetrieveTransactionsByAccountId() {
        // Given
        var account1Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var account2Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));

        var command1 = new CreateTransactionCommand(
                account1Id.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction for account 1",
                now(),
                INCOME,
                "Test"
        );
        var command2 = new CreateTransactionCommand(
                account2Id.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Transaction for account 2",
                now(),
                TransactionType.EXPENSE,
                "Test"
        );

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var account1Transactions = transactionService.getTransactionsByAccountId(account1Id.id());

        // Then
        assertThat(account1Transactions).hasSize(1);
        assertThat(account1Transactions.getFirst().description()).isEqualTo("Transaction for account 1");
    }

    @Test
    void shouldUpdateTransactionAmountAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Original transaction",
                now(),
                INCOME,
                "Original"
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                Money.of(new BigDecimal("150.00"), PLN),
                null,
                null
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.amount().value()).isEqualTo(new BigDecimal("150.00"));
        assertThat(updatedTransaction.description()).isEqualTo("Original transaction");
        assertThat(updatedTransaction.category()).isEqualTo("Original");

        var account = accountModuleFacade.getAccount(accountId.id());
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Original description",
                now(),
                INCOME,
                "Original"
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                "Updated description",
                null
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.description()).isEqualTo("Updated description");
        assertThat(updatedTransaction.amount().value()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                now(),
                INCOME,
                "Original Category"
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                null,
                "Updated Category"
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.category()).isEqualTo("Updated Category");
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();
        var updateCommand = new UpdateTransactionCommand(
                nonExistentId,
                Money.of(new BigDecimal("100.00"), PLN),
                null,
                null
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    void shouldDeleteTransactionAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction to delete",
                now(),
                INCOME,
                "Test"
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var deletedTransaction = transactionRepository.findByIdIncludingDeleted(transaction.id()).orElseThrow();
        assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();

        var account = accountModuleFacade.getAccount(accountId.id());
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDeleteExpenseTransactionAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), new Money(new BigDecimal("200.00"), PLN), INCOME);

        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Expense to delete",
                now(),
                TransactionType.EXPENSE,
                "Test"
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var account = accountModuleFacade.getAccount(accountId.id());
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlreadyDeletedTransaction() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction to delete twice",
                now(),
                INCOME,
                "Test"
        );
        var transaction = transactionService.createTransaction(createCommand);
        transactionService.deleteTransaction(transaction.id());

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(transaction.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction not found");
    }
}