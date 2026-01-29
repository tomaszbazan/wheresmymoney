package pl.btsoftware.backend.transaction.infrastructure.api;

import java.time.LocalDate;
import java.util.UUID;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateTransactionRequest(
        UUID accountId, LocalDate transactionDate, String type, BillRequest bill) {

    public CreateTransactionCommand toCommand(UserId userId) {
        return toCommand(userId, AccountId.from(accountId));
    }

    public CreateTransactionCommand toCommand(UserId userId, AccountId accountId) {
        return new CreateTransactionCommand(
                accountId,
                transactionDate,
                TransactionType.valueOf(type.toUpperCase()),
                bill.toCommand(),
                userId);
    }
}
