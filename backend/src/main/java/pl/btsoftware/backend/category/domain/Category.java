package pl.btsoftware.backend.category.domain;

import lombok.With;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.error.CategoryNameTooLongException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

import static lombok.AccessLevel.PRIVATE;

public record Category(
        CategoryId id,
        @With(PRIVATE) String name,
        CategoryType type,
        @With(PRIVATE) Color color,
        AuditInfo createdInfo,
        @With(PRIVATE) AuditInfo updatedInfo,
        Tombstone tombstone
) {
    public Category {
        if (name != null && name.length() > 100) {
            throw new CategoryNameTooLongException();
        }
    }
    public static Category create(
            String name,
            CategoryType type,
            Color color,
            AuditInfo createdInfo
    ) {
        return new Category(
                CategoryId.generate(),
                name,
                type,
                color,
                createdInfo,
                createdInfo,
                Tombstone.active()
        );
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

    public Category delete() {
        return new Category(id, name, type, color, createdInfo, updatedInfo, Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }

    public Category updateWith(UpdateCategoryCommand command, UserId updatedBy) {
        var category = this;
        if (command.color() != null && !command.color().equals(this.color)) {
            category = category.withColor(command.color()).withUpdatedInfo(createUpdateInfo(updatedBy));
        }
        if (command.name() != null && !command.name().equals(this.name)) {
            category = category.withName(command.name()).withUpdatedInfo(createUpdateInfo(updatedBy));
        }
        return category;
    }

    private AuditInfo createUpdateInfo(UserId updatedBy) {
        return AuditInfo.create(updatedBy, createdInfo.fromGroup());
    }
}