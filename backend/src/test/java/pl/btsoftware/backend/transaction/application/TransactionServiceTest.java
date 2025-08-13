package pl.btsoftware.backend.transaction.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.Currency.USD;

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
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Salary";

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, category);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("1000.12"));
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.category()).isEqualTo("Salary");

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id())).isPresent();
        assertThat(transactionRepository.findAll()).hasSize(1);

        // Verify account balance updated by +1000.12
        var updatedAccount = accountModuleFacade.getAccount(account.id());
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("1000.12"));

        // Verify transaction ID added to account's transaction list
        assertThat(updatedAccount.transactionIds()).contains(transaction.id());
    }

    @Test
    void shouldCreateExpenseTransaction() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("250.50");
        var description = "Grocery shopping";
        var date = OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.EXPENSE;
        var category = "Food";

        // When
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, category);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().value()).isEqualTo(new BigDecimal("250.50"));
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");
        assertThat(transaction.category()).isEqualTo("Food");

        // Verify transaction is stored in repository
        assertThat(transactionRepository.findById(transaction.id())).isPresent();
        assertThat(transactionRepository.findAll()).hasSize(1);

        // Verify account balance updated by -250.50
        var updatedAccount = accountModuleFacade.getAccount(account.id());
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
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(nonExistentAccountId, Money.of(amount, PLN), description, date, type, category);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithEmptyDescription() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, category);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description must be between 1 and 200 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithDescriptionTooLong() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "A".repeat(201); // 201 characters
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, category);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Description must be between 1 and 200 characters");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectTransactionWithCurrencyMismatch() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var description = "Test transaction";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Test";

        // When & Then
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, USD), description, date, type, category);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction currency must match account currency");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    void shouldGetTransactionById() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Salary";
        var command = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), description, date, type, category);
        var createdTransaction = transactionService.createTransaction(command);

        // When
        var foundTransaction = transactionService.getTransactionById(createdTransaction.id());

        // Then
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.id()).isEqualTo(createdTransaction.id());
        assertThat(foundTransaction.accountId()).isEqualTo(account.id());
        assertThat(foundTransaction.amount().value()).isEqualTo(amount);
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
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var command1 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("1000.00"), PLN), "Salary",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary");
        var command2 = new CreateTransactionCommand(account.id(), Money.of(new BigDecimal("250.50"), PLN), "Groceries",
                OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food");
        
        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);

        // When
        var allTransactions = transactionService.getAllTransactions();

        // Then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions.stream().map(Transaction::description).toList())
                .containsExactlyInAnyOrder("Salary", "Groceries");
    }

    @Test
    void shouldGetTransactionsByAccountId() {
        // Given
        var account1 = accountModuleFacade.createAccount(new CreateAccountCommand("Account 1", PLN));
        var account2 = accountModuleFacade.createAccount(new CreateAccountCommand("Account 2", PLN));

        var command1 = new CreateTransactionCommand(account1.id(), Money.of(new BigDecimal("1000.00"), PLN), "Salary",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary");
        var command2 = new CreateTransactionCommand(account2.id(), Money.of(new BigDecimal("250.50"), PLN), "Groceries",
                OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food");
        var command3 = new CreateTransactionCommand(account1.id(), Money.of(new BigDecimal("100.00"), PLN), "Coffee",
                OffsetDateTime.of(2024, 1, 17, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food");
        
        transactionService.createTransaction(command1);
        transactionService.createTransaction(command2);
        transactionService.createTransaction(command3);

        // When
        var account1Transactions = transactionService.getTransactionsByAccountId(account1.id());

        // Then
        assertThat(account1Transactions).hasSize(2);
        assertThat(account1Transactions.stream().map(Transaction::description).toList())
                .containsExactlyInAnyOrder("Salary", "Coffee");
        assertThat(account1Transactions).allMatch(t -> t.accountId().equals(account1.id()));
    }

    @Test
    void shouldUpdateTransactionAmount() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var initialAmount = new BigDecimal("500.00");
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(initialAmount, PLN), "Initial transaction",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary");
        var transaction = transactionService.createTransaction(createCommand);

        var newAmount = Money.of(new BigDecimal("750.00"), PLN);
        var updateCommand = new UpdateTransactionCommand(transaction.id(), newAmount, null, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.description()).isEqualTo("Initial transaction");
        assertThat(updatedTransaction.category()).isEqualTo("Salary");
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance updated by difference (+250.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id());
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("750.00"));
    }

    @Test
    void shouldUpdateTransactionDescription() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Original description",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.INCOME, "Salary");
        var transaction = transactionService.createTransaction(createCommand);
        
        var newDescription = "Updated description";
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, newDescription, null);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo(newDescription);
        assertThat(updatedTransaction.category()).isEqualTo("Salary");
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id());
        assertThat(updatedAccount.balance().value()).isEqualTo(amount);
    }

    @Test
    void shouldUpdateTransactionCategory() {
        // Given
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Test transaction",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Food");
        var transaction = transactionService.createTransaction(createCommand);
        
        var newCategory = "Entertainment";
        var updateCommand = new UpdateTransactionCommand(transaction.id(), null, null, newCategory);

        // When
        var updatedTransaction = transactionService.updateTransaction(updateCommand);

        // Then
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.amount().value()).isEqualTo(amount);
        assertThat(updatedTransaction.description()).isEqualTo("Test transaction");
        assertThat(updatedTransaction.category()).isEqualTo(newCategory);
        assertThat(updatedTransaction.updatedAt()).isAfter(transaction.updatedAt());

        // Verify account balance unchanged
        var updatedAccount = accountModuleFacade.getAccount(account.id());
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("-100.00"));
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
        var account = accountModuleFacade.createAccount(new CreateAccountCommand("Test Account", PLN));
        var amount = new BigDecimal("100.00");
        var createCommand = new CreateTransactionCommand(account.id(), Money.of(amount, PLN), "Transaction to delete",
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC), TransactionType.EXPENSE, "Test");
        var transaction = transactionService.createTransaction(createCommand);

        // When
        transactionService.deleteTransaction(transaction.id());

        // Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transaction.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found");

        // Verify account balance reversed (+100.00)
        var updatedAccount = accountModuleFacade.getAccount(account.id());
        assertThat(updatedAccount.balance().value()).isEqualTo(new BigDecimal("0.00"));

        // Verify transaction not in normal queries
        assertThat(transactionService.getAllTransactions()).isEmpty();
        assertThat(transactionService.getTransactionsByAccountId(account.id())).isEmpty();

        // Verify transaction ID removed from account's transaction list
        assertThat(updatedAccount.transactionIds()).doesNotContain(transaction.id());
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