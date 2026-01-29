package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.BillItemDescriptionInvalidCharactersException;
import pl.btsoftware.backend.transaction.domain.error.BillItemDescriptionTooLongException;

class BillItemTest {

    @Test
    void shouldCreateBillItemWithAllFields() {
        // given
        var billItemId = BillItemId.generate();
        var categoryId = CategoryId.generate();
        var amount = Money.of(new BigDecimal("10.50"));
        var description = "Coca Cola 2L";

        // when
        var billItem = new BillItem(billItemId, categoryId, amount, description);

        // then
        assertThat(billItem.id()).isEqualTo(billItemId);
        assertThat(billItem.categoryId()).isEqualTo(categoryId);
        assertThat(billItem.amount()).isEqualTo(amount);
        assertThat(billItem.description()).isEqualTo(description);
    }

    @Test
    void shouldCreateBillItemWithNullDescription() {
        // given
        var billItemId = BillItemId.generate();
        var categoryId = CategoryId.generate();
        var amount = Money.of(new BigDecimal("5.00"));

        // when
        var billItem = new BillItem(billItemId, categoryId, amount, null);

        // then
        assertThat(billItem.id()).isEqualTo(billItemId);
        assertThat(billItem.categoryId()).isEqualTo(categoryId);
        assertThat(billItem.amount()).isEqualTo(amount);
        assertThat(billItem.description()).isNull();
    }

    @Test
    void shouldTrimDescription() {
        // given
        var description = "  Trimmed  ";

        // when
        var billItem = billItemWithDescription(description);

        // then
        assertThat(billItem.description()).isEqualTo("Trimmed");
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        // given
        var longDescription = "a".repeat(256);

        // when // then
        assertThatThrownBy(() -> billItemWithDescription(longDescription))
                .isInstanceOf(BillItemDescriptionTooLongException.class);
    }

    @Test
    void shouldFailWhenDescriptionContainsInvalidCharacters() {
        // given
        var description = "Invalid\u0000chars";

        // when // then
        assertThatThrownBy(() -> billItemWithDescription(description))
                .isInstanceOf(BillItemDescriptionInvalidCharactersException.class);
    }

    private BillItem billItemWithDescription(String description) {
        return new BillItem(
                BillItemId.generate(),
                CategoryId.generate(),
                Money.of(new BigDecimal("10.00")),
                description);
    }
}
