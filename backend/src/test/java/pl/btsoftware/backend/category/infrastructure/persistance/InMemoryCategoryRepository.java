package pl.btsoftware.backend.category.infrastructure.persistance;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@Profile("test")
public class InMemoryCategoryRepository implements CategoryRepository {
    private final HashMap<CategoryId, Category> database = new HashMap<>();

    @Override
    public void store(Category category) {
        database.put(category.id(), category);
    }

    @Override
    public Optional<Category> findById(CategoryId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id))
                .filter(category -> category.ownedBy().equals(groupId) && !category.isDeleted());
    }

    @Override
    public Optional<Category> findByIdIncludingDeleted(CategoryId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id))
                .filter(category -> category.ownedBy().equals(groupId));
    }

    @Override
    public List<Category> findByType(CategoryType type, GroupId groupId) {
        return database.values().stream()
                .filter(
                        category ->
                                category.type().equals(type)
                                        && category.ownedBy().equals(groupId)
                                        && !category.isDeleted())
                .toList();
    }

    @Override
    public List<Category> findAllByIds(Set<CategoryId> ids, GroupId groupId) {
        return database.values().stream()
                .filter(
                        category ->
                                ids.contains(category.id())
                                        && category.ownedBy().equals(groupId)
                                        && !category.isDeleted())
                .toList();
    }
}
