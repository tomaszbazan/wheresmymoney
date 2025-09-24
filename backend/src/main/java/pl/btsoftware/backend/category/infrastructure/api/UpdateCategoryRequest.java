package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Color;

import java.util.UUID;

public record UpdateCategoryRequest(
        String name,
        Color color,
        UUID parentId
) {
    public UpdateCategoryCommand toCommand(UUID categoryId) {
        return new UpdateCategoryCommand(
                CategoryId.of(categoryId),
                name,
                color,
                parentId != null ? CategoryId.of(parentId) : null
        );
    }
}
