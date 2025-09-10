package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.Color;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryView(
        UUID id,
        String name,
        String type,
        Color color,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CategoryView from(Category category) {
        return new CategoryView(
                category.id().value(),
                category.name(),
                category.type().name(),
                category.color(),
                category.createdAt(),
                category.lastUpdatedAt()
        );
    }
}