package pl.btsoftware.backend.transfer.application;

import jakarta.validation.constraints.NotNull;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record CreateTransferCommand(
        @NotNull AccountId sourceAccountId,
        @NotNull AccountId targetAccountId,
        @NotNull BigDecimal sourceAmount,
        @NotNull BigDecimal targetAmount,
        String description,
        @NotNull UserId userId
) {
    public CreateTransferCommand {
        requireNonNull(sourceAccountId, "Source account id cannot be null");
        requireNonNull(targetAccountId, "Target account id cannot be null");
        requireNonNull(sourceAmount, "Source amount cannot be null");
        requireNonNull(targetAmount, "Target amount cannot be null");
        requireNonNull(userId, "User id cannot be null");
    }
}
