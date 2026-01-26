package pl.btsoftware.backend.transaction.application;

import java.util.List;
import pl.btsoftware.backend.shared.TransactionId;

public record BulkCreateResult(
        int savedCount, int duplicateCount, List<TransactionId> savedTransactionIds) {
    public BulkCreateResult {
        savedTransactionIds = List.copyOf(savedTransactionIds);
    }

    public static BulkCreateResult of(List<TransactionId> savedIds, int duplicateCount) {
        return new BulkCreateResult(savedIds.size(), duplicateCount, savedIds);
    }
}
