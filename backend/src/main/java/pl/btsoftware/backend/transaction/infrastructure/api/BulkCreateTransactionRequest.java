package pl.btsoftware.backend.transaction.infrastructure.api;

import java.util.List;
import java.util.UUID;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.transaction.application.BulkCreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

public record BulkCreateTransactionRequest(
        UUID accountId, List<CreateTransactionRequest> transactions) {
    public BulkCreateTransactionRequest {
        transactions = List.copyOf(transactions);
    }

    public BulkCreateTransactionCommand toCommands(UserId userId) {
        var accountId = AccountId.from(this.accountId);
        return new BulkCreateTransactionCommand(
                accountId,
                transactions.stream().map(req -> req.toCommand(userId, accountId)).toList());
    }
}
