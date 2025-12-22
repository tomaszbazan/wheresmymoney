package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID accountId,
        Money amount,
        String description,
        LocalDate transactionDate,
        String type,
        UUID categoryId
) {
    public CreateTransactionCommand toCommand(UserId userId) {
        return new CreateTransactionCommand(
                AccountId.from(accountId),
                amount,
                description,
                transactionDate,
                TransactionType.valueOf(type.toUpperCase()),
                new CategoryId(categoryId),
                userId
        );
    }
}
