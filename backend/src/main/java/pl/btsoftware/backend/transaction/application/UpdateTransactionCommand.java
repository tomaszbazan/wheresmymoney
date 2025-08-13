package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;

public record UpdateTransactionCommand(
        TransactionId transactionId,
        Money amount,
        String description,
        String category
) {
}