package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateTransactionCommand(
        AccountId accountId,
        BigDecimal amount,
        String description,
        OffsetDateTime date,
        TransactionType type,
        String category,
        Currency currency
) {
    public Transaction toDomain() {
        return Transaction.create(accountId, amount, description, date, type, category, currency);
    }
}