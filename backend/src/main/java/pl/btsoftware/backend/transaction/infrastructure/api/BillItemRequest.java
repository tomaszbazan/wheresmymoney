package pl.btsoftware.backend.transaction.infrastructure.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.application.BillItemCommand;

public record BillItemRequest(
        @NotNull UUID categoryId,
        @NotNull BigDecimal amount,
        @Nullable String description) {
    public BillItemCommand toCommand() {
        return new BillItemCommand(CategoryId.of(categoryId), amount, description);
    }
}
