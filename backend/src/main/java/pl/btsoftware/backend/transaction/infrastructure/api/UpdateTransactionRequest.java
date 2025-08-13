package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;

public record UpdateTransactionRequest(
        Money amount,
        String description,
        String category
) {
    public UpdateTransactionCommand toCommand(TransactionId transactionId) {
        return new UpdateTransactionCommand(
                transactionId,
                amount,
                description,
                category
        );
    }
}