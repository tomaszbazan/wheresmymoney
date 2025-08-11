package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.transaction.domain.TransactionId;

import java.math.BigDecimal;

public record UpdateTransactionCommand(
        TransactionId transactionId,
        BigDecimal amount,
        String description,
        String category
) {
}