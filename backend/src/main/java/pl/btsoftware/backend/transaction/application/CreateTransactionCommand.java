package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.time.OffsetDateTime;

public record CreateTransactionCommand(
        AccountId accountId,
        Money amount,
        String description,
        OffsetDateTime date,
        TransactionType type,
        String category
) {
    public Transaction toDomain() {
        return Transaction.create(accountId, amount, description, date, type, category);
    }
}