package pl.btsoftware.backend.category.domain;

import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

public record Category(
        CategoryId id,
        String name,
        String description,
        CategoryType type,
        String color,
        AuditInfo createdInfo,
        AuditInfo updatedInfo,
        Tombstone tombstone
) {
    public static Category create(
            String name,
            String description,
            CategoryType type,
            String color,
            AuditInfo createdInfo
    ) {
        return new Category(
                CategoryId.generate(),
                name,
                description,
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

    public Category updateName(String newName, UserId updatedBy) {
        return new Category(id, newName, description, type, color, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Category updateDescription(String newDescription, UserId updatedBy) {
        return new Category(id, name, newDescription, type, color, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Category updateColor(String newColor, UserId updatedBy) {
        return new Category(id, name, description, type, newColor, createdInfo,
                new AuditInfo(updatedBy, updatedInfo.fromGroup(), updatedInfo.when()).updateTimestamp(), tombstone);
    }

    public Category delete() {
        return new Category(id, name, description, type, color, createdInfo, updatedInfo, Tombstone.deleted());
    }

    public boolean isDeleted() {
        return tombstone.isDeleted();
    }
}