package pl.btsoftware.backend.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.Currency.USD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

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
        var mockUser =
                User.create(new UserId("user-123"), "test@example.com", "Test User", testGroupId);
        when(usersModuleFacade.findUserOrThrow(any(UserId.class))).thenReturn(mockUser);
        when(categoryQueryFacade.allCategoriesExists(any(), any(GroupId.class))).thenReturn(true);

        var auditModuleFacade = Mockito.mock(AuditModuleFacade.class);
        var accountService =
                new AccountService(
                        accountRepository,
                        usersModuleFacade,
                        Mockito.mock(TransactionQueryFacade.class),
                        auditModuleFacade);
        this.accountModuleFacade = new AccountModuleFacade(accountService, usersModuleFacade);
        var transactionAuditModuleFacade = Mockito.mock(AuditModuleFacade.class);
        this.transactionService =
                new TransactionService(
                        transactionRepository,
                        accountModuleFacade,
                        categoryQueryFacade,
                        usersModuleFacade,
                        transactionAuditModuleFacade);
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("1000.12"));
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id(), testGroupId)).isPresent();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(1);

        // Verify account balance updated by +1000.12
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("1000.12"));
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("250.50"));
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id(), testGroupId)).isPresent();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(1);

        // Verify account balance updated by -250.50
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-250.50"));
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command =
                new CreateTransactionCommand(
                        nonExistentAccountId, date, type, billCommand, UserId.generate());
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .isEmpty();
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isEmpty();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(1);
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.description()).isNull();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(1);
    }

    @Test
    void shouldRejectTransactionWithDescriptionTooLong() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var description = "A".repeat(101);
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.INCOME;
        var categoryId = CategoryId.generate();

        // When & Then
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(
                        pl.btsoftware.backend.transaction.domain.error
                                .BillItemDescriptionTooLongException.class)
                .hasMessageContaining("Bill item description cannot exceed 100 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .isEmpty();
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, USD), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(TransactionCurrencyMismatchException.class)
                .hasMessageContaining(
                        "Transaction currency (USD) must match account currency (PLN)");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .isEmpty();
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
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var foundTransaction =
                transactionService.getTransactionById(createdTransaction.id(), testGroupId);

        // Then
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(foundTransaction.accountId()).isEqualTo(account.id());
        assertThat(foundTransaction.amount().value()).isEqualTo(amount);
        assertThat(foundTransaction.description()).isEqualTo(description);
        assertThat(foundTransaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);
        assertThat(foundTransaction.type()).isEqualTo(type);
    }

    @Test
    void shouldThrowExceptionForNonExistentTransactionId() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(
                        () ->
                                transactionService.getTransactionById(
                                        nonExistentTransactionId, testGroupId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(
                        "Transaction not found with id: " + nonExistentTransactionId.value());
    }

    @Test
    void shouldGetAllTransactions() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();

        var billItem1 =
                new BillItemCommand(
                        categoryId1, Money.of(new BigDecimal("1000.00"), PLN), "Salary");
        var billCommand1 = new BillCommand(List.of(billItem1));
        var command1 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.INCOME,
                        billCommand1,
                        userId);

        var billItem2 =
                new BillItemCommand(
                        categoryId2, Money.of(new BigDecimal("250.50"), PLN), "Groceries");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var command2 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 16),
                        TransactionType.EXPENSE,
                        billCommand2,
                        userId);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var allTransactions =
                transactionService.getAllTransactions(testGroupId, Pageable.ofSize(20));

        // Then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions.stream().map(Transaction::description).toList())
                .containsExactlyInAnyOrder("Salary", "Groceries");
    }

    @Test
    void shouldUpdateTransactionAmount() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var initialAmount = new BigDecimal("500.00");
        var categoryId = CategoryId.generate();

        var billItem =
                new BillItemCommand(
                        categoryId, Money.of(initialAmount, PLN), "Initial transaction");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.INCOME,
                        billCommand,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newAmount = Money.of(new BigDecimal("750.00"), PLN);
        var billItems = List.of(new BillItemCommand(categoryId, newAmount, "Initial transaction"));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(),
                        new BillCommand(billItems),
                        account.id(),
                        createCommand.transactionDate());

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.description()).isEqualTo("Initial transaction");
        assertThat(updatedTransaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);
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

        var billItem =
                new BillItemCommand(categoryId, Money.of(amount, PLN), "Original description");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.INCOME,
                        billCommand,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newDescription = "Updated description";
        var billItems =
                List.of(new BillItemCommand(categoryId, Money.of(amount, PLN), newDescription));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(),
                        new BillCommand(billItems),
                        account.id(),
                        transaction.transactionDate());

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo(newDescription);
        assertThat(updatedTransaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);
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

        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), "Test transaction");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.EXPENSE,
                        billCommand,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newCategoryId = CategoryId.generate();
        var billItems =
                List.of(
                        new BillItemCommand(
                                newCategoryId, Money.of(amount, PLN), "Test transaction"));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(),
                        new BillCommand(billItems),
                        account.id(),
                        createCommand.transactionDate());

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
        assertThat(updatedTransaction.bill().items().getFirst().categoryId())
                .isEqualTo(newCategoryId);
        assertThat(updatedTransaction.lastUpdatedAt()).isAfter(transaction.lastUpdatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void shouldRejectUpdateForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();
        var billItems =
                List.of(
                        new BillItemCommand(
                                CategoryId.generate(),
                                Money.of(new BigDecimal("100.00"), PLN),
                                "Updated description"));
        var updateCommand =
                new UpdateTransactionCommand(
                        nonExistentTransactionId,
                        new BillCommand(billItems),
                        AccountId.generate(),
                        LocalDate.now());

        // When & Then
        assertThatThrownBy(
                        () ->
                                transactionService.updateTransaction(
                                        updateCommand, UserId.generate()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(
                        "Transaction not found with id: " + nonExistentTransactionId.value());
    }

    @Test
    void shouldUpdateTransactionAccount() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand1 = new CreateAccountCommand("Account 1", PLN, userId);
        var account1 = accountModuleFacade.createAccount(createAccountCommand1);
        var createAccountCommand2 = new CreateAccountCommand("Account 2", PLN, userId);
        var account2 = accountModuleFacade.createAccount(createAccountCommand2);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();

        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), "Test transaction");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account1.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.EXPENSE,
                        billCommand,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        var billItems =
                List.of(new BillItemCommand(categoryId, Money.of(amount, PLN), "Test transaction"));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(),
                        new BillCommand(billItems),
                        account2.id(),
                        transaction.transactionDate());

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.accountId()).isEqualTo(account2.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);

        // Verify account1 balance reversed (+100.00)
        var updatedAccount1 = accountModuleFacade.getAccount(account1.id(), userId);
        assertThat(updatedAccount1.balance().value()).isEqualTo(new BigDecimal("0.00"));

        // Verify account2 balance updated (-100.00)
        var updatedAccount2 = accountModuleFacade.getAccount(account2.id(), userId);
        assertThat(updatedAccount2.balance().value()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void shouldUpdateTransactionDate() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();
        var originalDate = LocalDate.of(2024, 1, 15);

        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), "Test transaction");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account.id(), originalDate, TransactionType.EXPENSE, billCommand, userId);
        var transaction = transactionService.createTransaction(createCommand);

        var newDate = LocalDate.of(2024, 2, 20);
        var billItems =
                List.of(new BillItemCommand(categoryId, Money.of(amount, PLN), "Test transaction"));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(), new BillCommand(billItems), account.id(), newDate);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand, userId);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.transactionDate()).isEqualTo(newDate);
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void shouldDeleteTransaction() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("100.00");
        var categoryId = CategoryId.generate();

        var billItem =
                new BillItemCommand(categoryId, Money.of(amount, PLN), "Transaction to delete");
        var billCommand = new BillCommand(List.of(billItem));
        var createCommand =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.EXPENSE,
                        billCommand,
                        userId);
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id(), userId);

        // Then
        assertThatThrownBy(
                        () -> transactionService.getTransactionById(transaction.id(), testGroupId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: " + transaction.id().value());

        // Verify account balance reversed (+100.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id(), userId);
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("0.00"));

        // Verify transaction not in normal queries
        assertThat(transactionService.getAllTransactions(testGroupId, Pageable.ofSize(20)))
                .isEmpty();
    }

    @Test
    void shouldRejectDeleteForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(
                        () ->
                                transactionService.deleteTransaction(
                                        nonExistentTransactionId, UserId.generate()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(
                        "Transaction not found with id: " + nonExistentTransactionId.value());
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

        when(categoryQueryFacade.allCategoriesExists(any(), any())).thenReturn(false);

        // When & Then
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(
                        pl.btsoftware.backend.category.domain.error.CategoryNotFoundException
                                .class);

        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .isEmpty();
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

        when(categoryQueryFacade.allCategoriesExists(any(), any())).thenReturn(false);

        // When & Then
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(
                        pl.btsoftware.backend.category.domain.error.CategoryNotFoundException
                                .class);

        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .isEmpty();
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

        when(categoryQueryFacade.allCategoriesExists(any(), any())).thenReturn(true);

        // When
        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        var transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(1);
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

        var billItem1 = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand1 = new BillCommand(List.of(billItem1));
        var command1 = new CreateTransactionCommand(account.id(), date, type, billCommand1, userId);
        transactionService.createTransaction(command1);

        // When - different description makes it non-duplicate
        var billItem2 =
                new BillItemCommand(categoryId, Money.of(amount, PLN), "Second transaction");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var command2 = new CreateTransactionCommand(account.id(), date, type, billCommand2, userId);
        var transaction2 = transactionService.createTransaction(command2);

        // Then
        assertThat(transaction2).isNotNull();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(2);
    }

    @Test
    void shouldBulkCreateTransactionsWithNoDuplicates() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        var billItem1 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("100.00"), PLN), "Transaction 1");
        var billCommand1 = new BillCommand(List.of(billItem1));
        var billItem2 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("200.00"), PLN), "Transaction 2");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var billItem3 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("300.00"), PLN), "Transaction 3");
        var billCommand3 = new BillCommand(List.of(billItem3));

        var commands =
                List.of(
                        new CreateTransactionCommand(
                                account.id(),
                                LocalDate.of(2024, 1, 15),
                                TransactionType.INCOME,
                                billCommand1,
                                userId),
                        new CreateTransactionCommand(
                                account.id(),
                                LocalDate.of(2024, 1, 16),
                                TransactionType.INCOME,
                                billCommand2,
                                userId),
                        new CreateTransactionCommand(
                                account.id(),
                                LocalDate.of(2024, 1, 17),
                                TransactionType.EXPENSE,
                                billCommand3,
                                userId));

        // When
        var result =
                transactionService.bulkCreateTransactions(
                        new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(3);
        assertThat(result.duplicateCount()).isEqualTo(0);
        assertThat(result.savedTransactionIds()).hasSize(3);
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(3);

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
        var billItemExisting =
                new BillItemCommand(
                        categoryId,
                        Money.of(new BigDecimal("100.00"), PLN),
                        "Duplicate transaction");
        var billCommandExisting = new BillCommand(List.of(billItemExisting));
        var existingCommand =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.INCOME,
                        billCommandExisting,
                        userId);
        transactionService.createTransaction(existingCommand);

        // Prepare bulk commands with duplicates
        var billItem2 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("200.00"), PLN), "Transaction 2");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var billItem3 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("300.00"), PLN), "Transaction 3");
        var billCommand3 = new BillCommand(List.of(billItem3));

        var commands =
                List.of(
                        existingCommand, // This is a duplicate
                        new CreateTransactionCommand(
                                account.id(),
                                LocalDate.of(2024, 1, 16),
                                TransactionType.INCOME,
                                billCommand2,
                                userId),
                        existingCommand, // Another duplicate
                        new CreateTransactionCommand(
                                account.id(),
                                LocalDate.of(2024, 1, 17),
                                TransactionType.EXPENSE,
                                billCommand3,
                                userId));

        // When
        var result =
                transactionService.bulkCreateTransactions(
                        new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.duplicateCount()).isEqualTo(2);
        assertThat(result.savedTransactionIds()).hasSize(2);
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(3); // 1 existing + 2 new

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
        var billItem1 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("100.00"), PLN), "Transaction 1");
        var billCommand1 = new BillCommand(List.of(billItem1));
        var command1 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 15),
                        TransactionType.INCOME,
                        billCommand1,
                        userId);

        var billItem2 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("200.00"), PLN), "Transaction 2");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var command2 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.of(2024, 1, 16),
                        TransactionType.INCOME,
                        billCommand2,
                        userId);

        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // Prepare bulk commands with all duplicates
        var commands = List.of(command1, command2);

        // When
        var result =
                transactionService.bulkCreateTransactions(
                        new BulkCreateTransactionCommand(account.id(), commands), userId);

        // Then
        assertThat(result.savedCount()).isEqualTo(0);
        assertThat(result.duplicateCount()).isEqualTo(2);
        assertThat(result.savedTransactionIds()).isEmpty();
        assertThat(transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).getContent())
                .hasSize(2);

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

        when(categoryQueryFacade.allCategoriesExists(any(), any())).thenReturn(true);

        var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
        var billCommand = new BillCommand(List.of(billItem));
        var command = new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
        var transaction = transactionService.createTransaction(command);

        when(categoryQueryFacade.allCategoriesExists(any(), any())).thenReturn(false);

        // When & Then
        var billItems =
                List.of(
                        new BillItemCommand(
                                categoryId,
                                Money.of(new BigDecimal("2000.00"), PLN),
                                "Updated description"));
        var updateCommand =
                new UpdateTransactionCommand(
                        transaction.id(), new BillCommand(billItems), account.id(), date);
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand, userId))
                .isInstanceOf(
                        pl.btsoftware.backend.category.domain.error.CategoryNotFoundException
                                .class);
    }

    @Test
    void shouldGetTransactionsPaginatedFirstPage() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        for (int i = 0; i < 25; i++) {
            var amount = new BigDecimal("100.00");
            var description = "Transaction " + i;
            var date = LocalDate.of(2024, 1, i + 1);
            var type = TransactionType.EXPENSE;

            var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
            var billCommand = new BillCommand(List.of(billItem));
            var command =
                    new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
            transactionService.createTransaction(command);
        }

        // When
        var pageable = PageRequest.of(0, 10, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void shouldGetTransactionsPaginatedSecondPage() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        for (int i = 0; i < 25; i++) {
            var amount = new BigDecimal("100.00");
            var description = "Transaction " + i;
            var date = LocalDate.of(2024, 1, 1);
            var type = TransactionType.EXPENSE;

            var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
            var billCommand = new BillCommand(List.of(billItem));
            var command =
                    new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
            transactionService.createTransaction(command);
        }

        // When
        var pageable = PageRequest.of(1, 10, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void shouldGetTransactionsPaginatedEmptyPage() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        for (int i = 0; i < 5; i++) {
            var amount = new BigDecimal("100.00");
            var description = "Transaction " + i;
            var date = LocalDate.of(2024, 1, 1);
            var type = TransactionType.EXPENSE;

            var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
            var billCommand = new BillCommand(List.of(billItem));
            var command =
                    new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
            transactionService.createTransaction(command);
        }

        // When
        var pageable = PageRequest.of(5, 10, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(5);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void shouldGetTransactionsPaginatedWithCustomPageSize() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        for (int i = 0; i < 30; i++) {
            var amount = new BigDecimal("100.00");
            var description = "Transaction " + i;
            var date = LocalDate.of(2024, 1, 1);
            var type = TransactionType.EXPENSE;

            var billItem = new BillItemCommand(categoryId, Money.of(amount, PLN), description);
            var billCommand = new BillCommand(List.of(billItem));
            var command =
                    new CreateTransactionCommand(account.id(), date, type, billCommand, userId);
            transactionService.createTransaction(command);
        }

        // When
        var pageable = PageRequest.of(0, 5, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(30);
        assertThat(page.getTotalPages()).isEqualTo(6);
        assertThat(page.getSize()).isEqualTo(5);
    }

    @Test
    void shouldGetTransactionsPaginatedInDescendingOrderByDate() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        var date1 = LocalDate.of(2024, 1, 10);
        var date2 = LocalDate.of(2024, 1, 15);
        var date3 = LocalDate.of(2024, 1, 5);

        var billItem1 =
                new BillItemCommand(categoryId, Money.of(new BigDecimal("100.00"), PLN), "Old");
        var billCommand1 = new BillCommand(List.of(billItem1));
        var command1 =
                new CreateTransactionCommand(
                        account.id(), date1, TransactionType.EXPENSE, billCommand1, userId);
        var transaction1 = transactionService.createTransaction(command1);

        var billItem2 =
                new BillItemCommand(categoryId, Money.of(new BigDecimal("200.00"), PLN), "Newest");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var command2 =
                new CreateTransactionCommand(
                        account.id(), date2, TransactionType.EXPENSE, billCommand2, userId);
        var transaction2 = transactionService.createTransaction(command2);

        var billItem3 =
                new BillItemCommand(categoryId, Money.of(new BigDecimal("300.00"), PLN), "Oldest");
        var billCommand3 = new BillCommand(List.of(billItem3));
        var command3 =
                new CreateTransactionCommand(
                        account.id(), date3, TransactionType.EXPENSE, billCommand3, userId);
        var transaction3 = transactionService.createTransaction(command3);

        // When
        var pageable = PageRequest.of(0, 10, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent().get(0).id()).isEqualTo(transaction2.id());
        assertThat(page.getContent().get(1).id()).isEqualTo(transaction1.id());
        assertThat(page.getContent().get(2).id()).isEqualTo(transaction3.id());
    }

    @Test
    void shouldGetTransactionsPaginatedExcludeSoftDeleted() {
        // Given
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var categoryId = CategoryId.generate();

        var billItem1 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("100.00"), PLN), "Transaction 1");
        var billCommand1 = new BillCommand(List.of(billItem1));
        var command1 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        billCommand1,
                        userId);
        var transaction1 = transactionService.createTransaction(command1);

        var billItem2 =
                new BillItemCommand(
                        categoryId, Money.of(new BigDecimal("200.00"), PLN), "Transaction 2");
        var billCommand2 = new BillCommand(List.of(billItem2));
        var command2 =
                new CreateTransactionCommand(
                        account.id(),
                        LocalDate.now(),
                        TransactionType.EXPENSE,
                        billCommand2,
                        userId);
        transactionService.createTransaction(command2);

        transactionService.deleteTransaction(transaction1.id(), userId);

        // When
        var pageable = PageRequest.of(0, 10, Sort.by("transactionDate", "createdAt").descending());
        var page = transactionRepository.findAll(testGroupId, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
