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
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class TransactionTest {

    @Test
    void shouldCreateTransactionWithGeneratedId() {
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var description = "Test transaction";
        var categoryId = CategoryId.generate();
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        var billItem = new BillItem(BillItemId.generate(), categoryId, amount, description);
        var bill = new Bill(BillId.generate(), List.of(billItem));

        var transaction =
                Transaction.create(
                        accountId, amount, EXPENSE, bill, transactionDate, transactionHash, auditInfo);

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
    void shouldReturnDescriptionFromSingleBillItem() {
        var billItem =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(100), PLN),
                        "Test description");
        var bill = new Bill(BillId.generate(), List.of(billItem));
        var transaction = Instancio.of(Transaction.class).set(field(Transaction::bill), bill).create();

        assertThat(transaction.description()).isEqualTo("Test description");
    }

    @Test
    void shouldReturnNullDescriptionWhenBillItemHasNullDescription() {
        var billItem =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(100), PLN),
                        null);
        var bill = new Bill(BillId.generate(), List.of(billItem));
        var transaction = Instancio.of(Transaction.class).set(field(Transaction::bill), bill).create();

        assertThat(transaction.description()).isNull();
    }


    @Test
    void shouldReturnCreatedBy() {
        var userId = UserId.generate();
        var auditInfo = Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.createdBy()).isEqualTo(userId);
    }

    @Test
    void shouldReturnLastUpdatedBy() {
        var userId = UserId.generate();
        var auditInfo = Instancio.of(AuditInfo.class).set(field(AuditInfo::who), userId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::updatedInfo), auditInfo)
                        .create();

        assertThat(transaction.lastUpdatedBy()).isEqualTo(userId);
    }

    @Test
    void shouldReturnOwnedBy() {
        var groupId = GroupId.generate();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::fromGroup), groupId).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.ownedBy()).isEqualTo(groupId);
    }

    @Test
    void shouldReturnCreatedAt() {
        var timestamp = OffsetDateTime.now();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::createdInfo), auditInfo)
                        .create();

        assertThat(transaction.createdAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldReturnLastUpdatedAt() {
        var timestamp = OffsetDateTime.now();
        var auditInfo =
                Instancio.of(AuditInfo.class).set(field(AuditInfo::when), timestamp).create();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::updatedInfo), auditInfo)
                        .create();

        assertThat(transaction.lastUpdatedAt()).isEqualTo(timestamp);
    }

    @Test
    void shouldUpdateAmount() {
        var originalAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(200), PLN);
        var userId = UserId.generate();
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::amount), originalAmount)
                        .create();

        var updatedTransaction = transaction.updateAmount(newAmount, userId);

        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.amount()).isEqualTo(originalAmount);
    }

    @Test
    void shouldUpdateBill() {
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
                Instancio.of(Transaction.class).set(field(Transaction::bill), originalBill).create();

        var updatedTransaction = transaction.updateBill(newBill, userId);

        assertThat(updatedTransaction.bill()).isEqualTo(newBill);
        assertThat(updatedTransaction.description()).isEqualTo("Updated");
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.bill()).isEqualTo(originalBill);
        assertThat(transaction.description()).isEqualTo("Original");
    }

    @Test
    void shouldDeleteTransaction() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.active())
                        .create();

        var deletedTransaction = transaction.delete();

        assertThat(deletedTransaction.isDeleted()).isTrue();
        assertThat(deletedTransaction.tombstone().isDeleted()).isTrue();
        assertThat(deletedTransaction.tombstone().deletedAt()).isNotNull();
        assertThat(deletedTransaction.id()).isEqualTo(transaction.id());
        assertThat(transaction.isDeleted()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenTransactionIsDeleted() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.deleted())
                        .create();

        assertThat(transaction.isDeleted()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTransactionIsNotDeleted() {
        var transaction =
                Instancio.of(Transaction.class)
                        .set(field(Transaction::tombstone), Tombstone.active())
                        .create();

        assertThat(transaction.isDeleted()).isFalse();
    }
}
