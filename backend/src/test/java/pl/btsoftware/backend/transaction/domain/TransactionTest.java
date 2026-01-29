package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class TransactionTest {

    @Nested
    @DisplayName("Creation Tests")
    class Creation {
        @Test
        void shouldCreateTransaction() {
            // given
            var accountId = AccountId.generate();
            var amount = Money.of(BigDecimal.valueOf(100), PLN);
            var description = "Test transaction";
            var categoryId = CategoryId.generate();
            var transactionDate = LocalDate.now();
            var transactionHash = new TransactionHash("a".repeat(64));
            var auditInfo = Instancio.create(AuditInfo.class);

            var billItem = new BillItem(BillItemId.generate(), categoryId, amount, description);
            var bill = new Bill(BillId.generate(), List.of(billItem));

            // when
            var transaction =
                    Transaction.create(
                            accountId, EXPENSE, bill, transactionDate, transactionHash, auditInfo);

            // then
            assertThat(transaction.id()).isNotNull();
            assertThat(transaction.accountId()).isEqualTo(accountId);
            assertThat(transaction.amount()).isEqualTo(amount);
            assertThat(transaction.description()).isEqualTo(description);
            assertThat(transaction.type()).isEqualTo(EXPENSE);
            assertThat(transaction.bill()).isEqualTo(bill);
            assertThat(transaction.transactionDate()).isEqualTo(transactionDate);
            assertThat(transaction.transactionHash()).isEqualTo(transactionHash);
            assertThat(transaction.createdInfo()).isEqualTo(auditInfo);
            assertThat(transaction.updatedInfo()).isEqualTo(auditInfo);
            assertThat(transaction.tombstone().isDeleted()).isFalse();
            assertThat(transaction.isDeleted()).isFalse();
        }

        @Test
        void shouldCreateTransactionWithMultipleItemsBill() {
            // given
            var accountId = AccountId.generate();
            var transactionDate = LocalDate.now();
            var transactionHash = new TransactionHash("a".repeat(64));
            var auditInfo = Instancio.create(AuditInfo.class);

            var item1 =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(30), PLN),
                            "Clothes");
            var item2 =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(20), PLN),
                            "Drinks");
            var bill = new Bill(BillId.generate(), List.of(item1, item2));

            // when
            var transaction =
                    Transaction.create(
                            accountId, EXPENSE, bill, transactionDate, transactionHash, auditInfo);

            // then
            assertThat(transaction.bill().items()).hasSize(2);
            assertThat(transaction.bill().items()).containsExactly(item1, item2);
        }
    }

    @Nested
    @DisplayName("Description Logic Tests")
    class DescriptionLogic {
        @Test
        void shouldReturnDescriptionFromSingleBillItem() {
            // given
            var billItem =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(100), PLN),
                            "Test description");
            var bill = new Bill(BillId.generate(), List.of(billItem));

            // when
            var transaction =
                    Instancio.of(Transaction.class).set(field(Transaction::bill), bill).create();

            // then
            assertThat(transaction.description()).isEqualTo("Test description");
        }

        @Test
        void shouldReturnNullDescriptionWhenBillItemHasNullDescription() {
            // given
            var billItem =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(100), PLN),
                            null);
            var bill = new Bill(BillId.generate(), List.of(billItem));

            // when
            var transaction =
                    Instancio.of(Transaction.class).set(field(Transaction::bill), bill).create();

            // then
            assertThat(transaction.description()).isNull();
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class Metadata {
        @Test
        void shouldReturnCreatedBy() {
            // given
            var userId = UserId.generate();
            var auditInfo =
                    Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();

            // when
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::createdInfo), auditInfo)
                            .create();

            // then
            assertThat(transaction.createdBy()).isEqualTo(userId);
        }

        @Test
        void shouldReturnLastUpdatedBy() {
            // given
            var userId = UserId.generate();
            var auditInfo =
                    Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();

            // when
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::updatedInfo), auditInfo)
                            .create();

            // then
            assertThat(transaction.lastUpdatedBy()).isEqualTo(userId);
        }

        @Test
        void shouldReturnOwnedBy() {
            // given
            var groupId = GroupId.generate();
            var auditInfo =
                    Instancio.of(AuditInfo.class)
                            .set(field(AuditInfo::fromGroup), groupId)
                            .create();

            // when
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::createdInfo), auditInfo)
                            .create();

            // then
            assertThat(transaction.ownedBy()).isEqualTo(groupId);
        }

        @Test
        void shouldReturnCreatedAt() {
            // given
            var timestamp = OffsetDateTime.now();
            var auditInfo =
                    Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();

            // when
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::createdInfo), auditInfo)
                            .create();

            // then
            assertThat(transaction.createdAt()).isEqualTo(timestamp);
        }

        @Test
        void shouldReturnLastUpdatedAt() {
            // given
            var timestamp = OffsetDateTime.now();
            var auditInfo =
                    Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();

            // when
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::updatedInfo), auditInfo)
                            .create();

            // then
            assertThat(transaction.lastUpdatedAt()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class Update {
        @Test
        void shouldUpdateBill() {
            // given
            var originalBillItem =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(100), PLN),
                            "Original");
            var originalBill = new Bill(BillId.generate(), List.of(originalBillItem));

            var newBillItem =
                    new BillItem(
                            BillItemId.generate(),
                            CategoryId.generate(),
                            Money.of(BigDecimal.valueOf(200), PLN),
                            "Updated");
            var newBill = new Bill(BillId.generate(), List.of(newBillItem));

            var userId = UserId.generate();
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::bill), originalBill)
                            .create();

            // when
            var updatedTransaction = transaction.updateBill(newBill, userId);

            // then
            assertThat(updatedTransaction.bill()).isEqualTo(newBill);
            assertThat(updatedTransaction.description()).isEqualTo("Updated");
            assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
            assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
            assertThat(transaction.bill()).isEqualTo(originalBill);
            assertThat(transaction.description()).isEqualTo("Original");
        }
    }

    @Nested
    @DisplayName("Deletion Information Tests")
    class DeletionInfo {
        @Test
        void shouldReturnTrueWhenTransactionIsDeleted() {
            // given
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::tombstone), Tombstone.deleted())
                            .create();

            // when
            var isDeleted = transaction.isDeleted();

            // then
            assertThat(isDeleted).isTrue();
        }

        @Test
        void shouldReturnFalseWhenTransactionIsNotDeleted() {
            // given
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::tombstone), Tombstone.active())
                            .create();

            // when
            var isDeleted = transaction.isDeleted();

            // then
            assertThat(isDeleted).isFalse();
        }
    }

    @Nested
    @DisplayName("Action Tests")
    class Actions {
        @Test
        void shouldDeleteTransaction() {
            // given
            var transaction =
                    Instancio.of(Transaction.class)
                            .set(field(Transaction::tombstone), Tombstone.active())
                            .create();

            // when
            var deletedTransaction = transaction.delete();

            // then
            assertThat(deletedTransaction.isDeleted()).isTrue();
            assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();
            assertThat(deletedTransaction.tombstone().deletedAt()).isNotNull();
            assertThat(deletedTransaction.id()).isEqualTo(transaction.id());
            assertThat(deletedTransaction.bill()).isEqualTo(transaction.bill());
            assertThat(transaction.isDeleted()).isFalse();
        }
    }
}
