package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.shared.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

public record Transaction(
        TransactionId id,
        AccountId accountId,
        Money amount,
        TransactionType type,
        String description,
        String category,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Tombstone tombstone
) {
    public static Transaction create(
            AccountId accountId,
            BigDecimal amount,
            String description,
            OffsetDateTime createdAt,
            TransactionType type,
            String category,
            Currency currency
    ) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                Money.of(amount, currency),
                type,
                description,
                category,
                createdAt,
                now(),
                Tombstone.active()
        );
    }

    public Transaction updateAmount(BigDecimal newAmount) {
        return new Transaction(id, accountId, Money.of(newAmount, amount.currency()), type, description, category, createdAt, now(UTC), tombstone);
    }

    public Transaction updateDescription(String newDescription) {
        return new Transaction(id, accountId, amount, type, newDescription, category, createdAt, now(UTC), tombstone);
    }

    public Transaction updateCategory(String newCategory) {
        return new Transaction(id, accountId, amount, type, description, newCategory, createdAt, now(UTC), tombstone);
    }

    public Transaction delete() {
        return new Transaction(id, accountId, amount, type, description, category, createdAt, updatedAt, Tombstone.deleted());
    }
}