package pl.btsoftware.backend.category.infrastructure.persistance;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaCategoryRepository implements CategoryRepository {

    private final CategoryJpaRepository repository;

    @Override
    public void store(Category category) {
        var entity = CategoryEntity.fromDomain(category);
        repository.save(entity);
    }

    @Override
    public Optional<Category> findById(CategoryId id, GroupId groupId) {
        return repository
                .findByIdAndCreatedByGroupAndIsDeletedFalse(id.value(), groupId.value())
                .map(CategoryEntity::toDomain);
    }

    @Override
    public Optional<Category> findByIdIncludingDeleted(CategoryId id, GroupId groupId) {
        return repository
                .findByIdAndCreatedByGroup(id.value(), groupId.value())
                .map(CategoryEntity::toDomain);
    }

    @Override
    public List<Category> findByType(CategoryType type, GroupId groupId) {
        return repository
                .findByTypeAndCreatedByGroupAndIsDeletedFalse(type, groupId.value())
                .stream()
                .map(CategoryEntity::toDomain)
                .toList();
    }
}
