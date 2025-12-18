package pl.btsoftware.backend.category;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.domain.GroupId;

@RequiredArgsConstructor
public class CategoryQueryFacade {
    private final CategoryRepository categoryRepository;

    public boolean hasCategories(CategoryType type, GroupId groupId) {
        return !categoryRepository.findByType(type, groupId).isEmpty();
    }
}
