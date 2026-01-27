package pl.btsoftware.backend.transaction.infrastructure.api;

import java.util.List;
import java.util.UUID;
import pl.btsoftware.backend.transaction.application.BulkCreateResult;

public record BulkCreateTransactionResponse(
        int savedCount, int duplicateCount, List<UUID> savedTransactionIds) {
    public BulkCreateTransactionResponse {
        savedTransactionIds = List.copyOf(savedTransactionIds);
    }

    public static BulkCreateTransactionResponse from(BulkCreateResult result) {
        var ids = result.savedTransactionIds().stream().map(id -> id.value()).toList();
        return new BulkCreateTransactionResponse(result.savedCount(), result.duplicateCount(), ids);
    }
}
