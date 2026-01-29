package pl.btsoftware.backend.transaction.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.error.BillItemsMustHaveSameCurrencyException;
import pl.btsoftware.backend.transaction.domain.error.BillMustHaveAtLeastOneItemException;

public record Bill(BillId id, List<BillItem> items) {
    public Bill(BillId id, List<BillItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BillMustHaveAtLeastOneItemException();
        }
        var firstCurrency = items.getFirst().amount().currency();
        if (items.stream().anyMatch(item -> !item.amount().currency().equals(firstCurrency))) {
            throw new BillItemsMustHaveSameCurrencyException();
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

    public Set<CategoryId> categories() {
        return items.stream().map(BillItem::categoryId).collect(Collectors.toSet());
    }
}
