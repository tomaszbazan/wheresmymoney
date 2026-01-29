package pl.btsoftware.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.CreateAccountCommand;
import pl.btsoftware.backend.account.application.UpdateAccountCommand;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.audit.application.AuditLogService;
import pl.btsoftware.backend.audit.domain.AuditEntityType;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditOperation;
import pl.btsoftware.backend.audit.domain.EntityId;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.application.BillItemCommand;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.infrastructure.persistance.TransactionCommandFixture;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class HappyPathSystemTest {

    @Autowired private UsersModuleFacade usersModuleFacade;

    @Autowired private AccountModuleFacade accountModuleFacade;

    @Autowired private CategoryModuleFacade categoryModuleFacade;

    @Autowired private TransactionService transactionService;

    @Autowired private AuditLogService auditLogService;

    @Test
    void shouldCompleteFullUserJourneyWithAccountsCategoriesAndTransactions() {
        var user = registerUser();
        var account = createAccount(user.id());
        var incomeCategory = createIncomeCategory(user.id());
        var expenseCategory = createExpenseCategory(user.id());

        var incomeTransaction = addIncomeTransaction(account, incomeCategory, user.id());
        verifyAccountBalance(account.id(), user.id(), "5000.00");

        var expenseTransaction = addExpenseTransaction(account, expenseCategory, user.id());
        verifyAccountBalance(account.id(), user.id(), "4849.50");

        updateExpenseTransaction(expenseTransaction, user.id());
        verifyAccountBalance(account.id(), user.id(), "4800.00");

        updateAccountName(account.id(), user.id());
        updateCategoryDetails(expenseCategory.id(), user.id());

        deleteTransactionAndVerifyBalance(
                expenseTransaction.id(), account.id(), user.id(), "5000.00");
        deleteCategoryAndVerifyRemoval(expenseCategory.id(), user.id());
        deleteAccountAndVerifyRemoval(incomeTransaction.id(), account.id(), user.id());

        verifyAuditTrail(
                user,
                account,
                incomeCategory,
                expenseCategory,
                incomeTransaction,
                expenseTransaction);
    }

    private User registerUser() {
        var timestamp = System.currentTimeMillis();
        var command =
                new RegisterUserCommand(
                        "happy-path-auth-" + timestamp,
                        "happypath" + timestamp + "@example.com",
                        "Happy Path User",
                        "Happy Path Group " + timestamp,
                        null);
        var user = usersModuleFacade.registerUser(command);
        assertThat(user.id()).isNotNull();
        assertThat(user.email()).isEqualTo("happypath" + timestamp + "@example.com");
        return user;
    }

    private Account createAccount(UserId userId) {
        var command = new CreateAccountCommand("Main Account", PLN, userId);
        var account = accountModuleFacade.createAccount(command);
        assertThat(account.id()).isNotNull();
        assertThat(account.name()).isEqualTo("Main Account");
        assertThat(account.balance().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.balance().currency()).isEqualTo(PLN);
        return account;
    }

    private Category createIncomeCategory(UserId userId) {
        var command =
                new CreateCategoryCommand(
                        "Salary", CategoryType.INCOME, Color.of("#4CAF50"), userId);
        var category = categoryModuleFacade.createCategory(command);
        assertThat(category.id()).isNotNull();
        assertThat(category.name()).isEqualTo("Salary");
        assertThat(category.type()).isEqualTo(CategoryType.INCOME);
        return category;
    }

    private Category createExpenseCategory(UserId userId) {
        var command =
                new CreateCategoryCommand(
                        "Groceries", CategoryType.EXPENSE, Color.of("#FF5722"), userId);
        var category = categoryModuleFacade.createCategory(command);
        assertThat(category.id()).isNotNull();
        assertThat(category.name()).isEqualTo("Groceries");
        assertThat(category.type()).isEqualTo(CategoryType.EXPENSE);
        return category;
    }

    private Transaction addIncomeTransaction(Account account, Category category, UserId userId) {
        var command =
                TransactionCommandFixture.createCommand(
                        account.id(),
                        Money.of(new BigDecimal("5000.00"), PLN),
                        "Monthly salary",
                        LocalDate.now(),
                        INCOME,
                        category.id(),
                        userId);
        var transaction = transactionService.createTransaction(command);
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.amount().value()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(transaction.description()).isEqualTo("Monthly salary");
        assertThat(transaction.type()).isEqualTo(INCOME);
        return transaction;
    }

    private Transaction addExpenseTransaction(Account account, Category category, UserId userId) {
        var command =
                TransactionCommandFixture.createCommand(
                        account.id(),
                        Money.of(new BigDecimal("150.50"), PLN),
                        "Weekly shopping",
                        LocalDate.now(),
                        EXPENSE,
                        category.id(),
                        userId);
        var transaction = transactionService.createTransaction(command);
        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.amount().value()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(transaction.type()).isEqualTo(EXPENSE);
        return transaction;
    }

    private void updateExpenseTransaction(Transaction transaction, UserId userId) {
        var billItem =
                new BillItemCommand(
                        transaction.bill().items().getFirst().categoryId(),
                        Money.of(new BigDecimal("200.00"), PLN),
                        "Weekly shopping - updated");
        var command =
                new UpdateTransactionCommand(
                        transaction.id(), Money.of(new BigDecimal("200.00"), PLN), List.of(billItem));
        var updated = transactionService.updateTransaction(command, userId);
        assertThat(updated.description()).isEqualTo("Weekly shopping - updated");
        assertThat(updated.amount().value()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    private void updateAccountName(AccountId accountId, UserId userId) {
        var command = new UpdateAccountCommand(accountId, "Updated Account Name");
        var updated = accountModuleFacade.updateAccount(command, userId);
        assertThat(updated.name()).isEqualTo("Updated Account Name");
    }

    private void updateCategoryDetails(CategoryId categoryId, UserId userId) {
        var command =
                new UpdateCategoryCommand(
                        categoryId, "Food and Groceries", Color.of("#FF9800"), null);
        var updated = categoryModuleFacade.updateCategory(command, userId);
        assertThat(updated.name()).isEqualTo("Food and Groceries");
        assertThat(updated.color()).isEqualTo(Color.of("#FF9800"));
    }

    private void verifyAccountBalance(AccountId accountId, UserId userId, String expectedBalance) {
        var account = accountModuleFacade.getAccount(accountId, userId);
        assertThat(account.balance().value()).isEqualByComparingTo(new BigDecimal(expectedBalance));
    }

    private void deleteTransactionAndVerifyBalance(
            TransactionId transactionId,
            AccountId accountId,
            UserId userId,
            String expectedBalance) {
        transactionService.deleteTransaction(transactionId, userId);
        verifyAccountBalance(accountId, userId, expectedBalance);
    }

    private void deleteCategoryAndVerifyRemoval(CategoryId categoryId, UserId userId) {
        categoryModuleFacade.deleteCategory(categoryId.value(), userId);
        var remaining = categoryModuleFacade.getCategoriesByType(CategoryType.EXPENSE, userId);
        assertThat(remaining).noneMatch(c -> c.id().equals(categoryId));
    }

    private void deleteAccountAndVerifyRemoval(
            TransactionId transactionId, AccountId accountId, UserId userId) {
        transactionService.deleteTransaction(transactionId, userId);
        accountModuleFacade.deleteAccount(accountId, userId);
        var allAccounts = accountModuleFacade.getAccounts(userId);
        assertThat(allAccounts).noneMatch(a -> a.id().equals(accountId));
    }

    private void verifyAuditTrail(
            User user,
            Account account,
            Category incomeCategory,
            Category expenseCategory,
            Transaction incomeTransaction,
            Transaction expenseTransaction) {
        var groupId = user.groupId();

        var accountQuery =
                new AuditLogQuery(
                        groupId,
                        AuditEntityType.ACCOUNT,
                        EntityId.from(account.id().value()),
                        null,
                        null,
                        null,
                        null);
        var accountAuditLogs = auditLogService.findByQuery(accountQuery, PageRequest.of(0, 100));
        assertThat(accountAuditLogs.getContent()).hasSize(9);
        assertThat(accountAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.CREATE);
        assertThat(accountAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.UPDATE);
        assertThat(accountAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.DELETE);

        var incomeCategoryQuery =
                new AuditLogQuery(
                        groupId,
                        AuditEntityType.CATEGORY,
                        EntityId.from(incomeCategory.id().value()),
                        null,
                        null,
                        null,
                        null);
        var incomeCategoryAuditLogs =
                auditLogService.findByQuery(incomeCategoryQuery, PageRequest.of(0, 100));
        assertThat(incomeCategoryAuditLogs.getContent()).hasSize(1);
        assertThat(incomeCategoryAuditLogs.getContent().getFirst().operation())
                .isEqualTo(AuditOperation.CREATE);

        var expenseCategoryQuery =
                new AuditLogQuery(
                        groupId,
                        AuditEntityType.CATEGORY,
                        EntityId.from(expenseCategory.id().value()),
                        null,
                        null,
                        null,
                        null);
        var expenseCategoryAuditLogs =
                auditLogService.findByQuery(expenseCategoryQuery, PageRequest.of(0, 100));
        assertThat(expenseCategoryAuditLogs.getContent()).hasSize(3);
        assertThat(expenseCategoryAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.CREATE);
        assertThat(expenseCategoryAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.UPDATE);
        assertThat(expenseCategoryAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.DELETE);

        var incomeTransactionQuery =
                new AuditLogQuery(
                        groupId,
                        AuditEntityType.TRANSACTION,
                        EntityId.from(incomeTransaction.id().value()),
                        null,
                        null,
                        null,
                        null);
        var incomeTransactionAuditLogs =
                auditLogService.findByQuery(incomeTransactionQuery, PageRequest.of(0, 100));
        assertThat(incomeTransactionAuditLogs.getContent()).hasSize(2);
        assertThat(incomeTransactionAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.CREATE);
        assertThat(incomeTransactionAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.DELETE);

        var expenseTransactionQuery =
                new AuditLogQuery(
                        groupId,
                        AuditEntityType.TRANSACTION,
                        EntityId.from(expenseTransaction.id().value()),
                        null,
                        null,
                        null,
                        null);
        var expenseTransactionAuditLogs =
                auditLogService.findByQuery(expenseTransactionQuery, PageRequest.of(0, 100));
        assertThat(expenseTransactionAuditLogs.getContent()).hasSize(3);
        assertThat(expenseTransactionAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.CREATE);
        assertThat(expenseTransactionAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.UPDATE);
        assertThat(expenseTransactionAuditLogs.getContent())
                .anyMatch(log -> log.operation() == AuditOperation.DELETE);
    }
}
