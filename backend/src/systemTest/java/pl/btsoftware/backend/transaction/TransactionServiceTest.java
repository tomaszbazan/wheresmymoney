package pl.btsoftware.backend.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.TransactionAlreadyDeletedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionTooLongException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.UserId;

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

    @Autowired
    private UsersModuleFacade usersModuleFacade;

    private String uniqueAccountName() {
        return "Account-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private UserId createTestUser() {
        var timestamp = System.currentTimeMillis();
        var command = new RegisterUserCommand(
                "test-auth-id-" + timestamp,
                "test" + timestamp + "@example.com",
                "Test User",
                "Test Group " + timestamp,
                null
        );
        var user = usersModuleFacade.registerUser(command);
        return user.id();
    }

    @Test
    void shouldCreateIncomeTransactionAndUpdateAccountBalance() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Salary payment",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(accountId.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("100.00"));
        assertThat(transaction.type()).isEqualTo(INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.categoryId()).isNotNull();
        assertThat(transaction.amount().currency()).isEqualTo(PLN);
        assertThat(transaction.tombstone().isDeleted()).isFalse();

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldCreateExpenseTransactionAndUpdateAccountBalance() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), Money.of(new BigDecimal("200.00"), PLN), INCOME, userId);

        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Grocery shopping",
                now(),
                TransactionType.EXPENSE,
                CategoryId.generate(),
                userId
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldAllowTransactionWithEmptyDescription() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isEqualTo("");
    }

    @Test
    void shouldAllowTransactionWithNullDescription() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                null,
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var longDescription = "A".repeat(201);
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                longDescription,
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionDescriptionTooLongException.class)
                .hasMessage("Description cannot exceed 200 characters");
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesDontMatch() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), EUR),
                "Payment",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionCurrencyMismatchException.class)
                .hasMessage("Transaction currency (EUR) must match account currency (PLN)");
    }

    @Test
    void shouldRetrieveTransactionById() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var retrievedTransaction = transactionService.getTransactionById(createdTransaction.id(), user.groupId());

        // Then
        assertThat(retrievedTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(retrievedTransaction.description()).isEqualTo(createdTransaction.description());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        var nonExistentId = TransactionId.generate();

        // When & Then
        var userId = createTestUser();
        var user = usersModuleFacade.findUserOrThrow(userId);
        assertThatThrownBy(() -> transactionService.getTransactionById(nonExistentId, user.groupId()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldRetrieveAllTransactions() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command1 = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction 1",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var command2 = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Transaction 2",
                now(),
                TransactionType.EXPENSE,
                CategoryId.generate(),
                userId
        );

        var transaction1 = transactionService.createTransaction(command1);
        var transaction2 = transactionService.createTransaction(command2);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var allTransactions = transactionService.getAllTransactions(user.groupId());

        // Then
        assertThat(allTransactions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction1.id()));
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction2.id()));
    }

    @Test
    void shouldRetrieveTransactionsByAccountId() {
        // Given
        var userId = createTestUser();
        var account1Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var account2Id = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));

        var command1 = new CreateTransactionCommand(
                account1Id.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction for account 1",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var command2 = new CreateTransactionCommand(
                account2Id.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Transaction for account 2",
                now(),
                TransactionType.EXPENSE,
                CategoryId.generate(),
                userId
        );

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account1Transactions = transactionService.getTransactionsByAccountId(account1Id.id(), user.groupId());

        // Then
        assertThat(account1Transactions).hasSize(1);
        assertThat(account1Transactions.getFirst().description()).isEqualTo("Transaction for account 1");
    }

    @Test
    void shouldUpdateTransactionAmountAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Original transaction",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                Money.of(new BigDecimal("150.00"), PLN),
                null,
                null
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.amount().value()).isEqualTo(new BigDecimal("150.00"));
        assertThat(updatedTransaction.description()).isEqualTo("Original transaction");
        assertThat(updatedTransaction.categoryId()).isNotNull();

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Original description",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                "Updated description",
                null
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.description()).isEqualTo("Updated description");
        assertThat(updatedTransaction.amount().value()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand = new UpdateTransactionCommand(
                transaction.id(),
                null,
                null,
                CategoryId.generate()
        );

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.categoryId()).isNotNull();
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();
        var userId = createTestUser();
        accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var updateCommand = new UpdateTransactionCommand(
                nonExistentId,
                Money.of(new BigDecimal("100.00"), PLN),
                null,
                null
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand, userId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldDeleteTransactionAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction to delete",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        var user = usersModuleFacade.findUserOrThrow(userId);
        var deletedTransaction = transactionRepository.findByIdIncludingDeleted(transaction.id(), user.groupId()).orElseThrow();
        assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDeleteExpenseTransactionAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        accountModuleFacade.addTransaction(accountId.id(), TransactionId.generate(), new Money(new BigDecimal("200.00"), PLN), INCOME, userId);

        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("50.00"), PLN),
                "Expense to delete",
                now(),
                TransactionType.EXPENSE,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();
        var userId = createTestUser();
        accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentId, userId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlreadyDeletedTransaction() {
        // Given
        var userId = createTestUser();
        var accountId = accountModuleFacade.createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand = new CreateTransactionCommand(
                accountId.id(),
                Money.of(new BigDecimal("100.00"), PLN),
                "Transaction to delete twice",
                now(),
                INCOME,
                CategoryId.generate(),
                userId
        );
        var transaction = transactionService.createTransaction(createCommand);
        transactionService.deleteTransaction(transaction.id(), userId);

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(transaction.id(), userId))
                .isInstanceOf(TransactionAlreadyDeletedException.class)
                .hasMessage("Transaction with id " + transaction.id().value() + " is already deleted");
    }
}
