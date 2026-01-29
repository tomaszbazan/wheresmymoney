package pl.btsoftware.backend.transaction.application;

import java.time.LocalDate;
import java.util.List;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.domain.Bill;
import pl.btsoftware.backend.transaction.domain.BillId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHashCalculator;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateTransactionCommand(
        AccountId accountId,
        LocalDate transactionDate,
        TransactionType type,
        BillCommand billCommand,
        UserId userId) {
    private static final TransactionHashCalculator HASH_CALCULATOR =
            new TransactionHashCalculator();

    public Transaction toDomain(AuditInfo auditInfo) {
        var bill = billCommand.toDomain();
        var amount = bill.totalAmount();
        var description = String.join(", ", billCommand.billItemsDescription());
        var hash =
                HASH_CALCULATOR.calculateHash(
                        accountId, amount, description, transactionDate, type);
        return Transaction.create(accountId, amount, type, bill, transactionDate, hash, auditInfo);
    }

    public record BillCommand(List<BillItemCommand> billItems) {
        public BillCommand {
            billItems = List.copyOf(billItems);
        }

        public Bill toDomain() {
            return new Bill(
                    BillId.generate(), billItems.stream().map(BillItemCommand::toDomain).toList());
        }

        public List<String> billItemsDescription() {
            return billItems.stream().map(BillItemCommand::description).toList();
        }
    }
}
