package pl.btsoftware.backend.category.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.category.domain.error.CategoryAccessDeniedException;
import pl.btsoftware.backend.category.domain.error.CategoryHierarchyTooDeepException;
import pl.btsoftware.backend.category.domain.error.CategoryNotFoundException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UsersModuleFacade usersModuleFacade;

    @Transactional
    public Category createCategory(CreateCategoryCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());

        validateHierarchyDepth(command.parentId(), user.groupId());

        var auditInfo = AuditInfo.create(command.userId().value(), user.groupId().value());
        var category = command.toDomain(auditInfo);
        categoryRepository.store(category);

        return category;
    }

    public Category getCategoryById(CategoryId categoryId, GroupId groupId) {
        return categoryRepository.findById(categoryId, groupId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    public List<Category> getCategoriesByType(CategoryType type, GroupId groupId) {
        return categoryRepository.findByType(type, groupId);
    }

    @Transactional
    public Category updateCategory(UpdateCategoryCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var category = categoryRepository.findById(command.categoryId(), user.groupId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        if (command.parentId() != null && !command.parentId().equals(category.parentId())) {
            validateHierarchyDepth(command.parentId(), user.groupId());
        }

        var updatedCategory = category.updateWith(command, userId);

        categoryRepository.store(updatedCategory);
        return updatedCategory;
    }

    @Transactional
    public void deleteCategory(CategoryId categoryId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var category = categoryRepository.findByIdIncludingDeleted(categoryId, user.groupId())
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if (!category.ownedBy().equals(user.groupId())) {
            throw new CategoryAccessDeniedException();
        }

        var deletedCategory = category.delete();
        categoryRepository.store(deletedCategory);
    }

    private void validateHierarchyDepth(CategoryId parentId, GroupId groupId) {
        if (parentId == null) {
            return;
        }

        var depth = calculateHierarchyDepth(parentId, groupId);
        if (depth >= 5) {
            throw new CategoryHierarchyTooDeepException();
        }
    }

    private int calculateHierarchyDepth(CategoryId categoryId, GroupId groupId) {
        if (categoryId == null) {
            return 0;
        }

        var category = categoryRepository.findById(categoryId, groupId);
        if (category.isEmpty()) {
            return 0;
        }

        return 1 + calculateHierarchyDepth(category.get().parentId(), groupId);
    }
}
