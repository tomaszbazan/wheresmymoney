package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Money;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transaction(
        TransactionId id,
        AccountId accountId,
        Money amount,
        TransactionType type,
        String description,
        String category,
        OffsetDateTime when,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static Transaction create(
            AccountId accountId,
            BigDecimal amount,
            String description,
            OffsetDateTime date,
            TransactionType type,
            String category,
            String currency
    ) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                Money.of(amount, currency),
                type,
                description,
                category,
                date,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}