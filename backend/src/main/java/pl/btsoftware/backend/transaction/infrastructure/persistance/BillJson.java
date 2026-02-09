package pl.btsoftware.backend.transaction.infrastructure.persistance;

import static java.math.BigDecimal.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.Bill;
import pl.btsoftware.backend.transaction.domain.BillId;
import pl.btsoftware.backend.transaction.domain.BillItem;
import pl.btsoftware.backend.transaction.domain.BillItemId;

public record BillJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("items") List<BillItemJson> items,
        @JsonProperty("totalAmount") BigDecimal totalAmount) {
    public BillJson {
        items = List.copyOf(items);
    }

    public static BillJson fromDomain(Bill bill) {
        var itemsJson = bill.items().stream().map(BillItemJson::fromDomain).toList();
        var total = bill.items().stream().map(item -> item.amount().value()).reduce(ZERO, BigDecimal::add);

        return new BillJson(bill.id().value(), itemsJson, total);
    }

    public Bill toDomain() {
        var billItems = items.stream().map(BillItemJson::toDomain).toList();
        return new Bill(BillId.of(id), billItems);
    }

    record BillItemJson(
            @JsonProperty("id") UUID id,
            @JsonProperty("categoryId") UUID categoryId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("description") String description) {

        public static BillItemJson fromDomain(BillItem item) {
            return new BillItemJson(
                    item.id().value(),
                    item.categoryId().value(),
                    item.amount().value(),
                    item.amount().currency().name(),
                    item.description());
        }

        public BillItem toDomain() {
            return new BillItem(
                    BillItemId.of(id),
                    CategoryId.of(categoryId),
                    Money.of(amount, Currency.valueOf(currency)),
                    description);
        }
    }
}
