package pl.btsoftware.backend.transaction;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.EUR;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.audit.domain.AuditEntityType;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditOperation;
import pl.btsoftware.backend.audit.domain.EntityId;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.application.BillItemCommand;
import pl.btsoftware.backend.transaction.application.BulkCreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.BillItemDescriptionTooLongException;
import pl.btsoftware.backend.transaction.domain.error.TransactionAlreadyDeletedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.transaction.infrastructure.persistance.TransactionCommandFixture;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class TransactionServiceTest {

    @Autowired private TransactionService transactionService;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private AccountModuleFacade accountModuleFacade;

    @Autowired private UsersModuleFacade usersModuleFacade;

    @Autowired private CategoryModuleFacade categoryModuleFacade;

    @Autowired private pl.btsoftware.backend.audit.domain.AuditLogRepository auditLogRepository;

    private String uniqueAccountName() {
        return "Account-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private UserId createTestUser() {
        var timestamp = System.currentTimeMillis();
        var command =
                new RegisterUserCommand(
                        "test-auth-id-" + timestamp,
                        "test" + timestamp + "@example.com",
                        "Test User",
                        "Test Group " + timestamp,
                        null);
        var user = usersModuleFacade.registerUser(command);
        return user.id();
    }

    private CategoryId createIncomeCategory(UserId userId) {
        var command =
                new CreateCategoryCommand(
                        "Test Income", CategoryType.INCOME, Color.of("#4CAF50"), userId);
        var category = categoryModuleFacade.createCategory(command);
        return category.id();
    }

    private CategoryId createExpenseCategory(UserId userId) {
        var command =
                new CreateCategoryCommand(
                        "Test Expense", CategoryType.EXPENSE, Color.of("#FF5722"), userId);
        var category = categoryModuleFacade.createCategory(command);
        return category.id();
    }

    @Test
    void shouldCreateIncomeTransactionAndUpdateAccountBalance() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Salary payment",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(accountId.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("100.00"));
        assertThat(transaction.type()).isEqualTo(INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.bill()).isNotNull();
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.bill().items().getFirst().categoryId()).isNotNull();
        assertThat(transaction.amount().currency()).isEqualTo(PLN);
        assertThat(transaction.tombstone().isDeleted()).isFalse();

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldCreateExpenseTransactionAndUpdateAccountBalance() {
        // Given
        var userId = createTestUser();
        var categoryId = createExpenseCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        accountModuleFacade.deposit(
                accountId.id(), Money.of(new BigDecimal("200.00"), PLN), userId);

        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("50.00"), PLN),
                        "Grocery shopping",
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        categoryId,
                        userId);

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
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isEqualTo("");
    }

    @Test
    void shouldAllowTransactionWithNullDescription() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        null,
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var longDescription = "A".repeat(101);
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        longDescription,
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(BillItemDescriptionTooLongException.class);
    }

    @Test
    void shouldThrowExceptionWhenCurrenciesDontMatch() {
        // Given
        var userId = createTestUser();
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), EUR),
                        "Payment",
                        LocalDate.now(),
                        INCOME,
                        CategoryId.generate(),
                        userId);

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionCurrencyMismatchException.class)
                .hasMessage("Transaction currency (EUR) must match account currency (PLN)");
    }

    @Test
    void shouldRetrieveTransactionById() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Test transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var retrievedTransaction =
                transactionService.getTransactionById(createdTransaction.id(), user.groupId());

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
        assertThatThrownBy(
                        () -> transactionService.getTransactionById(nonExistentId, user.groupId()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldRetrieveAllTransactions() {
        // Given
        var userId = createTestUser();
        var incomeCategoryId = createIncomeCategory(userId);
        var expenseCategoryId = createExpenseCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command1 =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Transaction 1",
                        LocalDate.now(),
                        INCOME,
                        incomeCategoryId,
                        userId);
        var command2 =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("50.00"), PLN),
                        "Transaction 2",
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        expenseCategoryId,
                        userId);

        var transaction1 = transactionService.createTransaction(command1);
        var transaction2 = transactionService.createTransaction(command2);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var allTransactions =
                transactionService.getAllTransactions(user.groupId(), Pageable.ofSize(20));

        // Then
        assertThat(allTransactions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction1.id()));
        assertThat(allTransactions).anyMatch(t -> t.id().equals(transaction2.id()));
    }

    @Test
    void shouldRetrieveTransactionsByAccountId() {
        // Given
        var userId = createTestUser();
        var incomeCategoryId = createIncomeCategory(userId);
        var expenseCategoryId = createExpenseCategory(userId);
        var account1Id =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var account2Id =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));

        var command1 =
                TransactionCommandFixture.createCommand(
                        account1Id.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Transaction for account 1",
                        LocalDate.now(),
                        INCOME,
                        incomeCategoryId,
                        userId);
        var command2 =
                TransactionCommandFixture.createCommand(
                        account2Id.id(),
                        Money.of(new BigDecimal("50.00"), PLN),
                        "Transaction for account 2",
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        expenseCategoryId,
                        userId);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var user = usersModuleFacade.findUserOrThrow(userId);
        var account1Transactions =
                transactionService.getTransactionsByAccountId(account1Id.id(), user.groupId());

        // Then
        assertThat(account1Transactions).hasSize(1);
        assertThat(account1Transactions.getFirst().description())
                .isEqualTo("Transaction for account 1");
    }

    @Test
    void shouldUpdateTransactionAmountAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Original transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(), Money.of(new BigDecimal("150.00"), PLN), null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.amount().value()).isEqualTo(new BigDecimal("150.00"));
        assertThat(updatedTransaction.description()).isEqualTo("Original transaction");
        assertThat(updatedTransaction.bill()).isNotNull();
        assertThat(updatedTransaction.bill().items()).hasSize(1);

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Original description",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var billItem =
                new BillItemCommand(
                        transaction.bill().items().getFirst().categoryId(),
                        transaction.amount(),
                        "Updated description");
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, of(billItem));

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
        var categoryId = createIncomeCategory(userId);
        var newCategoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Test transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var billItem =
                new BillItemCommand(newCategoryId, transaction.amount(), transaction.description());
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, of(billItem));

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.bill().items().getFirst().categoryId())
                .isEqualTo(newCategoryId);
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTransaction() {
        // Given
        var nonExistentId = TransactionId.generate();
        var userId = createTestUser();
        accountModuleFacade.createAccount(
                new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var updateCommand =
                new UpdateTransactionCommand(
                        nonExistentId, Money.of(new BigDecimal("100.00"), PLN), null);

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand, userId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldDeleteTransactionAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Transaction to delete",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        var user = usersModuleFacade.findUserOrThrow(userId);
        var deletedTransaction =
                transactionRepository
                        .findByIdIncludingDeleted(transaction.id(), user.groupId())
                        .orElseThrow();
        assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();

        var account = accountModuleFacade.getAccount(accountId.id(), userId);
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDeleteExpenseTransactionAndAdjustAccountBalance() {
        // Given
        var userId = createTestUser();
        var categoryId = createExpenseCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        accountModuleFacade.deposit(
                accountId.id(), new Money(new BigDecimal("200.00"), PLN), userId);

        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("50.00"), PLN),
                        "Expense to delete",
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        categoryId,
                        userId);
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
        accountModuleFacade.createAccount(
                new CreateAccountCommand(uniqueAccountName(), PLN, userId));

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentId, userId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: " + nonExistentId.value());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAlreadyDeletedTransaction() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Transaction to delete twice",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);
        transactionService.deleteTransaction(transaction.id(), userId);

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(transaction.id(), userId))
                .isInstanceOf(TransactionAlreadyDeletedException.class)
                .hasMessage(
                        "Transaction with id " + transaction.id().value() + " is already deleted");
    }

    @Test
    void shouldBulkCreateTransactionsAndSkipDuplicates() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade
                        .createAccount(new CreateAccountCommand(uniqueAccountName(), PLN, userId))
                        .id();

        // Existing transaction
        var existingCommand =
                TransactionCommandFixture.createCommand(
                        accountId,
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Existing Transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        transactionService.createTransaction(existingCommand);

        // New transactions (one duplicate, one new)
        var newCommand =
                TransactionCommandFixture.createCommand(
                        accountId,
                        Money.of(new BigDecimal("200.00"), PLN),
                        "New Transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        var commands = of(existingCommand, newCommand);

        // When
        var result =
                transactionService.bulkCreateTransactions(
                        new BulkCreateTransactionCommand(accountId, commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(1);
        assertThat(result.duplicateCount()).isEqualTo(1);
        assertThat(result.savedTransactionIds()).hasSize(1);

        var user = usersModuleFacade.findUserOrThrow(userId);
        var allTransactions =
                transactionService.getTransactionsByAccountId(accountId, user.groupId());
        assertThat(allTransactions).hasSize(2); // 1 existing + 1 new

        var account = accountModuleFacade.getAccount(accountId, userId);
        // 100 (existing) + 200 (new) = 300
        assertThat(account.balance().value()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void shouldLogAuditWhenTransactionIsCreated() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var command =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Test transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);

        // When
        var transaction = transactionService.createTransaction(command);

        // Then
        var user = usersModuleFacade.findUserOrThrow(userId);
        var query =
                new AuditLogQuery(
                        user.groupId(),
                        AuditEntityType.TRANSACTION,
                        EntityId.from(transaction.id().value()),
                        AuditOperation.CREATE,
                        null,
                        null,
                        null);
        var auditLogs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(auditLogs.getContent()).hasSize(1);
        var auditLog = auditLogs.getContent().getFirst();
        assertThat(auditLog.operation()).isEqualTo(AuditOperation.CREATE);
        assertThat(auditLog.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
        assertThat(auditLog.entityId()).isEqualTo(EntityId.from(transaction.id().value()));
        assertThat(auditLog.performedBy()).isEqualTo(userId);
        assertThat(auditLog.changeDescription()).isEqualTo("Transaction created: Test transaction");
    }

    @Test
    void shouldLogAuditWhenTransactionIsUpdated() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Original transaction",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var billItem =
                new BillItemCommand(
                        transaction.bill().items().getFirst().categoryId(),
                        transaction.amount(),
                        "Updated description");
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, of(billItem));

        // When
        transactionService.updateTransaction(updateCommand, userId);

        // Then
        var user = usersModuleFacade.findUserOrThrow(userId);
        var query =
                new AuditLogQuery(
                        user.groupId(),
                        AuditEntityType.TRANSACTION,
                        EntityId.from(transaction.id().value()),
                        AuditOperation.UPDATE,
                        null,
                        null,
                        null);
        var auditLogs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(auditLogs.getContent()).hasSize(1);
        var auditLog = auditLogs.getContent().getFirst();
        assertThat(auditLog.operation()).isEqualTo(AuditOperation.UPDATE);
        assertThat(auditLog.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
        assertThat(auditLog.entityId()).isEqualTo(EntityId.from(transaction.id().value()));
        assertThat(auditLog.performedBy()).isEqualTo(userId);
        assertThat(auditLog.changeDescription())
                .isEqualTo("Transaction updated: Updated description");
    }

    @Test
    void shouldLogAuditWhenTransactionIsDeleted() {
        // Given
        var userId = createTestUser();
        var categoryId = createIncomeCategory(userId);
        var accountId =
                accountModuleFacade.createAccount(
                        new CreateAccountCommand(uniqueAccountName(), PLN, userId));
        var createCommand =
                TransactionCommandFixture.createCommand(
                        accountId.id(),
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Transaction to delete",
                        LocalDate.now(),
                        INCOME,
                        categoryId,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        var user = usersModuleFacade.findUserOrThrow(userId);
        var query =
                new AuditLogQuery(
                        user.groupId(),
                        AuditEntityType.TRANSACTION,
                        EntityId.from(transaction.id().value()),
                        AuditOperation.DELETE,
                        null,
                        null,
                        null);
        var auditLogs = auditLogRepository.findByQuery(query, PageRequest.of(0, 10));

        assertThat(auditLogs.getContent()).hasSize(1);
        var auditLog = auditLogs.getContent().getFirst();
        assertThat(auditLog.operation()).isEqualTo(AuditOperation.DELETE);
        assertThat(auditLog.entityType()).isEqualTo(AuditEntityType.TRANSACTION);
        assertThat(auditLog.entityId()).isEqualTo(EntityId.from(transaction.id().value()));
        assertThat(auditLog.performedBy()).isEqualTo(userId);
        assertThat(auditLog.changeDescription())
                .isEqualTo("Transaction deleted: Transaction to delete");
    }
}
