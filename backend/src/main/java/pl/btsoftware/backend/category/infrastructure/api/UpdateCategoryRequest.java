package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.shared.CategoryId;

import java.util.UUID;

public record UpdateCategoryRequest(
        String name,
        String description,
        String color
) {
    public UpdateCategoryCommand toCommand(UUID categoryId) {
        return new UpdateCategoryCommand(
                CategoryId.of(categoryId),
                name,
                description,
                color
        );
    }
}