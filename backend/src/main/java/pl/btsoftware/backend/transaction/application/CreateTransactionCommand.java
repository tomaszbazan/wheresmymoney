package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

public record CreateTransactionCommand(
        AccountId accountId,
        Money amount,
        String description,
        OffsetDateTime date,
        TransactionType type,
        CategoryId categoryId,
        UserId userId
) {
    public Transaction toDomain(AuditInfo auditInfo) {
        return Transaction.create(accountId, amount, description, type, categoryId, auditInfo);
    }
}
