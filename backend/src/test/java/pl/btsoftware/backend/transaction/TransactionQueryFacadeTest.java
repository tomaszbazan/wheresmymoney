package pl.btsoftware.backend.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

import java.time.LocalDate;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Bill;
import pl.btsoftware.backend.transaction.domain.BillId;
import pl.btsoftware.backend.transaction.domain.BillItem;
import pl.btsoftware.backend.transaction.domain.BillItemId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
import pl.btsoftware.backend.transaction.infrastructure.persistance.InMemoryTransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

class TransactionQueryFacadeTest {

    private TransactionQueryFacade transactionQueryFacade;
    private InMemoryTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository = new InMemoryTransactionRepository();
        transactionQueryFacade = new TransactionQueryFacade(transactionRepository);
    }

    @Nested
    class HasTransactionsByCategory {

        @Test
        void shouldReturnFalseWhenNoTransactionsExistForCategory() {
            // given
            var categoryId = CategoryId.generate();
            var groupId = GroupId.generate();

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(categoryId, groupId);

            // then
            assertThat(hasTransactions).isFalse();
        }

        @Test
        void shouldReturnTrueWhenTransactionsExistForCategory() {
            // given
            var categoryId = CategoryId.generate();
            var accountId = AccountId.generate();
            var groupId = GroupId.generate();

            storeTransaction(accountId, categoryId, groupId);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(categoryId, groupId);

            // then
            assertThat(hasTransactions).isTrue();
        }

        @Test
        void shouldReturnFalseForDifferentCategoryId() {
            // given
            var categoryId1 = CategoryId.generate();
            var categoryId2 = CategoryId.generate();
            var accountId = AccountId.generate();
            var groupId = GroupId.generate();

            storeTransaction(accountId, categoryId1, groupId);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(categoryId2, groupId);

            // then
            assertThat(hasTransactions).isFalse();
        }

        @Test
        void shouldReturnFalseForDifferentGroupId() {
            // given
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();
            var groupId1 = GroupId.generate();
            var groupId2 = GroupId.generate();

            storeTransaction(accountId, categoryId, groupId1);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(categoryId, groupId2);

            // then
            assertThat(hasTransactions).isFalse();
        }
    }

    @Nested
    class HasTransactionsByAccount {

        @Test
        void shouldReturnFalseWhenNoTransactionsExistForAccount() {
            // given
            var accountId = AccountId.generate();
            var groupId = GroupId.generate();

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(accountId, groupId);

            // then
            assertThat(hasTransactions).isFalse();
        }

        @Test
        void shouldReturnTrueWhenTransactionsExistForAccount() {
            // given
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();
            var groupId = GroupId.generate();

            storeTransaction(accountId, categoryId, groupId);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(accountId, groupId);

            // then
            assertThat(hasTransactions).isTrue();
        }

        @Test
        void shouldReturnFalseForDifferentAccountId() {
            // given
            var accountId1 = AccountId.generate();
            var accountId2 = AccountId.generate();
            var categoryId = CategoryId.generate();
            var groupId = GroupId.generate();

            storeTransaction(accountId1, categoryId, groupId);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(accountId2, groupId);

            // then
            assertThat(hasTransactions).isFalse();
        }

        @Test
        void shouldReturnFalseForDifferentGroupId() {
            // given
            var accountId = AccountId.generate();
            var categoryId = CategoryId.generate();
            var groupId1 = GroupId.generate();
            var groupId2 = GroupId.generate();

            storeTransaction(accountId, categoryId, groupId1);

            // when
            var hasTransactions = transactionQueryFacade.hasTransactions(accountId, groupId2);

            // then
            assertThat(hasTransactions).isFalse();
        }
    }

    private void storeTransaction(AccountId accountId, CategoryId categoryId, GroupId groupId) {
        var bill = createBill(categoryId);
        var auditInfo = createAuditInfo(groupId);
        var transaction =
                Transaction.create(
                        accountId,
                        Money.zero(),
                        TransactionType.EXPENSE,
                        bill,
                        LocalDate.now(),
                        new TransactionHash("a".repeat(64)),
                        auditInfo);
        transactionRepository.store(transaction);
    }

    private Bill createBill(CategoryId categoryId) {
        var billItem = new BillItem(BillItemId.generate(), categoryId, Money.zero(), "Test");
        return new Bill(BillId.generate(), List.of(billItem));
    }

    private AuditInfo createAuditInfo(GroupId groupId) {
        return Instancio.of(AuditInfo.class).set(field(AuditInfo::fromGroup), groupId).create();
    }
}
