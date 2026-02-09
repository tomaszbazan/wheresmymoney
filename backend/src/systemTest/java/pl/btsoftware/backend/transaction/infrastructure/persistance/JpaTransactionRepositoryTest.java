package pl.btsoftware.backend.transaction.infrastructure.persistance;

import static java.math.BigDecimal.TEN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.*;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class JpaTransactionRepositoryTest {

    private final UserId testUserId = new UserId("test-user");
    private final GroupId testGroupId = new GroupId(randomUUID());

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository
                .findAll(TransactionSearchCriteria.empty(), testGroupId, Pageable.ofSize(20))
                .forEach(transaction -> transactionRepository.store(transaction.delete()));
    }

    private AuditInfo createAuditInfo() {
        return AuditInfo.create(
                testUserId.value(),
                testGroupId.value(),
                OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS));
    }

    private Transaction createTransaction(
            AccountId accountId,
            Money amount,
            String description,
            TransactionType type,
            CategoryId categoryId,
            AuditInfo auditInfo) {
        return createTransaction(accountId, amount, description, type, categoryId, auditInfo, LocalDate.now());
    }

    private Transaction createTransaction(
            AccountId accountId,
            Money amount,
            String description,
            TransactionType type,
            CategoryId categoryId,
            AuditInfo auditInfo,
            LocalDate transactionDate) {
        var hash = TransactionHashCalculator.calculateHash(accountId, amount, description, transactionDate, type);
        var billItem = new BillItem(BillItemId.generate(), categoryId, amount, description);
        var bill = new Bill(BillId.generate(), List.of(billItem));
        return Transaction.create(accountId, type, bill, transactionDate, hash, auditInfo);
    }

    @Test
    void shouldStoreAndRetrieveTransaction() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("150.75"), EUR);
        var auditInfo = createAuditInfo();
        var transaction =
                createTransaction(accountId, amount, "Grocery shopping", EXPENSE, CategoryId.generate(), auditInfo);

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
        assertThat(retrievedTransaction.get().bill()).isNotNull();
        assertThat(retrievedTransaction.get().bill().items()).hasSize(1);
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
        var auditInfo = createAuditInfo();
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo);
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
        var auditInfo = createAuditInfo();
        var transaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo);
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

        var auditInfo = createAuditInfo();
        var transaction1 = createTransaction(
                accountId1,
                Money.of(new BigDecimal("50.00"), USD),
                "Transaction 1",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);
        var transaction2 = createTransaction(
                accountId2,
                Money.of(new BigDecimal("200.00"), GBP),
                "Transaction 2",
                INCOME,
                CategoryId.generate(),
                auditInfo);
        var transaction3 = createTransaction(
                accountId1,
                Money.of(new BigDecimal("75.00"), EUR),
                "Transaction 3",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        transactionRepository.store(transaction1);
        transactionRepository.store(transaction2);
        transactionRepository.store(transaction3);

        // Delete one transaction
        transactionRepository.store(transaction3.delete());

        // when
        var allTransactions =
                transactionRepository.findAll(TransactionSearchCriteria.empty(), testGroupId, Pageable.ofSize(20));

        // then
        assertThat(allTransactions).hasSize(2);
        assertThat(allTransactions)
                .extracting(Transaction::description)
                .containsExactlyInAnyOrder("Transaction 1", "Transaction 2");
        assertThat(allTransactions).allMatch(t -> !t.tombstone().isDeleted());
    }

    @Test
    void shouldNotIncludeDeletedTransactionsInAccountQuery() {
        // given
        var accountId = AccountId.generate();

        var auditInfo = createAuditInfo();
        var activeTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Active transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo);
        var toDeleteTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("50.00"), PLN),
                "To delete transaction",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        transactionRepository.store(activeTransaction);
        transactionRepository.store(toDeleteTransaction);

        // Delete one transaction
        transactionRepository.store(toDeleteTransaction.delete());

        // when
        var accountTransactions =
                transactionRepository
                        .findAll(TransactionSearchCriteria.empty(), testGroupId, Pageable.ofSize(20))
                        .stream()
                        .toList();

        // then
        assertThat(accountTransactions).hasSize(1);
        assertThat(accountTransactions.getFirst().description()).isEqualTo("Active transaction");
        assertThat(accountTransactions.getFirst().tombstone().isDeleted()).isFalse();
    }

    @Test
    void shouldUpdateTransaction() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var originalCategoryId = CategoryId.generate();
        var originalTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Original description",
                EXPENSE,
                originalCategoryId,
                auditInfo);
        transactionRepository.store(originalTransaction);

        // when
        var newCategoryId = CategoryId.generate();
        var newAmount = Money.of(new BigDecimal("150.00"), PLN);
        var newBillItem = new BillItem(BillItemId.generate(), newCategoryId, newAmount, "Updated description");
        var newBill = new Bill(BillId.generate(), List.of(newBillItem));
        var updatedTransaction = originalTransaction.updateBill(newBill, null, null, testUserId);
        transactionRepository.store(updatedTransaction);

        // then
        var retrievedTransaction = transactionRepository.findById(originalTransaction.id(), testGroupId);
        assertThat(retrievedTransaction).isPresent();
        assertThat(retrievedTransaction.get().amount().value()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(retrievedTransaction.get().description()).isEqualTo("Updated description");
        assertThat(retrievedTransaction.get().bill().items().getFirst().categoryId())
                .isEqualTo(newCategoryId);
        assertThat(retrievedTransaction.get().lastUpdatedAt())
                .isAfter(retrievedTransaction.get().createdAt());
        assertThat(retrievedTransaction.get().lastUpdatedBy()).isEqualTo(testUserId);
    }

    @Test
    void shouldHandleDifferentCurrencies() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var plnTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "PLN transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo);
        var eurTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("50.00"), EUR),
                "EUR transaction",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);
        var usdTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("75.00"), USD),
                "USD transaction",
                INCOME,
                CategoryId.generate(),
                auditInfo);
        var gbpTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("25.00"), GBP),
                "GBP transaction",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        // when
        transactionRepository.store(plnTransaction);
        transactionRepository.store(eurTransaction);
        transactionRepository.store(usdTransaction);
        transactionRepository.store(gbpTransaction);

        var accountTransactions =
                transactionRepository
                        .findAll(TransactionSearchCriteria.empty(), testGroupId, Pageable.ofSize(20))
                        .stream()
                        .toList();

        // then
        assertThat(accountTransactions).hasSize(4);
        assertThat(accountTransactions)
                .extracting(t -> t.amount().currency())
                .containsExactlyInAnyOrder(PLN, EUR, USD, GBP);
    }

    @Test
    void shouldHandleDifferentTransactionTypes() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var incomeTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("1000.00"), PLN),
                "Salary",
                INCOME,
                CategoryId.generate(),
                auditInfo);
        var expenseTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("200.00"), PLN),
                "Grocery",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        // when
        transactionRepository.store(incomeTransaction);
        transactionRepository.store(expenseTransaction);

        var accountTransactions =
                transactionRepository
                        .findAll(TransactionSearchCriteria.empty(), testGroupId, Pageable.ofSize(20))
                        .stream()
                        .toList();

        // then
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions).extracting("type").containsExactlyInAnyOrder(INCOME, EXPENSE);
    }

    @Test
    void shouldReturnTrueWhenTransactionsExistForCategory() {
        // given
        var categoryId = CategoryId.generate();
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var transaction = createTransaction(
                accountId, Money.of(new BigDecimal("100.00"), PLN), "Test transaction", EXPENSE, categoryId, auditInfo);
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
        var auditInfo = createAuditInfo();
        var transaction = createTransaction(
                accountId, Money.of(new BigDecimal("100.00"), PLN), "Test transaction", EXPENSE, categoryId, auditInfo);
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

    @Test
    void shouldFilterTransactionsByType() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var incomeTransaction = createTransaction(
                accountId, Money.of(new BigDecimal("100.00"), PLN), "Income", INCOME, CategoryId.generate(), auditInfo);
        var expenseTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Expense",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        transactionRepository.store(incomeTransaction);
        transactionRepository.store(expenseTransaction);

        var criteria = TransactionSearchCriteria.incomes();

        // when
        var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(incomeTransaction.id());
    }

    @Test
    void shouldReturnAllTransactionsWhenNoTypeFilterProvided() {
        // given
        var accountId = AccountId.generate();
        var auditInfo = createAuditInfo();
        var incomeTransaction = createTransaction(
                accountId, Money.of(new BigDecimal("100.00"), PLN), "Income", INCOME, CategoryId.generate(), auditInfo);
        var expenseTransaction = createTransaction(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Expense",
                EXPENSE,
                CategoryId.generate(),
                auditInfo);

        transactionRepository.store(incomeTransaction);
        transactionRepository.store(expenseTransaction);

        var criteria = TransactionSearchCriteria.empty();

        // when
        var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Nested
    class TransactionFilteringTest {

        @Test
        void shouldFilterByDateFrom() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var transaction1 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "Old",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().minusDays(10));
            var transaction2 = createTransaction(
                    accountId, Money.of(TEN, EUR), "New", EXPENSE, categoryId, auditInfo, LocalDate.now());
            var transaction3 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "New",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().plusDays(10));

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);
            transactionRepository.store(transaction3);

            var criteria = new TransactionSearchCriteria(
                    Set.of(), LocalDate.now().minusDays(5), null, null, null, Set.of(), Set.of(), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2).containsExactlyInAnyOrder(transaction2, transaction3);
        }

        @Test
        void shouldFilterByDateTo() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var transaction1 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "Old",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().minusDays(10));
            var transaction2 = createTransaction(
                    accountId, Money.of(TEN, EUR), "New", EXPENSE, categoryId, auditInfo, LocalDate.now());
            var transaction3 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "New",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().plusDays(10));

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);
            transactionRepository.store(transaction3);

            var criteria = new TransactionSearchCriteria(
                    Set.of(), null, LocalDate.now(), null, null, Set.of(), Set.of(), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2).containsExactlyInAnyOrder(transaction1, transaction2);
        }

        @Test
        void shouldFilterByDateRange() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var transaction1 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "Old",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().minusDays(10));
            var transaction2 = createTransaction(
                    accountId, Money.of(TEN, EUR), "New", EXPENSE, categoryId, auditInfo, LocalDate.now());
            var transaction3 = createTransaction(
                    accountId,
                    Money.of(TEN, EUR),
                    "New",
                    EXPENSE,
                    categoryId,
                    auditInfo,
                    LocalDate.now().plusDays(10));

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);
            transactionRepository.store(transaction3);

            var criteria = new TransactionSearchCriteria(
                    Set.of(),
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(5),
                    null,
                    null,
                    Set.of(),
                    Set.of(),
                    null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(transaction2);
        }

        @Test
        void shouldFilterByMinAmount() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var smallTransaction = createTransaction(
                    accountId, Money.of(new BigDecimal("10.00"), EUR), "Small", INCOME, categoryId, auditInfo);
            var largeTransaction = createTransaction(
                    accountId, Money.of(new BigDecimal("100.00"), EUR), "Large", INCOME, categoryId, auditInfo);

            transactionRepository.store(smallTransaction);
            transactionRepository.store(largeTransaction);

            var criteria = new TransactionSearchCriteria(
                    Set.of(), null, null, new BigDecimal("50.00"), null, Set.of(), Set.of(), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(largeTransaction);
        }

        @Test
        void shouldFilterByMaxAmount() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var smallTransaction = createTransaction(
                    accountId, Money.of(new BigDecimal("10.00"), EUR), "Small", INCOME, categoryId, auditInfo);
            var largeTransaction = createTransaction(
                    accountId, Money.of(new BigDecimal("100.00"), EUR), "Large", INCOME, categoryId, auditInfo);

            transactionRepository.store(smallTransaction);
            transactionRepository.store(largeTransaction);

            var criteria = new TransactionSearchCriteria(
                    Set.of(), null, null, null, new BigDecimal("50.00"), Set.of(), Set.of(), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(smallTransaction);
        }

        @Test
        void shouldFilterByAccountIds() {
            // given
            var auditInfo = createAuditInfo();
            var accountId1 = AccountId.generate();
            var accountId2 = AccountId.generate();
            var categoryId = CategoryId.generate();

            var transaction1 =
                    createTransaction(accountId1, Money.of(TEN, EUR), "Acc 1", EXPENSE, categoryId, auditInfo);
            var transaction2 =
                    createTransaction(accountId2, Money.of(TEN, EUR), "Acc 2", EXPENSE, categoryId, auditInfo);

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);

            var criteria =
                    new TransactionSearchCriteria(Set.of(), null, null, null, null, Set.of(accountId1), Set.of(), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(transaction1);
        }

        @Test
        void shouldFilterByCategoryIds() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId1 = CategoryId.generate();
            var categoryId2 = CategoryId.generate();

            var transaction1 =
                    createTransaction(accountId, Money.of(TEN, EUR), "Cat 1", EXPENSE, categoryId1, auditInfo);
            var transaction2 =
                    createTransaction(accountId, Money.of(TEN, EUR), "Cat 2", EXPENSE, categoryId2, auditInfo);

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);

            var criteria = new TransactionSearchCriteria(
                    Set.of(), null, null, null, null, Set.of(), Set.of(categoryId1), null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(transaction1);
        }

        @Test
        void shouldFilterByTypes() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var income = createTransaction(accountId, Money.of(TEN, EUR), "Income", INCOME, categoryId, auditInfo);
            var expense = createTransaction(accountId, Money.of(TEN, EUR), "Expense", EXPENSE, categoryId, auditInfo);

            transactionRepository.store(income);
            transactionRepository.store(expense);

            var criteria = TransactionSearchCriteria.incomes();

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(income);
        }

        @Test
        void shouldFilterByMultipleCriteria() {
            // given
            var auditInfo = createAuditInfo();
            var account1 = AccountId.generate();
            var account2 = AccountId.generate();
            var cat1 = CategoryId.generate();
            var cat2 = CategoryId.generate();

            // Match: Account 1, Cat 1, Date Now, Amount 100
            var target = createTransaction(
                    account1,
                    Money.of(new BigDecimal("100.00"), EUR),
                    "Target",
                    EXPENSE,
                    cat1,
                    auditInfo,
                    LocalDate.now());

            // Mismatch: Account 2
            var wrongAccount = createTransaction(
                    account2,
                    Money.of(new BigDecimal("100.00"), EUR),
                    "Wrong Account",
                    EXPENSE,
                    cat1,
                    auditInfo,
                    LocalDate.now());

            // Mismatch: Category 2
            var wrongCat = createTransaction(
                    account1,
                    Money.of(new BigDecimal("100.00"), EUR),
                    "Wrong Cat",
                    EXPENSE,
                    cat2,
                    auditInfo,
                    LocalDate.now());

            // Mismatch: Date old
            var wrongDate = createTransaction(
                    account1,
                    Money.of(new BigDecimal("100.00"), EUR),
                    "Wrong Date",
                    EXPENSE,
                    cat1,
                    auditInfo,
                    LocalDate.now().minusDays(10));

            transactionRepository.store(target);
            transactionRepository.store(wrongAccount);
            transactionRepository.store(wrongCat);
            transactionRepository.store(wrongDate);

            var criteria = new TransactionSearchCriteria(
                    Set.of(),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1),
                    null,
                    null,
                    Set.of(account1),
                    Set.of(cat1),
                    null);

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(target);
        }

        @Test
        void shouldFilterByDescription() {
            // given
            var auditInfo = createAuditInfo();
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();

            var transaction1 = createTransaction(
                    accountId, Money.of(TEN, EUR), "Grocery shopping", EXPENSE, categoryId, auditInfo);
            var transaction2 =
                    createTransaction(accountId, Money.of(TEN, EUR), "Car fuel", EXPENSE, categoryId, auditInfo);

            transactionRepository.store(transaction1);
            transactionRepository.store(transaction2);

            var criteria =
                    new TransactionSearchCriteria(Set.of(), null, null, null, null, Set.of(), Set.of(), "Grocery");

            // when
            var result = transactionRepository.findAll(criteria, testGroupId, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1).containsExactly(transaction1);
        }
    }
}
