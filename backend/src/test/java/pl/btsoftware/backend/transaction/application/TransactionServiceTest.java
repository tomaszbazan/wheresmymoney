package pl.btsoftware.backend.transaction.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.category.domain.error.NoCategoriesAvailableException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionTooLongException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.Currency.USD;

class TransactionServiceTest {
    private TransactionRepository transactionRepository;
    private AccountModuleFacade accountModuleFacade;
    private CategoryQueryFacade categoryQueryFacade;
    private TransactionService transactionService;
    private GroupId testGroupId;

    @BeforeEach
    void setUp() {
        this.transactionRepository = new InMemoryTransactionRepository();
        var accountRepository = new InMemoryAccountRepository();
        var usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        this.categoryQueryFacade = Mockito.mock(CategoryQueryFacade.class);

        this.testGroupId = new GroupId(UUID.randomUUID());
        var mockUser = User.create(
                new UserId("user-123"),
                "test@example.com",
                "Test User",
                testGroupId
        );
        when(usersModuleFacade.findUserOrThrow(any(UserId.class))).thenReturn(mockUser);
        when(categoryQueryFacade.hasCategories(any(CategoryType.class), any(GroupId.class))).thenReturn(true);

        var accountService = new AccountService(accountRepository, usersModuleFacade);
        this.accountModuleFacade = new AccountModuleFacade(accountService, usersModuleFacade);
        this.transactionService = new TransactionService(transactionRepository, accountModuleFacade, categoryQueryFacade, usersModuleFacade);
    }

