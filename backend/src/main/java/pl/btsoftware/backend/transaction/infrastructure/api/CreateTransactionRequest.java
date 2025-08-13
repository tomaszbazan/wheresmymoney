package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID accountId,
        Money amount,
        String description,
        OffsetDateTime date,
        String type,
        String category
) {
    public CreateTransactionCommand toCommand() {
        return new CreateTransactionCommand(
                AccountId.from(accountId),
                amount,
                description,
                date,
                TransactionType.valueOf(type.toUpperCase()),
                category
        );
    }
}