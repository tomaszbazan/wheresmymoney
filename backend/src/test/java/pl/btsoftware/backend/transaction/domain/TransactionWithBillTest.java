package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.Currency.PLN;
import static pl.btsoftware.backend.shared.TransactionType.EXPENSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.users.domain.UserId;

class TransactionWithBillTest {

    @Test
    void shouldCreateTransactionWithSingleItemBill() {
        var accountId = AccountId.generate();
        var amount = Money.of(BigDecimal.valueOf(100), PLN);
        var categoryId = CategoryId.generate();
        var description = "Groceries";
        var transactionDate = LocalDate.now();
        var transactionHash = new TransactionHash("a".repeat(64));
        var auditInfo = Instancio.create(AuditInfo.class);

        var billItem = new BillItem(BillItemId.generate(), categoryId, amount, description);
        var bill = new Bill(BillId.generate(), List.of(billItem));

        var transaction =
                Transaction.create(
                        accountId,
                        amount,
                        EXPENSE,
                        bill,
                        transactionDate,
                        transactionHash,
                        auditInfo);

        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.accountId()).isEqualTo(accountId);
        assertThat(transaction.amount()).isEqualTo(amount);
        assertThat(transaction.type()).isEqualTo(EXPENSE);
        assertThat(transaction.bill()).isEqualTo(bill);
        assertThat(transaction.bill().items()).hasSize(1);
        assertThat(transaction.transactionDate()).isEqualTo(transactionDate);
        assertThat(transaction.transactionHash()).isEqualTo(transactionHash);
        assertThat(transaction.createdInfo()).isEqualTo(auditInfo);
        assertThat(transaction.updatedInfo()).isEqualTo(auditInfo);
        assertThat(transaction.tombstone().isDeleted()).isFalse();
        assertThat(transaction.isDeleted()).isFalse();
    }

    @Test
    void shouldCreateTransactionWithMultipleItemsBill() {
        var accountId = AccountId.generate();
        var totalAmount = Money.of(BigDecimal.valueOf(50), PLN);
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

        var transaction =
                Transaction.create(
                        accountId,
                        totalAmount,
                        EXPENSE,
                        bill,
                        transactionDate,
                        transactionHash,
                        auditInfo);

        assertThat(transaction.bill().items()).hasSize(2);
        assertThat(transaction.bill().items()).containsExactly(item1, item2);
    }

    @Test
    void shouldUpdateAmountAndPreserveBill() {
        var originalAmount = Money.of(BigDecimal.valueOf(100), PLN);
        var newAmount = Money.of(BigDecimal.valueOf(200), PLN);
        var userId = UserId.generate();
        var billItem =
                new BillItem(BillItemId.generate(), CategoryId.generate(), originalAmount, "Test");
        var bill = new Bill(BillId.generate(), List.of(billItem));
        var transaction =
                Transaction.create(
                        AccountId.generate(),
                        originalAmount,
                        EXPENSE,
                        bill,
                        LocalDate.now(),
                        new TransactionHash("a".repeat(64)),
                        Instancio.create(AuditInfo.class));

        var updatedTransaction = transaction.updateAmount(newAmount, userId);

        assertThat(updatedTransaction.amount()).isEqualTo(newAmount);
        assertThat(updatedTransaction.bill()).isEqualTo(bill);
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
        var transaction =
                Transaction.create(
                        AccountId.generate(),
                        Money.of(BigDecimal.valueOf(100), PLN),
                        EXPENSE,
                        originalBill,
                        LocalDate.now(),
                        new TransactionHash("a".repeat(64)),
                        Instancio.create(AuditInfo.class));

        var newBillItem =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(150), PLN),
                        "Updated");
        var newBill = new Bill(BillId.generate(), List.of(newBillItem));
        var userId = UserId.generate();

        var updatedTransaction = transaction.updateBill(newBill, userId);

        assertThat(updatedTransaction.bill()).isEqualTo(newBill);
        assertThat(updatedTransaction.bill().items()).hasSize(1);
        assertThat(updatedTransaction.bill().items().getFirst().description()).isEqualTo("Updated");
        assertThat(updatedTransaction.id()).isEqualTo(transaction.id());
        assertThat(updatedTransaction.lastUpdatedBy()).isEqualTo(userId);
        assertThat(transaction.bill()).isEqualTo(originalBill);
    }

    @Test
    void shouldDeleteTransactionAndPreserveBill() {
        var billItem =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(100), PLN),
                        "Test");
        var bill = new Bill(BillId.generate(), List.of(billItem));
        var transaction =
                Transaction.create(
                        AccountId.generate(),
                        Money.of(BigDecimal.valueOf(100), PLN),
                        EXPENSE,
                        bill,
                        LocalDate.now(),
                        new TransactionHash("a".repeat(64)),
                        Instancio.create(AuditInfo.class));

        var deletedTransaction = transaction.delete();

        assertThat(deletedTransaction.isDeleted()).isTrue();
        assertThat(deletedTransaction.bill()).isEqualTo(bill);
        assertThat(deletedTransaction.id()).isEqualTo(transaction.id());
        assertThat(transaction.isDeleted()).isFalse();
    }
}
