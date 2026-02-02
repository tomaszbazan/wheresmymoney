package pl.btsoftware.backend.transaction.application;

import java.math.BigDecimal;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.BillItem;
import pl.btsoftware.backend.transaction.domain.BillItemId;

public record BillItemCommand(CategoryId categoryId, BigDecimal amount, String description) {
    public BillItem toDomain(pl.btsoftware.backend.shared.Currency currency) {
        return new BillItem(BillItemId.generate(), categoryId, Money.of(amount, currency), description);
    }
}
