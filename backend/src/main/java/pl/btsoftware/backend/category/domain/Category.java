package pl.btsoftware.backend.category.domain;

import static lombok.AccessLevel.PRIVATE;

import java.time.OffsetDateTime;
import lombok.With;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.error.CategoryNameEmptyException;
import pl.btsoftware.backend.category.domain.error.CategoryNameInvalidCharactersException;
import pl.btsoftware.backend.category.domain.error.CategoryNameTooLongException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.shared.validation.NameValidationRules;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

public record Category(
        CategoryId id,
        @With(PRIVATE) String name,
        CategoryType type,
        @With(PRIVATE) Color color,
        @With(PRIVATE) CategoryId parentId,
        AuditInfo createdInfo,
        @With(PRIVATE) AuditInfo updatedInfo,
        Tombstone tombstone) {
    public Category(
            CategoryId id,
            String name,
            CategoryType type,
            Color color,
            CategoryId parentId,
            AuditInfo createdInfo,
            AuditInfo updatedInfo,
            Tombstone tombstone) {
        validateName(name);
        this.id = id;
        this.name = name.trim();
        this.type = type;
        this.color = color;
        this.parentId = parentId;
        this.createdInfo = createdInfo;
        this.updatedInfo = updatedInfo;
        this.tombstone = tombstone;
    }

    public static Category create(
            String name,
            CategoryType type,
            Color color,
            CategoryId parentId,
            AuditInfo createdInfo) {
        return new Category(
                CategoryId.generate(),
                name,
                type,
                color,
                parentId,
                createdInfo,
                createdInfo,
                Tombstone.active());
    }

    public static Category create(
            String name, CategoryType type, Color color, AuditInfo createdInfo) {
        return create(name, type, color, null, createdInfo);
    }

    public UserId createdBy() {
        return createdInfo.who();
    }

    public UserId lastUpdatedBy() {
        return updatedInfo.who();
    }

    public GroupId ownedBy() {
        return createdInfo.fromGroup();
    }

    public OffsetDateTime createdAt() {
        return createdInfo.when();
    }

    public OffsetDateTime lastUpdatedAt() {
        return updatedInfo.when();
    }

    private static void validateName(String name) {
        NameValidationRules.validate(
                name,
                CategoryNameEmptyException::new,
                CategoryNameTooLongException::new,
                CategoryNameInvalidCharactersException::new);
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }

    public Category delete() {
        return new Category(
                id, name, type, color, parentId, createdInfo, updatedInfo, Tombstone.deleted());
    }

    private AuditInfo createUpdateInfo(UserId updatedBy) {
        return AuditInfo.create(updatedBy, createdInfo.fromGroup());
    }

    public Category updateWith(UpdateCategoryCommand command, UserId updatedBy) {
        var category = this;
        if (command.color() != null && !command.color().equals(this.color)) {
            category =
                    category.withColor(command.color())
                            .withUpdatedInfo(createUpdateInfo(updatedBy));
        }
        if (command.name() != null && !command.name().equals(this.name)) {
            category =
                    category.withName(command.name()).withUpdatedInfo(createUpdateInfo(updatedBy));
        }
        if (command.parentId() != this.parentId) {
            category =
                    category.withParentId(command.parentId())
                            .withUpdatedInfo(createUpdateInfo(updatedBy));
        }
        return category;
    }
}
