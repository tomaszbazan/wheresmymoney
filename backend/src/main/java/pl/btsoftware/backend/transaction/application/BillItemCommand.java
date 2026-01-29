package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.BillItem;
import pl.btsoftware.backend.transaction.domain.BillItemId;

public record BillItemCommand(CategoryId categoryId, Money amount, String description) {
    public BillItem toDomain() {
        return new BillItem(BillItemId.generate(), categoryId, amount, description);
    }
}
