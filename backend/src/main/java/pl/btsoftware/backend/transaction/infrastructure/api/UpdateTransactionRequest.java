package pl.btsoftware.backend.transaction.infrastructure.api;

import java.math.BigDecimal;

public record UpdateTransactionRequest(
        BigDecimal amount,
        String description,
        String category
) {
}