    @Test
    void shouldCreateIncomeTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("1000.12"));
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.categoryId()).isEqualTo(categoryId);

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id(), testGroupId)).isPresent();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);

        // Verify account balance updated by +1000.12
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("1000.12"));

        // Verify transaction ID added to account's transaction list
        assertThat(updatedAccount.transactionIds()).contains(transaction.id());
    }

    @Test
    void shouldCreateExpenseTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("250.50");
        var description = "Grocery shopping";
        var date = LocalDate.of(2024, 1, 16);
        var type = TransactionType.EXPENSE;
        var categoryId = CategoryId.generate();

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("250.50"));
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");
        assertThat(transaction.categoryId()).isEqualTo(categoryId);

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id(), testGroupId)).isPresent();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);

        // Verify account balance updated by -250.50
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-250.50"));

        // Verify transaction ID added to account's transaction list
        assertThat(updatedAccount.transactionIds()).contains(transaction.id());
    }

    @Test
    void shouldRejectTransactionForNonexistentAccount() {
        // Given
        var nonExistentAccountId = AccountId.generate();
        var amount = new BigDecimal("100.00");
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When & Then
        var command = new CreateTransactionCommand(nonExistentAccountId, Money.of(amount, PLN), description, date, type, categoryId, UserId.generate());
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId)).isEmpty();
    }

    @Test
    void shouldAllowTransactionWithEmptyDescription() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isEqualTo("");
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);
    }

    @Test
    void shouldAllowTransactionWithNullDescription() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        String description = null;
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isNull();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);
    }

    @Test
    void shouldRejectTransactionWithDescriptionTooLong() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "A".repeat(201); // 201 characters
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionDescriptionTooLongException.class)
                .hasMessageContaining("Description cannot exceed 200 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId)).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithCurrencyMismatch() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, USD), description, date, type, categoryId, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionCurrencyMismatchException.class)
                .hasMessageContaining("Transaction currency (USD) must match account currency (PLN)");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId)).isEmpty();
    }

    @Test
    void shouldGetTransactionById() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var foundTransaction = transactionService.getTransactionById(createdTransaction.id(), testGroupId);

        // Then
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(foundTransaction.accountId()).isEqualTo(account.id());
        assertThat(foundTransaction.amount().value()).isEqualTo(amount);
        assertThat(foundTransaction.description()).isEqualTo(description);
        assertThat(foundTransaction.categoryId()).isEqualTo(categoryId);
        assertThat(foundTransaction.type()).isEqualTo(type);
    }

    @Test
    void shouldThrowExceptionForNonExistentTransactionId() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(nonExistentTransactionId, testGroupId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + nonExistentTransactionId.value());
    }

    @Test
    void shouldGetAllTransactions() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();
        var command1 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("1000.00"), PLN), "Salary",
                LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId1, userId);
        var command2 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("250.50"), PLN), "Groceries",
                LocalDate.of(2024, 1, 16), TransactionType.EXPENSE, categoryId2, userId);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var allTransactions = transactionService.getAllTransactions(testGroupId);

        // Then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions.stream().map(Transaction::description).toList())
                .containsExactlyInAnyOrder("Salary", "Groceries");
    }

    @Test
    void shouldGetTransactionsByAccountId() {
        // Given
        var userId1 = UserId.generate();
        var userId2 = UserId.generate();
        var createAccountCommand1 = new CreateAccountCommand("Test Account 1", PLN, userId1);
        var account1 = accountModuleFacade.createAccount(createAccountCommand1);
        var createAccountCommand2 = new CreateAccountCommand("Test Account 2", PLN, userId2);
        var account2 = accountModuleFacade.createAccount(createAccountCommand2);

        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();
        var categoryId3 = CategoryId.generate();
        var command1 = new CreateTransactionCommand(account1.id(), Money.of(new BigDecimal("1000.00"), PLN), "Salary",
                LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId1, userId1);
        var command2 = new CreateTransactionCommand(account2.id(), Money.of(new BigDecimal("250.50"), PLN), "Groceries",
                LocalDate.of(2024, 1, 16), TransactionType.EXPENSE, categoryId2, userId2);
        var command3 = new CreateTransactionCommand(account1.id(), Money.of(new BigDecimal("100.00"), PLN), "Coffee",
                LocalDate.of(2024, 1, 17), TransactionType.EXPENSE, categoryId3, userId1);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);
        transactionService.createTransaction(command3);

        // When
        var account1Transactions = transactionService.getTransactionsByAccountId(account1.id(), testGroupId);

        // Then
        assertThat(account1Transactions).hasSize(2);
        assertThat(account1Transactions.stream().map(Transaction::description).toList())
                .containsExactlyInAnyOrder("Salary", "Coffee");
        assertThat(account1Transactions).allMatch(t -> t.accountId().equals(account1.id()));
    }

    @Test
    void shouldUpdateTransactionAmount() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var initialAmount = new BigDecimal("500.00");
        var categoryId = CategoryId.generate();
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(initialAmount, PLN), "Initial transaction",
                LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId, userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newAmount = Money.of(new BigDecimal("750.00"), PLN);
        var updateCommand = new UpdateTransactionCommand(transaction.id(), newAmount, null, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.description()).isEqualTo("Initial transaction");
        assertThat(updatedTransaction.categoryId()).isEqualTo(categoryId);
        assertThat(updatedTransaction.lastUpdatedAt()).isAfter(transaction.lastUpdatedAt());

        // Verify account balance updated by difference (+250.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("750.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Original description",
                LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId, userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newDescription = "Updated description";
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, newDescription, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo(newDescription);
        assertThat(updatedTransaction.categoryId()).isEqualTo(categoryId);
        assertThat(updatedTransaction.lastUpdatedAt()).isAfter(transaction.lastUpdatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(amount);
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Test transaction",
                LocalDate.of(2024, 1, 15), TransactionType.EXPENSE, categoryId, userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newCategoryId = CategoryId.generate();
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, null, newCategoryId);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
        assertThat(updatedTransaction.categoryId()).isEqualTo(newCategoryId);
        assertThat(updatedTransaction.lastUpdatedAt()).isAfter(transaction.lastUpdatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void shouldRejectUpdateForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();
        var updateCommand = new UpdateTransactionCommand(nonExistentTransactionId, null, "Updated description", null);

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand, UserId.generate()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + nonExistentTransactionId.value());
    }

    @Test
    void shouldDeleteTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Transaction to delete",
                LocalDate.of(2024, 1, 15), TransactionType.EXPENSE, categoryId, userId);
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transaction.id(), testGroupId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + transaction.id().value());

        // Verify account balance reversed (+100.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("0.00"));

        // Verify transaction not in normal queries
        assertThat(transactionService.getAllTransactions(testGroupId)).isEmpty();
        assertThat(transactionService.getTransactionsByAccountId(account.id(), testGroupId)).isEmpty();

        // Verify transaction ID removed fromGroup account's transaction list
        assertThat(updatedAccount.transactionIds()).doesNotContain(transaction.id());
    }

    @Test
    void shouldRejectDeleteForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentTransactionId, UserId.generate()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + nonExistentTransactionId.value());
    }

    @Test
    void shouldRejectCreateIncomeTransactionWhenNoCategoriesExist() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.00");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        when(categoryQueryFacade.hasCategories(CategoryType.INCOME, testGroupId)).thenReturn(false);

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(NoCategoriesAvailableException.class)
                .hasMessageContaining("No categories of type INCOME are available");

        assertThat(transactionRepository.findAll(testGroupId)).isEmpty();
    }

    @Test
    void shouldRejectCreateExpenseTransactionWhenNoCategoriesExist() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("250.50");
        var description = "Grocery shopping";
        var date = LocalDate.of(2024, 1, 16);
        var type = TransactionType.EXPENSE;
        var categoryId = CategoryId.generate();

        when(categoryQueryFacade.hasCategories(CategoryType.EXPENSE, testGroupId)).thenReturn(false);

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(NoCategoriesAvailableException.class)
                .hasMessageContaining("No categories of type EXPENSE are available");

        assertThat(transactionRepository.findAll(testGroupId)).isEmpty();
    }

    @Test
    void shouldAllowCreateTransactionWhenCategoriesExist() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.00");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        when(categoryQueryFacade.hasCategories(CategoryType.INCOME, testGroupId)).thenReturn(true);

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "Duplicate transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        transactionService.createTransaction(command);

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(pl.btsoftware.backend.transaction.domain.error.DuplicateTransactionException.class)
                .hasMessageContaining("duplicate");

        // Verify only one transaction was created
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(1);
    }

    @Test
    void shouldAllowNonDuplicateTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "First transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        var command1 = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        transactionService.createTransaction(command1);

        // When - different description makes it non-duplicate
        var command2 = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Second transaction", date, type, categoryId, userId);
        var transaction2 = transactionService.createTransaction(command2);

        // Then
        assertThat(transaction2).isNotNull();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(2);
    }

    @Test
    void shouldBulkCreateTransactionsWithNoDuplicates() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        var commands = List.of(
                new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("100.00"), PLN), "Transaction 1",
                        LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId, userId),
                new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("200.00"), PLN), "Transaction 2",
                        LocalDate.of(2024, 1, 16), TransactionType.INCOME, categoryId, userId),
                new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("300.00"), PLN), "Transaction 3",
                        LocalDate.of(2024, 1, 17), TransactionType.EXPENSE, categoryId, userId)
        );

        // When
        var result = transactionService.bulkCreateTransactions(new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(3);
        assertThat(result.duplicateCount()).isEqualTo(0);
        assertThat(result.savedTransactionIds()).hasSize(3);
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(3);

        // Verify account balance updated correctly: +100 +200 -300 = 0
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void shouldBulkCreateTransactionsSkippingDuplicates() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        // Create initial transaction
        var existingCommand = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("100.00"), PLN),
                "Duplicate transaction", LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId, userId);
        transactionService.createTransaction(existingCommand);

        // Prepare bulk commands with duplicates
        var commands = List.of(
                existingCommand,  // This is a duplicate
                new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("200.00"), PLN), "Transaction 2",
                        LocalDate.of(2024, 1, 16), TransactionType.INCOME, categoryId, userId),
                existingCommand,  // Another duplicate
                new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("300.00"), PLN), "Transaction 3",
                        LocalDate.of(2024, 1, 17), TransactionType.EXPENSE, categoryId, userId)
        );

        // When
        var result = transactionService.bulkCreateTransactions(new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.duplicateCount()).isEqualTo(2);
        assertThat(result.savedTransactionIds()).hasSize(2);
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(3); // 1 existing + 2 new

        // Verify account balance updated correctly: +100 (existing) +200 -300 = 0
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void shouldBulkCreateTransactionsSkippingAllDuplicates() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        // Create initial transactions
        var command1 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("100.00"), PLN),
                "Transaction 1", LocalDate.of(2024, 1, 15), TransactionType.INCOME, categoryId, userId);
        var command2 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("200.00"), PLN),
                "Transaction 2", LocalDate.of(2024, 1, 16), TransactionType.INCOME, categoryId, userId);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // Prepare bulk commands with all duplicates
        var commands = List.of(command1, command2);

        // When
        var result = transactionService.bulkCreateTransactions(new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(0);
        assertThat(result.duplicateCount()).isEqualTo(2);
        assertThat(result.savedTransactionIds()).isEmpty();
        assertThat(transactionRepository.findAll(testGroupId)).hasSize(2);

        // Verify account balance unchanged: +100 +200 = 300
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("300.00"));
    }

    @Test
    void shouldRejectUpdateTransactionWhenNoCategoriesExist() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.00");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        when(categoryQueryFacade.hasCategories(CategoryType.INCOME, testGroupId)).thenReturn(true);
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, categoryId, userId);
        var transaction = transactionService.createTransaction(command);

        when(categoryQueryFacade.hasCategories(CategoryType.INCOME, testGroupId)).thenReturn(false);

        // When & Then
        var updateCommand = new UpdateTransactionCommand(transaction.id(), Money.of(new BigDecimal("2000.00"), PLN), "Updated description", categoryId);
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand, userId))
                .isInstanceOf(NoCategoriesAvailableException.class)
                .hasMessageContaining("No categories of type INCOME are available");
    }
}
