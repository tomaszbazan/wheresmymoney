package pl.btsoftware.backend.account.infrastructure.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateExpenseRequest(BigDecimal amount, String description, OffsetDateTime date) {
}
