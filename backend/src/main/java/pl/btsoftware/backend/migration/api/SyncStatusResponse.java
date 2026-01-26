package pl.btsoftware.backend.migration.api;

import java.time.LocalDateTime;

public record SyncStatusResponse(
        String entityType,
        LocalDateTime lastSyncAt,
        Integer lastSyncedCount,
        Integer totalSynced,
        String status,
        String errorMessage) {
}
