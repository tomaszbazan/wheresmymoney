package pl.btsoftware.backend.category.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByIdAndCreatedByGroupAndIsDeletedFalse(UUID id, UUID createdByGroup);

    Optional<CategoryEntity> findByIdAndCreatedByGroup(UUID id, UUID createdByGroup);

    List<CategoryEntity> findByTypeAndCreatedByGroupAndIsDeletedFalse(CategoryType type, UUID createdByGroup);
}