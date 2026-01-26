package pl.btsoftware.backend.category.domain;

import java.util.List;
import java.util.Optional;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.domain.GroupId;

public interface CategoryRepository {
    void store(Category category);

    Optional<Category> findById(CategoryId id, GroupId groupId);

    Optional<Category> findByIdIncludingDeleted(CategoryId id, GroupId groupId);

    List<Category> findByType(CategoryType type, GroupId groupId);
}
