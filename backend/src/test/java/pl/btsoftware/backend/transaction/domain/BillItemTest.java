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
        var billItemId = BillItemId.generate();
        var categoryId = CategoryId.generate();
        var amount = Money.of(BigDecimal.valueOf(10.50));
        var description = "Coca Cola 2L";

        var billItem = new BillItem(billItemId, categoryId, amount, description);

        assertThat(billItem.id()).isEqualTo(billItemId);
        assertThat(billItem.categoryId()).isEqualTo(categoryId);
        assertThat(billItem.amount()).isEqualTo(amount);
        assertThat(billItem.description()).isEqualTo(description);
    }

    @Test
    void shouldCreateBillItemWithNullDescription() {
        var billItemId = BillItemId.generate();
        var categoryId = CategoryId.generate();
        var amount = Money.of(BigDecimal.valueOf(5.00));

        var billItem = new BillItem(billItemId, categoryId, amount, null);

        assertThat(billItem.id()).isEqualTo(billItemId);
        assertThat(billItem.categoryId()).isEqualTo(categoryId);
        assertThat(billItem.amount()).isEqualTo(amount);
        assertThat(billItem.description()).isNull();
    }

    @Test
    void shouldTrimDescription() {
        var billItem =
                new BillItem(
                        BillItemId.generate(),
                        CategoryId.generate(),
                        Money.of(BigDecimal.valueOf(10.00)),
                        "  Trimmed  ");

        assertThat(billItem.description()).isEqualTo("Trimmed");
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        var longDescription = "a".repeat(256);

        assertThatThrownBy(
                        () ->
                                new BillItem(
                                        BillItemId.generate(),
                                        CategoryId.generate(),
                                        Money.of(BigDecimal.valueOf(10.00)),
                                        longDescription))
                .isInstanceOf(BillItemDescriptionTooLongException.class);
    }

    @Test
    void shouldFailWhenDescriptionContainsInvalidCharacters() {
        assertThatThrownBy(
                        () ->
                                new BillItem(
                                        BillItemId.generate(),
                                        CategoryId.generate(),
                                        Money.of(BigDecimal.valueOf(10.00)),
                                        "Invalid\u0000chars"))
                .isInstanceOf(BillItemDescriptionInvalidCharactersException.class);
    }
}
