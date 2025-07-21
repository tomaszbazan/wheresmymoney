package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionType;

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