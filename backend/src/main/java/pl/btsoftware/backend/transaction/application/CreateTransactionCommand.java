package pl.btsoftware.backend.transaction.application;

import java.time.LocalDate;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHashCalculator;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateTransactionCommand(
        AccountId accountId,
        LocalDate transactionDate,
        TransactionType type,
        BillCommand billCommand,
        UserId userId) {

    public Transaction toDomain(AuditInfo auditInfo) {
        var bill = billCommand.toDomain();
        var amount = bill.totalAmount();
        var description = String.join(", ", billCommand.billItemsDescription());
        var hash =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, description, transactionDate, type);
        return Transaction.create(accountId, type, bill, transactionDate, hash, auditInfo);
    }
}
