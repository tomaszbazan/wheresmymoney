package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.shared.*;

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
            Money amount,
            String description,
            OffsetDateTime createdAt,
            TransactionType type,
            String category
    ) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                amount,
                type,
                description,
                category,
                createdAt,
                now(),
                Tombstone.active()
        );
    }

    public Transaction updateAmount(Money newAmount) {
        return new Transaction(id, accountId, newAmount, type, description, category, createdAt, now(UTC), tombstone);
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