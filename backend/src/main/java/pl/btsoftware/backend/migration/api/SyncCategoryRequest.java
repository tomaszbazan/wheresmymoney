package pl.btsoftware.backend.migration.api;

import java.time.LocalDateTime;

public record SyncCategoryRequest(
        Integer oldId,
        String categoryName,
        String type,
        String comment,
        String icon,
        Integer oldParentCategoryId,
        Integer oldGroupId,
        Integer oldUserId,
        LocalDateTime date) {
}
