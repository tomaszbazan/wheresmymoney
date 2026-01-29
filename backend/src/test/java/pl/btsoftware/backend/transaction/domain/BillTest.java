package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.BillMustHaveAtLeastOneItemException;

class BillTest {

    @Test
    void shouldCreateBillWithSingleItem() {
        var billId = BillId.generate();
        var item =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(10.00)),
                        "Item 1");

        var bill = new Bill(billId, List.of(item));

        assertThat(bill.id()).isEqualTo(billId);
        assertThat(bill.items()).hasSize(1);
        assertThat(bill.items()).containsExactly(item);
    }

    @Test
    void shouldCreateBillWithMultipleItems() {
        var billId = BillId.generate();
        var item1 =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(10.00)),
                        "Item 1");
        var item2 =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(20.00)),
                        "Item 2");

        var bill = new Bill(billId, List.of(item1, item2));

        assertThat(bill.items()).hasSize(2);
        assertThat(bill.items()).containsExactly(item1, item2);
    }

    @Test
    void shouldFailWhenCreatingBillWithNoItems() {
        assertThatThrownBy(() -> new Bill(BillId.generate(), List.of()))
                .isInstanceOf(BillMustHaveAtLeastOneItemException.class);
    }

    @Test
    void shouldFailWhenCreatingBillWithNullItems() {
        assertThatThrownBy(() -> new Bill(BillId.generate(), null))
                .isInstanceOf(BillMustHaveAtLeastOneItemException.class);
    }

    @Test
    void shouldCalculateTotalAmount() {
        var item1 =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(10.50)),
                        "Item 1");
        var item2 =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(20.25)),
                        "Item 2");
        var bill = new Bill(BillId.generate(), List.of(item1, item2));

        var total = bill.totalAmount();

        assertThat(total).isEqualTo(Money.of(BigDecimal.valueOf(30.75)));
    }

    @Test
    void shouldReturnImmutableItemsList() {
        var item =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(10.00)),
                        "Item 1");
        var bill = new Bill(BillId.generate(), List.of(item));

        assertThatThrownBy(() -> bill.items().add(item))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
