package pl.btsoftware.backend.transaction.application;

import pl.btsoftware.backend.shared.TransactionId;

import java.util.List;

public record BulkCreateResult(
        int savedCount,
        int duplicateCount,
        List<TransactionId> savedTransactionIds
) {
    public BulkCreateResult {
        savedTransactionIds = List.copyOf(savedTransactionIds);
    }

    public static BulkCreateResult of(List<TransactionId> savedIds, int duplicateCount) {
        return new BulkCreateResult(savedIds.size(), duplicateCount, savedIds);
    }
}
