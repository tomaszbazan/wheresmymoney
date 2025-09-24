package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.UUID;

public record CreateCategoryRequest(
        String name,
        CategoryType type,
        Color color,
        UUID parentId
) {
    public CreateCategoryCommand toCommand(UserId userId) {
        return new CreateCategoryCommand(
                name,
                type,
                color,
                userId,
                parentId != null ? CategoryId.of(parentId) : null
        );
    }
}
