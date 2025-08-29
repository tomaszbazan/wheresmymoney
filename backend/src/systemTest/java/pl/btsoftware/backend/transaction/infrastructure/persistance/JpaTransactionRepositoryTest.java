package pl.btsoftware.backend.transaction.infrastructure.persistance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.*;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;
import static pl.btsoftware.backend.shared.TransactionType.INCOME;

@SystemTest
public class JpaTransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.findAll().forEach(
                transaction -> transactionRepository.store(transaction.delete())
        );
    }

    @Test
    void shouldStoreAndRetrieveTransaction() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("150.75"), EUR);
        var createdAt = OffsetDateTime.now(UTC);
        var transaction = Transaction.create(
                accountId,
                amount,
                "Grocery shopping",
                createdAt,
                EXPENSE,
                "Food"
        );

        // when
        transactionRepository.store(transaction);
        var retrievedTransaction = transactionRepository.findById(transaction.id());

        // then
        assertThat(retrievedTransaction).isPresent();
        assertThat(retrievedTransaction.get().id()).isEqualTo(transaction.id());
        assertThat(retrievedTransaction.get().accountId()).isEqualTo(accountId);
        assertThat(retrievedTransaction.get().amount()).isEqualTo(amount);
        assertThat(retrievedTransaction.get().description()).isEqualTo("Grocery shopping");
        assertThat(retrievedTransaction.get().type()).isEqualTo(EXPENSE);
        assertThat(retrievedTransaction.get().category()).isEqualTo("Food");
        assertThat(retrievedTransaction.get().tombstone().isDeleted()).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenTransactionNotFound() {
        // given
        var nonExistentId = TransactionId.generate();

        // when
        var result = transactionRepository.findById(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotFindDeletedTransactionById() {
        // given
        var accountId = AccountId.generate();
        var transaction = Transaction.create(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                OffsetDateTime.now(UTC),
                INCOME,
                "Salary"
        );
        transactionRepository.store(transaction);

        // when
        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);
        var result = transactionRepository.findById(transaction.id());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindDeletedTransactionByIdIncludingDeleted() {
        // given
        var accountId = AccountId.generate();
        var transaction = Transaction.create(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Test transaction",
                OffsetDateTime.now(UTC),
                INCOME,
                "Salary"
        );
        transactionRepository.store(transaction);

        // when
        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);
        var result = transactionRepository.findByIdIncludingDeleted(transaction.id());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().tombstone().isDeleted()).isTrue();
    }

    @Test
    void shouldFindAllActiveTransactions() {
        // given
        var accountId1 = AccountId.generate();
        var accountId2 = AccountId.generate();

        var transaction1 = Transaction.create(accountId1, Money.of(new BigDecimal("50.00"), USD), "Transaction 1", OffsetDateTime.now(UTC), EXPENSE, "Shopping");
        var transaction2 = Transaction.create(accountId2, Money.of(new BigDecimal("200.00"), GBP), "Transaction 2", OffsetDateTime.now(UTC), INCOME, "Work");
        var transaction3 = Transaction.create(accountId1, Money.of(new BigDecimal("75.00"), EUR), "Transaction 3", OffsetDateTime.now(UTC), EXPENSE, "Food");

        transactionRepository.store(transaction1);
        transactionRepository.store(transaction2);
        transactionRepository.store(transaction3);

        // Delete one transaction
        transactionRepository.store(transaction3.delete());

        // when
        var allTransactions = transactionRepository.findAll();

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

        var transaction1 = Transaction.create(targetAccountId, Money.of(new BigDecimal("100.00"), PLN), "Target account tx 1", OffsetDateTime.now(UTC), INCOME, "Work");
        var transaction2 = Transaction.create(otherAccountId, Money.of(new BigDecimal("50.00"), PLN), "Other account tx", OffsetDateTime.now(UTC), EXPENSE, "Shopping");
        var transaction3 = Transaction.create(targetAccountId, Money.of(new BigDecimal("25.00"), PLN), "Target account tx 2", OffsetDateTime.now(UTC), EXPENSE, "Food");

        transactionRepository.store(transaction1);
        transactionRepository.store(transaction2);
        transactionRepository.store(transaction3);

        // when
        var accountTransactions = transactionRepository.findByAccountId(targetAccountId);

        // then
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions).extracting("description").containsExactlyInAnyOrder("Target account tx 1", "Target account tx 2");
        assertThat(accountTransactions).allMatch(t -> t.accountId().equals(targetAccountId));
    }

    @Test
    void shouldNotIncludeDeletedTransactionsInAccountQuery() {
        // given
        var accountId = AccountId.generate();

        var activeTransaction = Transaction.create(accountId, Money.of(new BigDecimal("100.00"), PLN), "Active transaction", OffsetDateTime.now(UTC), INCOME, "Work");
        var toDeleteTransaction = Transaction.create(accountId, Money.of(new BigDecimal("50.00"), PLN), "To delete transaction", OffsetDateTime.now(UTC), EXPENSE, "Shopping");

        transactionRepository.store(activeTransaction);
        transactionRepository.store(toDeleteTransaction);

        // Delete one transaction
        transactionRepository.store(toDeleteTransaction.delete());

        // when
        var accountTransactions = transactionRepository.findByAccountId(accountId);

        // then
        assertThat(accountTransactions).hasSize(1);
        assertThat(accountTransactions.get(0).description()).isEqualTo("Active transaction");
        assertThat(accountTransactions.get(0).tombstone().isDeleted()).isFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsForAccount() {
        // given
        var nonExistentAccountId = AccountId.generate();

        // when
        var transactions = transactionRepository.findByAccountId(nonExistentAccountId);

        // then
        assertThat(transactions).isEmpty();
    }

    @Test
    void shouldUpdateTransaction() {
        // given
        var accountId = AccountId.generate();
        var originalTransaction = Transaction.create(
                accountId,
                Money.of(new BigDecimal("100.00"), PLN),
                "Original description",
                OffsetDateTime.now(UTC),
                EXPENSE,
                "Original category"
        );
        transactionRepository.store(originalTransaction);

        // when
        var updatedTransaction = originalTransaction
                .updateAmount(Money.of(new BigDecimal("150.00"), PLN))
                .updateDescription("Updated description")
                .updateCategory("Updated category");
        transactionRepository.store(updatedTransaction);

        // then
        var retrievedTransaction = transactionRepository.findById(originalTransaction.id());
        assertThat(retrievedTransaction).isPresent();
        assertThat(retrievedTransaction.get().amount().value()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(retrievedTransaction.get().description()).isEqualTo("Updated description");
        assertThat(retrievedTransaction.get().category()).isEqualTo("Updated category");
        assertThat(retrievedTransaction.get().updatedAt()).isAfter(retrievedTransaction.get().createdAt());
    }

    @Test
    void shouldHandleDifferentCurrencies() {
        // given
        var accountId = AccountId.generate();
        var plnTransaction = Transaction.create(accountId, Money.of(new BigDecimal("100.00"), PLN), "PLN transaction", OffsetDateTime.now(UTC), INCOME, "Work");
        var eurTransaction = Transaction.create(accountId, Money.of(new BigDecimal("50.00"), EUR), "EUR transaction", OffsetDateTime.now(UTC), EXPENSE, "Shopping");
        var usdTransaction = Transaction.create(accountId, Money.of(new BigDecimal("75.00"), USD), "USD transaction", OffsetDateTime.now(UTC), INCOME, "Bonus");
        var gbpTransaction = Transaction.create(accountId, Money.of(new BigDecimal("25.00"), GBP), "GBP transaction", OffsetDateTime.now(UTC), EXPENSE, "Travel");

        // when
        transactionRepository.store(plnTransaction);
        transactionRepository.store(eurTransaction);
        transactionRepository.store(usdTransaction);
        transactionRepository.store(gbpTransaction);

        var accountTransactions = transactionRepository.findByAccountId(accountId);

        // then
        assertThat(accountTransactions).hasSize(4);
        assertThat(accountTransactions).extracting(t -> t.amount().currency())
                .containsExactlyInAnyOrder(PLN, EUR, USD, GBP);
    }

    @Test
    void shouldHandleDifferentTransactionTypes() {
        // given
        var accountId = AccountId.generate();
        var incomeTransaction = Transaction.create(accountId, Money.of(new BigDecimal("1000.00"), PLN), "Salary", OffsetDateTime.now(UTC), INCOME, "Work");
        var expenseTransaction = Transaction.create(accountId, Money.of(new BigDecimal("200.00"), PLN), "Grocery", OffsetDateTime.now(UTC), EXPENSE, "Food");

        // when
        transactionRepository.store(incomeTransaction);
        transactionRepository.store(expenseTransaction);

        var accountTransactions = transactionRepository.findByAccountId(accountId);

        // then
        assertThat(accountTransactions).hasSize(2);
        assertThat(accountTransactions).extracting("type").containsExactlyInAnyOrder(INCOME, EXPENSE);
    }
}