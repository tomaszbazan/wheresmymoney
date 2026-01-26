package pl.btsoftware.backend.migration.api;

import java.util.UUID;

public record SyncResultResponse(
        boolean success,
        UUID newId,
        String message,
        Integer oldId) {
}
