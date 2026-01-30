package pl.btsoftware.backend.transaction.infrastructure.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;

public record UpdateTransactionRequest(
        @NotNull BillRequest bill, @NotNull String accountId, @NotNull LocalDate transactionDate) {

    public UpdateTransactionCommand toCommand(TransactionId transactionId) {
        return new UpdateTransactionCommand(
                transactionId,
                bill.toCommand(),
                new AccountId(UUID.fromString(accountId)),
                transactionDate);
    }
}
