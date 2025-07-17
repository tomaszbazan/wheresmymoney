package pl.btsoftware.backend.transaction.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.error.AccountNotFoundException;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.TransactionType;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", "PLN"));
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.INCOME;
        var category = "Salary";

        // When
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().amount()).isEqualTo(new BigDecimal("1000.12"));
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.description()).isEqualTo("Salary payment");
        assertThat(transaction.when()).isEqualTo(date);
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
        var account = accountModuleFacade.createAccount(new AccountModuleFacade.CreateAccountCommand("Test Account", "PLN"));
        var amount = new BigDecimal("250.50");
        var description = "Grocery shopping";
        var date = OffsetDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC);
        var type = TransactionType.EXPENSE;
        var category = "Food";

        // When
        var command = new CreateTransactionCommand(account.id(), amount, description, date, type, category);
        Transaction transaction = transactionService.createTransaction(command);

        // Then
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(account.id());
        assertThat(transaction.amount().amount()).isEqualTo(new BigDecimal("250.50"));
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.description()).isEqualTo("Grocery shopping");
        assertThat(transaction.when()).isEqualTo(date);
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
        var command = new CreateTransactionCommand(nonExistentAccountId, amount, description, date, type, category);
        assertThatThrownBy(() -> transactionService.createTransaction(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");

        // Verify no transaction was created
        assertThat(transactionRepository.findAll()).isEmpty();
    }
}