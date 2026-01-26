package pl.btsoftware.backend.transaction.infrastructure.persistance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHashCalculator;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.*;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

@SystemTest
public class JpaTransactionRepositoryTest {

    private final UserId testUserId = new UserId("test-user");
    private final GroupId testGroupId = new GroupId(UUID.randomUUID());
    private final TransactionHashCalculator hashCalculator = new TransactionHashCalculator();

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.findAll(testGroupId, Pageable.ofSize(20)).forEach(
                transaction -> transactionRepository.store(transaction.delete())
        );
    }

    private Transaction createTransaction(AccountId accountId, Money amount, String description,
                                          pl.btsoftware.backend.shared.TransactionType type,
                                          CategoryId categoryId, AuditInfo auditInfo) {
        var transactionDate = LocalDate.now();
        var hash = hashCalculator.calculateHash(accountId, amount, description, transactionDate, type);
        return Transaction.create(accountId, amount, description, type, categoryId, transactionDate, hash, auditInfo);
    }

    @Test
    void shouldStoreAndRetrieveTransaction() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("150.75"), EUR);
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction = createTransaction(
                accountId,
                amount,
                "Grocery shopping",
                EXPENSE,
                CategoryId.generate(),
                auditInfo
        );

        // when
        transactionRepository.store(transaction);
        var retrievedTransaction = transactionRepository.findById(transaction.id(), testGroupId);

        // then
        assertThat(retrievedTransaction).isPresent();
        assertThat(retrievedTransaction.get().id()).isEqualTo(transaction.id());
        assertThat(retrievedTransaction.get().accountId()).isEqualTo(accountId);
        assertThat(retrievedTransaction.get().amount()).isEqualTo(amount);
        assertThat(retrievedTransaction.get().description()).isEqualTo("Grocery shopping");
        assertThat(retrievedTransaction.get().type()).isEqualTo(EXPENSE);
        assertThat(retrievedTransaction.get().categoryId()).isNotNull();
        assertThat(retrievedTransaction.get().tombstone().isDeleted()).isFalse();
        assertThat(retrievedTransaction.get().createdBy()).isEqualTo(testUserId);
        assertThat(retrievedTransaction.get().ownedBy()).isEqualTo(testGroupId);
    }

    @Test
    void shouldReturnEmptyWhenTransactionNotFound() {
        // given
        var nonExistentId = TransactionId.generate();

        // when
        var result = transactionRepository.findById(nonExistentId, testGroupId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotFindDeletedTransactionById() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo
        );
        transactionRepository.store(transaction);

        // when
        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);
        var result = transactionRepository.findById(transaction.id(), testGroupId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindDeletedTransactionByIdIncludingDeleted() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo
        );
        transactionRepository.store(transaction);

        // when
        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);
        var result = transactionRepository.findByIdIncludingDeleted(transaction.id(), testGroupId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().tombstone().isDeleted()).isTrue();
    }

    @Test
    void shouldFindAllActiveTransactions() {
        // given
        var accountId1 = AccountId.generate();
        var accountId2 = AccountId.generate();

        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction1 = createTransaction(accountId1, Money.of(new BigDecimal("50.00"), USD), "Transaction 1", EXPENSE, CategoryId.generate(), auditInfo);
        var transaction2 = createTransaction(accountId2, Money.of(new BigDecimal("200.00"), GBP), "Transaction 2", INCOME, CategoryId.generate(), auditInfo);
        var transaction3 = createTransaction(accountId1, Money.of(new BigDecimal("75.00"), EUR), "Transaction 3", EXPENSE, CategoryId.generate(), auditInfo);

        transactionRepository.store(transaction1);
        transactionRepository.store(transaction2);
        transactionRepository.store(transaction3);

        // Delete one transaction
        transactionRepository.store(transaction3.delete());

        // when
        var allTransactions = transactionRepository.findAll(testGroupId, Pageable.ofSize(20));

        // then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions).extracting("description").containsExactlyInAnyOrder("Transaction 1", "Transaction 2");
        assertThat(allTransactions).allMatch(t -> !t.tombstone().isDeleted());
    }

    @Test
    void shouldFindTransactionsByAccountId() {
        // given
        var targetAccountId = AccountId.generate();
        var otherAccountId = AccountId.generate();

        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction1 = createTransaction(targetAccountId, Money.of(new BigDecimal("100.00"), PLN),
                "Target account tx 1", INCOME, CategoryId.generate(), auditInfo);
        var transaction2 = createTransaction(otherAccountId, Money.of(new BigDecimal("50.00"), PLN),
                "Other account tx", EXPENSE, CategoryId.generate(), auditInfo);
        var transaction3 = createTransaction(targetAccountId, Money.of(new BigDecimal("25.00"), PLN),
                "Target account tx 2", EXPENSE, CategoryId.generate(), auditInfo);

        transactionRepository.store(transaction1);
        transactionRepository.store(transaction2);
        transactionRepository.store(transaction3);

        // when
        var accountTransactions = transactionRepository.findByAccountId(targetAccountId, testGroupId);

        // then
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions).extracting("description")
                .containsExactlyInAnyOrder("Target account tx 1", "Target account tx 2");
        assertThat(accountTransactions).allMatch(t -> t.accountId().equals(targetAccountId));
    }

    @Test
    void shouldNotIncludeDeletedTransactionsInAccountQuery() {
        // given
        var accountId = AccountId.generate();

        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var activeTransaction = createTransaction(accountId, Money.of(new BigDecimal("100.00"), PLN),
                "Active transaction", INCOME, CategoryId.generate(), auditInfo);
        var toDeleteTransaction = createTransaction(accountId, Money.of(new BigDecimal("50.00"), PLN),
                "To delete transaction", EXPENSE, CategoryId.generate(), auditInfo);

        transactionRepository.store(activeTransaction);
        transactionRepository.store(toDeleteTransaction);

        // Delete one transaction
        transactionRepository.store(toDeleteTransaction.delete());

        // when
        var accountTransactions = transactionRepository.findByAccountId(accountId, testGroupId);

        // then
        assertThat(accountTransactions).hasSize(1);
        assertThat(accountTransactions.getFirst().description()).isEqualTo("Active transaction");
        assertThat(accountTransactions.getFirst().tombstone().isDeleted()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsForAccount() {
        // given
        var nonExistentAccountId = AccountId.generate();

        // when
        var transactions = transactionRepository.findByAccountId(nonExistentAccountId, testGroupId);

        // then
        assertThat(transactions).isEmpty();
    }

    @Test
    void shouldUpdateTransaction() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var originalCategoryId = CategoryId.generate();
        var originalTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Original description",
                EXPENSE,
                originalCategoryId,
                auditInfo
        );
        transactionRepository.store(originalTransaction);

        // when
        var newCategoryId = CategoryId.generate();
        var updatedTransaction = originalTransaction
                .updateAmount(Money.of(new BigDecimal("150.00"), PLN), testUserId)
                .updateDescription("Updated description", testUserId)
                .updateCategory(newCategoryId, testUserId);
        transactionRepository.store(updatedTransaction);

        // then
        var retrievedTransaction = transactionRepository.findById(originalTransaction.id(), testGroupId);
        assertThat(retrievedTransaction).isPresent();
        assertThat(retrievedTransaction.get().amount().value()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(retrievedTransaction.get().description()).isEqualTo("Updated description");
        assertThat(retrievedTransaction.get().categoryId()).isEqualTo(newCategoryId);
        assertThat(retrievedTransaction.get().lastUpdatedAt()).isAfter(retrievedTransaction.get().createdAt());
        assertThat(retrievedTransaction.get().lastUpdatedBy()).isEqualTo(testUserId);
    }

    @Test
    void shouldHandleDifferentCurrencies() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var plnTransaction = createTransaction(accountId, Money.of(new BigDecimal("100.00"), PLN),
                "PLN transaction", INCOME, CategoryId.generate(), auditInfo);
        var eurTransaction = createTransaction(accountId, Money.of(new BigDecimal("50.00"), EUR),
                "EUR transaction", EXPENSE, CategoryId.generate(), auditInfo);
        var usdTransaction = createTransaction(accountId, Money.of(new BigDecimal("75.00"), USD),
                "USD transaction", INCOME, CategoryId.generate(), auditInfo);
        var gbpTransaction = createTransaction(accountId, Money.of(new BigDecimal("25.00"), GBP),
                "GBP transaction", EXPENSE, CategoryId.generate(), auditInfo);

        // when
        transactionRepository.store(plnTransaction);
        transactionRepository.store(eurTransaction);
        transactionRepository.store(usdTransaction);
        transactionRepository.store(gbpTransaction);

        var accountTransactions = transactionRepository.findByAccountId(accountId, testGroupId);

        // then
        assertThat(accountTransactions).hasSize(4);
        assertThat(accountTransactions).extracting(t -> t.amount().currency())
                .containsExactlyInAnyOrder(PLN, EUR, USD, GBP);
    }

    @Test
    void shouldHandleDifferentTransactionTypes() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var incomeTransaction = createTransaction(accountId, Money.of(new BigDecimal("1000.00"), PLN), "Salary", INCOME, CategoryId.generate(), auditInfo);
        var expenseTransaction = createTransaction(accountId, Money.of(new BigDecimal("200.00"), PLN), "Grocery", EXPENSE, CategoryId.generate(), auditInfo);

        // when
        transactionRepository.store(incomeTransaction);
        transactionRepository.store(expenseTransaction);

        var accountTransactions = transactionRepository.findByAccountId(accountId, testGroupId);

        // then
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions).extracting("type").containsExactlyInAnyOrder(INCOME, EXPENSE);
    }

    @Test
    void shouldReturnTrueWhenTransactionsExistForCategory() {
        // given
        var categoryId = CategoryId.generate();
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                EXPENSE,
                categoryId,
                auditInfo
        );
        transactionRepository.store(transaction);

        // when
        var exists = transactionRepository.existsByCategoryId(categoryId, testGroupId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnlyDeletedTransactionsExistForCategory() {
        // given
        var categoryId = CategoryId.generate();
        var accountId = AccountId.generate();
        var auditInfo = AuditInfo.create(testUserId.value(), testGroupId.value());
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                EXPENSE,
                categoryId,
                auditInfo
        );
        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);

        // when
        var exists = transactionRepository.existsByCategoryId(categoryId, testGroupId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoTransactionsExistForCategory() {
        // given
        var categoryId = CategoryId.generate();

        // when
        var exists = transactionRepository.existsByCategoryId(categoryId, testGroupId);

        // then
        assertThat(exists).isFalse();
    }
}
