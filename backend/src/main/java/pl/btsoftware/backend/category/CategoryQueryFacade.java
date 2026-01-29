package pl.btsoftware.backend.category;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.users.domain.GroupId;

@RequiredArgsConstructor
public class CategoryQueryFacade {
    private final CategoryRepository categoryRepository;

    public boolean allCategoriesExists(Set<CategoryId> categoryIds, GroupId groupId) {
        var foundCategories = categoryRepository.findAllByIds(categoryIds, groupId);
        return foundCategories.size() == categoryIds.size();
    }
}
