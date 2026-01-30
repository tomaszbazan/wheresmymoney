package pl.btsoftware.backend.transaction.application;

import java.time.LocalDate;
import org.jetbrains.annotations.NotNull;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;

public record UpdateTransactionCommand(
        @NotNull TransactionId transactionId,
        @NotNull BillCommand bill,
        @NotNull AccountId accountId,
        @NotNull LocalDate transactionDate) {}
