package pl.btsoftware.backend.migration.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SyncTransactionRequest(
        Integer oldId,
        Integer oldAccountId,
        Integer oldCategoryId,
        Integer oldUserId,
        Integer oldGroupId,
        BigDecimal amount,
        LocalDateTime date,
        String comment,
        String type,
        Integer currencyId) {
}
