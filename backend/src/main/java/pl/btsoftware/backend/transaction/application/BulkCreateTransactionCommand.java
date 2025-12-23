package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.AccountId;

import java.util.List;

public record BulkCreateTransactionCommand(AccountId accountId, List<CreateTransactionCommand> transactions) {
    public BulkCreateTransactionCommand {
        transactions = List.copyOf(transactions);
    }
}
