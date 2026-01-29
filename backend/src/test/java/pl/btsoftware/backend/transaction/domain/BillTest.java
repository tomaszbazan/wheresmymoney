package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.BillItemsMustHaveSameCurrencyException;
import pl.btsoftware.backend.transaction.domain.error.BillMustHaveAtLeastOneItemException;

class BillTest {

    @Test
    void shouldCreateBillWithSingleItem() {
        // given
        var billId = BillId.generate();
        var item = billItem("10.00", "Item 1");

        // when
        var bill = new Bill(billId, List.of(item));

        // then
        assertThat(bill.id()).isEqualTo(billId);
        assertThat(bill.items()).hasSize(1);
        assertThat(bill.items()).containsExactly(item);
    }

    @Test
    void shouldCreateBillWithMultipleItems() {
        // given
        var billId = BillId.generate();
        var item1 = billItem("10.00", "Item 1");
        var item2 = billItem("20.00", "Item 2");
        var item3 = billItem("30.00", "Item 3");

        // when
        var bill = new Bill(billId, List.of(item1, item2, item3));

        // then
        assertThat(bill.items()).hasSize(3);
        assertThat(bill.items()).containsExactly(item1, item2, item3);
    }

    @Test
    void shouldFailWhenCreatingBillWithNoItems() {
        // given
        var billId = BillId.generate();
        var items = List.<BillItem>of();

        // when // then
        assertThatThrownBy(() -> new Bill(billId, items))
                .isInstanceOf(BillMustHaveAtLeastOneItemException.class);
    }

    @Test
    void shouldFailWhenCreatingBillWithNullItems() {
        // given
        var billId = BillId.generate();

        // when // then
        assertThatThrownBy(() -> new Bill(billId, null))
                .isInstanceOf(BillMustHaveAtLeastOneItemException.class);
    }

    @Test
    void shouldFailWhenCreatingBillWithMixedCurrencies() {
        // given
        var item1 = billItem("10.00", Currency.PLN, "Item 1");
        var item2 = billItem("20.00", Currency.EUR, "Item 2");

        // when // then
        assertThatThrownBy(() -> new Bill(BillId.generate(), List.of(item1, item2)))
                .isInstanceOf(BillItemsMustHaveSameCurrencyException.class);
    }

    @Test
    void shouldCalculateTotalAmount() {
        // given
        var item1 = billItem("10.50", "Item 1");
        var item2 = billItem("20.25", "Item 2");
        var bill = new Bill(BillId.generate(), List.of(item1, item2));

        // when
        var total = bill.totalAmount();

        // then
        assertThat(total).isEqualTo(Money.of(new BigDecimal("30.75")));
    }

    @Test
    void shouldReturnImmutableItemsList() {
        // given
        var item = billItem("10.00", "Item 1");
        var bill = new Bill(BillId.generate(), List.of(item));

        // when // then
        assertThatThrownBy(() -> bill.items().add(item))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReturnAllCategories() {
        // given
        var item1 = billItem("10.00", "Item 1");
        var item2 = billItem("20.00", "Item 2");
        var item3 = billItem("30.00", "Item 3");
        var bill = new Bill(BillId.generate(), List.of(item1, item2, item3));

        // when
        var categories = bill.categories();

        // then
        assertThat(categories)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        item1.categoryId(), item2.categoryId(), item3.categoryId());
    }

    private BillItem billItem(String amount, String description) {
        return new BillItem(
                BillItemId.generate(),
                CategoryId.generate(),
                Money.of(new BigDecimal(amount)),
                description);
    }

    private BillItem billItem(String amount, Currency currency, String description) {
        return new BillItem(
                BillItemId.generate(),
                CategoryId.generate(),
                Money.of(new BigDecimal(amount), currency),
                description);
    }
}
