package pl.btsoftware.backend.category.infrastructure.api;

import java.util.UUID;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateCategoryRequest(String name, CategoryType type, Color color, UUID parentId) {
    public CreateCategoryCommand toCommand(UserId userId) {
        return new CreateCategoryCommand(
                name, type, color, userId, parentId != null ? CategoryId.of(parentId) : null);
    }
}
