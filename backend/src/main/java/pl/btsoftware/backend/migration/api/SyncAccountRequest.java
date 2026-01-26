package pl.btsoftware.backend.migration.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SyncAccountRequest(
        Integer oldId,
        String accountName,
        BigDecimal amount,
        Integer currencyId,
        Integer oldGroupId,
        Integer oldUserId,
        String comment,
        LocalDateTime date) {
}
