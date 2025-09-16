package pl.btsoftware.backend.category.application;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Color;

public record UpdateCategoryCommand(
        CategoryId categoryId,
        String name,
        Color color,
        CategoryId parentId
) {
}