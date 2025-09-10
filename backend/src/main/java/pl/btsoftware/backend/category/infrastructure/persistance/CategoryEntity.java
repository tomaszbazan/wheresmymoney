package pl.btsoftware.backend.category.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CategoryEntity {
    @Id
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    private String color;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_by_group")
    private UUID createdByGroup;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_by_group")
    private UUID updatedByGroup;
    @Column(name = "is_deleted")
    private boolean isDeleted;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public static CategoryEntity fromDomain(Category category) {
        return new CategoryEntity(
                category.id().value(),
                category.name(),
                category.type(),
                category.color().value(),
                category.createdAt(),
                category.createdBy().value(),
                category.ownedBy().value(),
                category.lastUpdatedAt(),
                category.lastUpdatedBy().value(),
                category.ownedBy().value(),
                category.tombstone().isDeleted(),
                category.tombstone().deletedAt()
        );
    }

    public Category toDomain() {
        var createdAuditInfo = AuditInfo.create(createdBy, createdByGroup, createdAt);
        var updatedAuditInfo = AuditInfo.create(updatedBy, updatedByGroup, updatedAt);
        return new Category(
                CategoryId.of(id),
                name,
                type,
                Color.of(color),
                createdAuditInfo,
                updatedAuditInfo,
                new Tombstone(isDeleted, deletedAt)
        );
    }
}