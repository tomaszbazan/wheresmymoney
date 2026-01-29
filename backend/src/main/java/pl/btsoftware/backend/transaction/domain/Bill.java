package pl.btsoftware.backend.transaction.domain;

import java.util.List;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.BillMustHaveAtLeastOneItemException;

public record Bill(BillId id, List<BillItem> items) {
    public Bill(BillId id, List<BillItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BillMustHaveAtLeastOneItemException();
        }
        this.id = id;
        this.items = List.copyOf(items);
    }

    public Money totalAmount() {
        return items.stream()
                .map(BillItem::amount)
                .reduce(Money::add)
                .orElseThrow(BillMustHaveAtLeastOneItemException::new);
    }
}
