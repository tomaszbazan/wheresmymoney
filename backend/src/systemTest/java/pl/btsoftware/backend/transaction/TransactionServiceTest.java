package pl.btsoftware.backend.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.TransactionId;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Salary payment",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Salary",
                Currency.PLN
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertNotNull(transaction.id());
        assertEquals(accountId.id(), transaction.accountId());
        assertEquals(new BigDecimal("100.00"), transaction.amount().value());
        assertEquals(TransactionType.INCOME, transaction.type());
        assertEquals("Salary payment", transaction.description());
        assertEquals("Salary", transaction.category());
        assertEquals(Currency.PLN, transaction.amount().currency());
        assertFalse(transaction.tombstone().isDeleted());

        var account = accountModuleFacade.getAccount(accountId.id().value());
        assertEquals(new BigDecimal("100.00"), account.balance().value());
    }

    @Test
    void shouldCreateExpenseTransactionAndUpdateAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        accountModuleFacade.addTransaction(accountId.id().value(), new BigDecimal("200.00"), TransactionType.INCOME.name());

        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Grocery shopping",
                OffsetDateTime.now(),
                TransactionType.EXPENSE,
                "Food",
                Currency.PLN
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertNotNull(transaction.id());
        assertEquals(TransactionType.EXPENSE, transaction.type());
        assertEquals("Grocery shopping", transaction.description());

        var account = accountModuleFacade.getAccount(accountId.id().value());
        assertEquals(new BigDecimal("150.00"), account.balance().value());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
        // Given
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Category",
                Currency.PLN
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var longDescription = "A".repeat(201);
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                longDescription,
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Category",
                Currency.PLN
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Payment",
                OffsetDateTime.now(),
                TransactionType.INCOME,
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var command = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Test transaction",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Test",
                Currency.PLN
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var command1 = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction 1",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Test",
                Currency.PLN
        );
        var command2 = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Transaction 2",
                OffsetDateTime.now(),
                TransactionType.EXPENSE,
                "Test",
                Currency.PLN
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
        var account1Id = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var account2Id = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));

        var command1 = new CreateTransactionCommand(
                account1Id.id(),
                new BigDecimal("100.00"),
                "Transaction for account 1",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Test",
                Currency.PLN
        );
        var command2 = new CreateTransactionCommand(
                account2Id.id(),
                new BigDecimal("50.00"),
                "Transaction for account 2",
                OffsetDateTime.now(),
                TransactionType.EXPENSE,
                "Test",
                Currency.PLN
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Original transaction",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Original",
                Currency.PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                new BigDecimal("150.00"),
                null,
                null
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertEquals(new BigDecimal("150.00"), updatedTransaction.amount().value());
        assertEquals("Original transaction", updatedTransaction.description());
        assertEquals("Original", updatedTransaction.category());

        var account = accountModuleFacade.getAccount(accountId.id().value());
        assertEquals(new BigDecimal("150.00"), account.balance().value());
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Original description",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Original",
                Currency.PLN
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
        assertEquals("Updated description", updatedTransaction.description());
        assertEquals(new BigDecimal("100.00"), updatedTransaction.amount().value());
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Test transaction",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Original Category",
                Currency.PLN
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
                null
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction to delete",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Test",
                Currency.PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var deletedTransaction = transactionRepository.findByIdIncludingDeleted(transaction.id()).orElseThrow();
        assertTrue(deletedTransaction.tombstone().isDeleted());

        var account = accountModuleFacade.getAccount(accountId.id().value());
        assertEquals(0, account.balance().value().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldDeleteExpenseTransactionAndAdjustAccountBalance() {
        // Given
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        accountModuleFacade.addTransaction(accountId.id().value(), new BigDecimal("200.00"), TransactionType.INCOME.name());

        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("50.00"),
                "Expense to delete",
                OffsetDateTime.now(),
                TransactionType.EXPENSE,
                "Test",
                Currency.PLN
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        var account = accountModuleFacade.getAccount(accountId.id().value());
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
        var accountId = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand(uniqueAccountName(), Currency.PLN));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                new BigDecimal("100.00"),
                "Transaction to delete twice",
                OffsetDateTime.now(),
                TransactionType.INCOME,
                "Test",
                Currency.PLN
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
