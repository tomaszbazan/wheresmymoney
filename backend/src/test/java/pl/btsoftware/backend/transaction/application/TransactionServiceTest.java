package pl.btsoftware.backend.transaction.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionId;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.TransactionType;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.account.domain.Currency.PLN;
import static pl.btsoftware.backend.account.domain.Currency.USD;

class TransactionServiceTest {
    private TransactionRepository transactionRepository;
    private AccountModuleFacade accountModuleFacade;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        this.transactionRepository = new InMemoryTransactionRepository();
        var accountRepository = new InMemoryAccountRepository();
        var accountService = new AccountService(accountRepository);
        this.accountModuleFacade = new AccountModuleFacade(accountService);
        this.transactionService = new TransactionService(transactionRepository, accountModuleFacade);
    }

    @Test
    void shouldCreateIncomeTransaction() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Salary";

        // When
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, PLN);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().amount()).isEqualTo(new BigDecimal("1000.12"));
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.category()).isEqualTo("Salary");

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id())).isPresent();
        assertThat(transactionRepository.findAll()).hasSize(1);

        // Verify account balance updated by +1000.12
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(new BigDecimal("1000.12"));
    }

    @Test
    void shouldCreateExpenseTransaction() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("250.50");
        var description = "Grocery shopping";
        var date = OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.EXPENSE;
        var category = "Food";

        // When
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, PLN);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().amount()).isEqualTo(new BigDecimal("250.50"));
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");
        assertThat(transaction.category()).isEqualTo("Food");

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id())).isPresent();
        assertThat(transactionRepository.findAll()).hasSize(1);

        // Verify account balance updated by -250.50
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(new BigDecimal("-250.50"));
    }

    @Test
    void shouldRejectTransactionForNonexistentAccount() {
        // Given
        var nonExistentAccountId = AccountId.generate();
        var amount = new BigDecimal("100.00");
        var description = "Test transaction";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(nonExistentAccountId, amount, description, date, type, category, PLN);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithEmptyDescription() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, PLN);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description must be between 1 and 200 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithDescriptionTooLong() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "A".repeat(201); // 201 characters
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, PLN);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description must be between 1 and 200 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithCurrencyMismatch() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "Test transaction";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, USD);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction currency must match account currency");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldGetTransactionById() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Salary";
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category, PLN);
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var foundTransaction = transactionService.getTransactionById(createdTransaction.id());

        // Then
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(foundTransaction.accountId()).isEqualTo(account.id());
        assertThat(foundTransaction.amount().amount()).isEqualTo(amount);
        assertThat(foundTransaction.description()).isEqualTo(description);
        assertThat(foundTransaction.category()).isEqualTo(category);
        assertThat(foundTransaction.type()).isEqualTo(type);
    }

    @Test
    void shouldThrowExceptionForNonExistentTransactionId() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(nonExistentTransactionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void shouldGetAllTransactions() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var command1 = new CreateTransactionCommand(account.id(), new BigDecimal("1000.00"), "Salary", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary", PLN);
        var command2 = new CreateTransactionCommand(account.id(), new BigDecimal("250.50"), "Groceries", 
                OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food", PLN);
        
        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var allTransactions = transactionService.getAllTransactions();

        // Then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions.stream().map(t -> t.description()).toList())
                .containsExactlyInAnyOrder("Salary", "Groceries");
    }

    @Test
    void shouldGetTransactionsByAccountId() {
        // Given
        var account1 = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Account 1", PLN));
        var account2 = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Account 2", PLN));
        
        var command1 = new CreateTransactionCommand(account1.id(), new BigDecimal("1000.00"), "Salary", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary", PLN);
        var command2 = new CreateTransactionCommand(account2.id(), new BigDecimal("250.50"), "Groceries", 
                OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food", PLN);
        var command3 = new CreateTransactionCommand(account1.id(), new BigDecimal("100.00"), "Coffee", 
                OffsetDateTime.of(2024, 1, 17, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food", PLN);
        
        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);
        transactionService.createTransaction(command3);

        // When
        var account1Transactions = transactionService.getTransactionsByAccountId(account1.id());

        // Then
        assertThat(account1Transactions).hasSize(2);
        assertThat(account1Transactions.stream().map(t -> t.description()).toList())
                .containsExactlyInAnyOrder("Salary", "Coffee");
        assertThat(account1Transactions).allMatch(t -> t.accountId().equals(account1.id()));
    }

    @Test
    void shouldUpdateTransactionAmount() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var initialAmount = new BigDecimal("500.00");
        var createCommand = new CreateTransactionCommand(account.id(), initialAmount, "Initial transaction", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary", PLN);
        var transaction = transactionService.createTransaction(createCommand);
        
        var newAmount = new BigDecimal("750.00");
        var updateCommand = new UpdateTransactionCommand(transaction.id(), newAmount, null, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.description()).isEqualTo("Initial transaction");
        assertThat(updatedTransaction.category()).isEqualTo("Salary");
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance updated by difference (+250.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(new BigDecimal("750.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), amount, "Original description", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary", PLN);
        var transaction = transactionService.createTransaction(createCommand);
        
        var newDescription = "Updated description";
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, newDescription, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().amount()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo(newDescription);
        assertThat(updatedTransaction.category()).isEqualTo("Salary");
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(amount);
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), amount, "Test transaction", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food", PLN);
        var transaction = transactionService.createTransaction(createCommand);
        
        var newCategory = "Entertainment";
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, null, newCategory);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().amount()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
        assertThat(updatedTransaction.category()).isEqualTo(newCategory);
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(new BigDecimal("-100.00"));
    }

    @Test
    void shouldRejectUpdateForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();
        var updateCommand = new UpdateTransactionCommand(nonExistentTransactionId, null, "Updated description", null);

        // When & Then
        assertThatThrownBy(() -> transactionService.updateTransaction(updateCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void shouldDeleteTransaction() {
        // Given
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), amount, "Transaction to delete", 
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Test", PLN);
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transaction.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");

        // Verify account balance reversed (+100.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id().value());
        assertThat(updatedAccount.balance().amount()).isEqualTo(new BigDecimal("0.00"));

        // Verify transaction not in normal queries
        assertThat(transactionService.getAllTransactions()).isEmpty();
        assertThat(transactionService.getTransactionsByAccountId(account.id())).isEmpty();
    }

    @Test
    void shouldRejectDeleteForNonexistentTransaction() {
        // Given
        var nonExistentTransactionId = TransactionId.generate();

        // When & Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(nonExistentTransactionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");
    }
}