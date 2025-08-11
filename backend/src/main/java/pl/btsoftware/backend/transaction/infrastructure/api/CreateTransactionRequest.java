package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID accountId,
        BigDecimal amount,
        String description,
        OffsetDateTime date,
        String type,
        String category,
        String currency
) {
    public CreateTransactionCommand toCommand() {
        return new CreateTransactionCommand(
                AccountId.from(accountId),
                amount,
                description,
                date,
                TransactionType.valueOf(type.toUpperCase()),
                category,
                Currency.valueOf(currency.toUpperCase())
        );
    }
}