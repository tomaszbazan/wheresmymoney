package pl.btsoftware.backend.transaction.application;

import java.time.LocalDate;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHashCalculator;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateTransactionCommand(
        AccountId accountId,
        Money amount,
        String description,
        LocalDate transactionDate,
        TransactionType type,
        CategoryId categoryId,
        UserId userId) {
    private static final TransactionHashCalculator HASH_CALCULATOR =
            new TransactionHashCalculator();

    public Transaction toDomain(AuditInfo auditInfo) {
        var hash =
                HASH_CALCULATOR.calculateHash(
                        accountId, amount, description, transactionDate, type);
        return Transaction.create(
                accountId, amount, description, type, categoryId, transactionDate, hash, auditInfo);
    }
}
