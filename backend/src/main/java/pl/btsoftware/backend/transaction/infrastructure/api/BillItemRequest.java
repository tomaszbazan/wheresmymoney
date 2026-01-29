package pl.btsoftware.backend.transaction.infrastructure.api;

import java.util.UUID;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.application.BillItemCommand;

public record BillItemRequest(UUID categoryId, Money amount, String description) {
    public BillItemCommand toCommand() {
        return new BillItemCommand(CategoryId.of(categoryId), amount, description);
    }
}
