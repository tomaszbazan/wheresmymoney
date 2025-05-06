package pl.btsoftware.wheresmymoney.account.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateExpenseRequest(BigDecimal amount, String description, LocalDateTime date) {
}