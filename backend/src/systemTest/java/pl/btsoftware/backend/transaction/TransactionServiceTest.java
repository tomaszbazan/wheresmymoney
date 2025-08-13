package pl.btsoftware.backend.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.Currency;
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
import static org.junit.jupiter.api.Assertions.*;
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
                new BigDecimal("100.00"),
                "Salary payment",
                now(),
                INCOME,
                "Salary",
                PLN
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertNotNull(transaction.id());
        assertEquals(accountId.id(), transaction.accountId());
        assertEquals(new BigDecimal("100.00"), transaction.amount().value());
        assertEquals(INCOME, transaction.type());
        assertEquals("Salary payment", transaction.description());
        assertEquals("Salary", transaction.category());
        assertEquals(PLN, transaction.amount().currency());
        assertFalse(transaction.tombstone().isDeleted());

        var account = accountModuleFacade.getAccount(accountId.id());
        assertEquals(new BigDecimal("100.00"), account.balance().value());
    }

    @Test
    void shouldCreateExpenseTransactionAndUpdateAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), Money.of(new BigDecimal("200.00"), PLN), INCOME);

        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Grocery shopping",
                now(),
                TransactionType.EXPENSE,
                "Food",
                PLN
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertNotNull(transaction.id());
        assertEquals(TransactionType.EXPENSE, transaction.type());
        assertEquals("Grocery shopping", transaction.description());

        var account = accountModuleFacade.getAccount(accountId.id());
        assertEquals(new BigDecimal("150.00"), account.balance().value());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "",
                now(),
                INCOME,
                "Category",
                PLN
        );

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.createTransaction(command)
        );
        assertEquals("Description must be between 1 and 200 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var longDescription = "A".repeat(201);
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                longDescription,
                now(),
                INCOME,
                "Category",
                PLN
        );

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.createTransaction(command)
        );
        assertEquals("Description must be between 1 and 200 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesDontMatch() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Payment",
                now(),
                INCOME,
                "Category",
                Currency.EUR
        );

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.createTransaction(command)
        );
        assertEquals("Transaction currency must match account currency", exception.getMessage());
    }

    @Test
    void shouldRetrieveTransactionById() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Test transaction",
                now(),
                INCOME,
                "Test",
                PLN
        );
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var retrievedTransaction = transactionService.getTransactionById(createdTransaction.id());

        // Then
        assertEquals(createdTransaction.id(), retrievedTransaction.id());
        assertEquals(createdTransaction.description(), retrievedTransaction.description());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        var nonExistentId = TransactionId.generate();

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionById(nonExistentId)
        );
        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldRetrieveAllTransactions() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var command1 = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction 1",
                now(),
                INCOME,
                "Test",
                PLN
        );
        var command2 = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Transaction 2",
                now(),
                TransactionType.EXPENSE,
                "Test",
                PLN
        );

        var transaction1 = transactionService.createTransaction(command1);
        var transaction2 = transactionService.createTransaction(command2);

        // When
        var allTransactions = transactionService.getAllTransactions();

        // Then
        assertTrue(allTransactions.size() >= 2);
        assertTrue(allTransactions.stream().anyMatch(t -> t.id().equals(transaction1.id())));
        assertTrue(allTransactions.stream().anyMatch(t -> t.id().equals(transaction2.id())));
    }

    @Test
    void shouldRetrieveTransactionsByAccountId() {
        // Given
        var account1Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var account2Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));

        var command1 = new CreateTransactionCommand(
                account1Id.id(),
                new BigDecimal("100.00"),
                "Transaction for account 1",
                now(),
                INCOME,
                "Test",
                PLN
        );
        var command2 = new CreateTransactionCommand(
                account2Id.id(),
                new BigDecimal("50.00"),
                "Transaction for account 2",
                now(),
                TransactionType.EXPENSE,
                "Test",
                PLN
        );

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var account1Transactions = transactionService.getTransactionsByAccountId(account1Id.id());

        // Then
        assertEquals(1, account1Transactions.size());
        assertEquals("Transaction for account 1", account1Transactions.getFirst().description());
    }

    @Test
    void shouldUpdateTransactionAmountAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Original transaction",
                now(),
                INCOME,
                "Original",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                new BigDecimal("150.00"),
                null,
                null,
                PLN
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertEquals(new BigDecimal("150.00"), updatedTransaction.amount().value());
        assertEquals("Original transaction", updatedTransaction.description());
        assertEquals("Original", updatedTransaction.category());

        var account = accountModuleFacade.getAccount(accountId.id());
        assertEquals(new BigDecimal("150.00"), account.balance().value());
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Original description",
                now(),
                INCOME,
                "Original",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                "Updated description",
                null,
                PLN
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertEquals("Updated description", updatedTransaction.description());
        assertEquals(new BigDecimal("100.00"), updatedTransaction.amount().value());
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Test transaction",
                now(),
                INCOME,
                "Original Category",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                null,
                "Updated Category",
                PLN
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertEquals("Updated Category", updatedTransaction.category());
        assertEquals("Test transaction", updatedTransaction.description());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();
        var updateCommand = new UpdateTransactionCommand(
                nonExistentId,
                new BigDecimal("100.00"),
                null,
                null,
                PLN
        );

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.updateTransaction(updateCommand)
        );
        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldDeleteTransactionAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction to delete",
                now(),
                INCOME,
                "Test",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var deletedTransaction = transactionRepository.findByIdIncludingDeleted(transaction.id()).orElseThrow();
        assertTrue(deletedTransaction.tombstone().isDeleted());

        var account = accountModuleFacade.getAccount(accountId.id());
        assertEquals(0, account.balance().value().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldDeleteExpenseTransactionAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), new Money(new BigDecimal("200.00"), PLN), INCOME);

        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Expense to delete",
                now(),
                TransactionType.EXPENSE,
                "Test",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var account = accountModuleFacade.getAccount(accountId.id());
        assertEquals(new BigDecimal("200.00"), account.balance().value());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.deleteTransaction(nonExistentId)
        );
        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlreadyDeletedTransaction() {
        // Given
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction to delete twice",
                now(),
                INCOME,
                "Test",
                PLN
        );
        var transaction = transactionService.createTransaction(createCommand);
        transactionService.deleteTransaction(transaction.id());

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.deleteTransaction(transaction.id())
        );
        assertEquals("Transaction not found", exception.getMessage());
    }
}
