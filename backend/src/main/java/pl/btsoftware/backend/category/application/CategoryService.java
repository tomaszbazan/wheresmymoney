package pl.btsoftware.backend.category.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.category.domain.error.CategoryAccessDeniedException;
import pl.btsoftware.backend.category.domain.error.CategoryNameTooLongException;
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
        validateCategoryNameLength(command.name());

        var auditInfo = AuditInfo.create(command.userId().value(), user.groupId().value());
        var category = command.toDomain(auditInfo);
        categoryRepository.store(category);

        return category;
    }

    private void validateCategoryNameLength(String name) {
        if (name != null && name.length() > 100) {
            throw new CategoryNameTooLongException();
        }
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

        if (!category.ownedBy().equals(user.groupId())) {
            throw new CategoryAccessDeniedException();
        }

        var updatedCategory = category;

        if (command.name() != null) {
            validateCategoryNameLength(command.name());
            updatedCategory = updatedCategory.updateName(command.name(), userId);
        }

        if (command.description() != null) {
            updatedCategory = updatedCategory.updateDescription(command.description(), userId);
        }

        if (command.color() != null) {
            updatedCategory = updatedCategory.updateColor(command.color(), userId);
        }

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
}