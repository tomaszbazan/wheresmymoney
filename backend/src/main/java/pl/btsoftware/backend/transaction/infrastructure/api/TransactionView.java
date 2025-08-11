package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionView(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String type,
        String description,
        String category,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TransactionView from(Transaction transaction) {
        return new TransactionView(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.amount().value(),
                transaction.type().name(),
                transaction.description(),
                transaction.category(),
                transaction.createdAt(),
                transaction.updatedAt()
        );
    }
}