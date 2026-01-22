package pl.btsoftware.backend.transaction;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TransactionQueryFacadeTest {

    private TransactionQueryFacade transactionQueryFacade;
    private InMemoryTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository = new InMemoryTransactionRepository();
        transactionQueryFacade = new TransactionQueryFacade(transactionRepository);
    }

    @Test
    void shouldReturnFalseWhenNoTransactionsExistForCategory() {
        var categoryId = CategoryId.generate();
        var groupId = GroupId.generate();

        var hasTransactions = transactionQueryFacade.hasTransactions(categoryId, groupId);

        assertThat(hasTransactions).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTransactionsExistForCategory() {
        var categoryId = CategoryId.generate();
        var accountId = AccountId.generate();
        var groupId = GroupId.generate();
        var auditInfo = Instancio.of(AuditInfo.class)
                .set(field(AuditInfo::fromGroup), groupId)
                .create();

        var transaction = Transaction.create(
                accountId,
                Money.zero(),
                "Test",
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.now(),
                new TransactionHash("a".repeat(64)),
                auditInfo
        );
        transactionRepository.store(transaction);

        var hasTransactions = transactionQueryFacade.hasTransactions(categoryId, groupId);

        assertThat(hasTransactions).isTrue();
    }

    @Test
    void shouldReturnFalseForDifferentCategoryId() {
        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();
        var accountId = AccountId.generate();
        var groupId = GroupId.generate();
        var auditInfo = Instancio.of(AuditInfo.class)
                .set(field(AuditInfo::fromGroup), groupId)
                .create();

        var transaction = Transaction.create(
                accountId,
                Money.zero(),
                "Test",
                TransactionType.EXPENSE,
                categoryId1,
                LocalDate.now(),
                new TransactionHash("a".repeat(64)),
                auditInfo
        );
        transactionRepository.store(transaction);

        var hasTransactions = transactionQueryFacade.hasTransactions(categoryId2, groupId);

        assertThat(hasTransactions).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoTransactionsExistForAccount() {
        var accountId = AccountId.generate();
        var groupId = GroupId.generate();

        var hasTransactions = transactionQueryFacade.hasTransactions(accountId, groupId);

        assertThat(hasTransactions).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTransactionsExistForAccount() {
        var accountId = AccountId.generate();
        var categoryId = CategoryId.generate();
        var groupId = GroupId.generate();
        var auditInfo = Instancio.of(AuditInfo.class)
                .set(field(AuditInfo::fromGroup), groupId)
                .create();

        var transaction = Transaction.create(
                accountId,
                Money.zero(),
                "Test",
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.now(),
                new TransactionHash("a".repeat(64)),
                auditInfo
        );
        transactionRepository.store(transaction);

        var hasTransactions = transactionQueryFacade.hasTransactions(accountId, groupId);

        assertThat(hasTransactions).isTrue();
    }

    @Test
    void shouldReturnFalseForDifferentAccountId() {
        var accountId1 = AccountId.generate();
        var accountId2 = AccountId.generate();
        var categoryId = CategoryId.generate();
        var groupId = GroupId.generate();
        var auditInfo = Instancio.of(AuditInfo.class)
                .set(field(AuditInfo::fromGroup), groupId)
                .create();

        var transaction = Transaction.create(
                accountId1,
                Money.zero(),
                "Test",
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.now(),
                new TransactionHash("a".repeat(64)),
                auditInfo
        );
        transactionRepository.store(transaction);

        var hasTransactions = transactionQueryFacade.hasTransactions(accountId2, groupId);

        assertThat(hasTransactions).isFalse();
    }

    @Test
    void shouldReturnFalseForDifferentGroupId() {
        var accountId = AccountId.generate();
        var categoryId = CategoryId.generate();
        var groupId1 = GroupId.generate();
        var groupId2 = GroupId.generate();
        var auditInfo = Instancio.of(AuditInfo.class)
                .set(field(AuditInfo::fromGroup), groupId1)
                .create();

        var transaction = Transaction.create(
                accountId,
                Money.zero(),
                "Test",
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.now(),
                new TransactionHash("a".repeat(64)),
                auditInfo
        );
        transactionRepository.store(transaction);

        var hasTransactionsForCategory = transactionQueryFacade.hasTransactions(categoryId, groupId2);
        var hasTransactionsForAccount = transactionQueryFacade.hasTransactions(accountId, groupId2);

        assertThat(hasTransactionsForCategory).isFalse();
        assertThat(hasTransactionsForAccount).isFalse();
    }
}
