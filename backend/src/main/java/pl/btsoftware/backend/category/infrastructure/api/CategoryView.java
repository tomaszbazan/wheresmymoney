package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.domain.Category;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryView(
        UUID id,
        String name,
        String description,
        String type,
        String color,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CategoryView from(Category category) {
        return new CategoryView(
                category.id().value(),
                category.name(),
                category.description(),
                category.type().name(),
                category.color(),
                category.createdAt(),
                category.lastUpdatedAt()
        );
    }
}