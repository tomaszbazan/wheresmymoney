package pl.btsoftware.backend.category.application;

import pl.btsoftware.backend.shared.CategoryId;

public record UpdateCategoryCommand(
        CategoryId categoryId,
        String name,
        String description,
        String color
) {
}