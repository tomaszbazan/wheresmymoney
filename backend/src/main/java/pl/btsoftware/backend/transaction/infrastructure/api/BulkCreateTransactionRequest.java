package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.transaction.application.BulkCreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;
import java.util.UUID;

public record BulkCreateTransactionRequest(UUID accountId, List<CreateTransactionRequest> transactions) {
    public BulkCreateTransactionRequest {
        transactions = List.copyOf(transactions);
    }

    public BulkCreateTransactionCommand toCommands(UserId userId) {
        return new BulkCreateTransactionCommand(AccountId.from(accountId), transactions.stream().map(req -> req.toCommand(userId)).toList());
    }
}
