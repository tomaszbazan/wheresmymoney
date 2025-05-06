package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateExpenseRequest(UUID accountId, BigDecimal amount, String description, LocalDateTime date) {
}