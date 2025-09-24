package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;

import java.util.UUID;

public record UpdateTransactionRequest(
        Money amount,
        String description,
        UUID categoryId
) {
    public UpdateTransactionCommand toCommand(TransactionId transactionId) {
        return new UpdateTransactionCommand(
                transactionId,
                amount,
                description,
                CategoryId.of(categoryId)
        );
    }
}
