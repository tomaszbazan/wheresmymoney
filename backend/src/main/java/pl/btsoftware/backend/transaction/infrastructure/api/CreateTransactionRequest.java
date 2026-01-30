package pl.btsoftware.backend.transaction.infrastructure.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateTransactionRequest(
        @NotNull UUID accountId,
        @NotNull LocalDate transactionDate,
        @NotNull String type,
        @NotNull BillRequest bill) {

    public CreateTransactionCommand toCommand(UserId userId) {
        return toCommand(userId, AccountId.from(accountId));
    }

    public CreateTransactionCommand toCommand(UserId userId, AccountId accountId) {
        return new CreateTransactionCommand(
                accountId, transactionDate, TransactionType.valueOf(type.toUpperCase()), bill.toCommand(), userId);
    }
}
