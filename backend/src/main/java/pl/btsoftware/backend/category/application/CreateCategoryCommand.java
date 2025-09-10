package pl.btsoftware.backend.category.application;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.users.domain.UserId;

public record CreateCategoryCommand(
        String name,
        CategoryType type,
        Color color,
        UserId userId
) {
    public Category toDomain(AuditInfo auditInfo) {
        return Category.create(name, type, color, auditInfo);
    }
}