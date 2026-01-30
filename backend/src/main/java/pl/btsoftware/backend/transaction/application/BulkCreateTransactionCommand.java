package pl.btsoftware.backend.transaction.application;

import java.util.List;
import pl.btsoftware.backend.shared.AccountId;

public record BulkCreateTransactionCommand(AccountId accountId, List<CreateTransactionCommand> transactions) {
    public BulkCreateTransactionCommand {
        transactions = List.copyOf(transactions);
    }
}
