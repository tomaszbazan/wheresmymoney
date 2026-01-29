package pl.btsoftware.backend.transaction.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.transaction.infrastructure.persistance.TransactionCommandFixture.createCommand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

class SimpleTransactionServiceTest {
    private TransactionService transactionService;
    private AccountModuleFacade accountModuleFacade;
    private GroupId testGroupId;

    @BeforeEach
    void setUp() {
        var transactionRepository = new InMemoryTransactionRepository();
        var accountRepository = new InMemoryAccountRepository();
        var usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        var categoryQueryFacade = Mockito.mock(CategoryQueryFacade.class);

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
    void shouldCreateIncomeTransactionWithSingleBillItem() {
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        var amount = new BigDecimal("1000.12");
        var description = "Salary payment";
        var date = LocalDate.of(2024, 1, 15);
        var categoryId = CategoryId.generate();

        var command =
                createCommand(
                        account.id(),
                        Money.of(amount, PLN),
                        description,
                        date,
                        TransactionType.INCOME,
                        categoryId,
                        userId);

        var transaction = transactionService.createTransaction(command);

        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.amount().value()).isEqualByComparingTo(amount);
        assertThat(transaction.type()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.bill()).isNotNull();
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);
        assertThat(transaction.bill().items().getFirst().description()).isEqualTo(description);
        assertThat(transaction.bill().items().getFirst().amount()).isEqualTo(Money.of(amount, PLN));
    }

    @Test
    void shouldCreateExpenseTransactionWithSingleBillItem() {
        var userId = UserId.generate();
        var createAccountCommand = new CreateAccountCommand("Test Account", PLN, userId);
        var account = accountModuleFacade.createAccount(createAccountCommand);
        accountModuleFacade.deposit(account.id(), Money.of(BigDecimal.valueOf(2000), PLN), userId);

        var amount = new BigDecimal("500.50");
        var description = "Grocery shopping";
        var date = LocalDate.of(2024, 1, 20);
        var categoryId = CategoryId.generate();

        var command =
                createCommand(
                        account.id(),
                        Money.of(amount, PLN),
                        description,
                        date,
                        TransactionType.EXPENSE,
                        categoryId,
                        userId);

        var transaction = transactionService.createTransaction(command);

        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.amount().value()).isEqualByComparingTo(amount);
        assertThat(transaction.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.bill()).isNotNull();
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.bill().items().getFirst().categoryId()).isEqualTo(categoryId);
        assertThat(transaction.bill().items().getFirst().description()).isEqualTo(description);
    }
}
