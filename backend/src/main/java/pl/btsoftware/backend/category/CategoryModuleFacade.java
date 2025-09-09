package pl.btsoftware.backend.category;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.category.application.CategoryService;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CategoryModuleFacade {
    private final CategoryService categoryService;
    private final UsersModuleFacade usersModuleFacade;

    public Category createCategory(CreateCategoryCommand command) {
        return categoryService.createCategory(command);
    }

    public Category getCategoryById(CategoryId id, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return categoryService.getCategoryById(id, user.groupId());
    }

    public List<Category> getAllCategories(UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return categoryService.getAllCategories(user.groupId());
    }

    public List<Category> getCategoriesByType(CategoryType type, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return categoryService.getCategoriesByType(type, user.groupId());
    }

    public Category updateCategory(UpdateCategoryCommand command, UserId userId) {
        return categoryService.updateCategory(command, userId);
    }

    public void deleteCategory(UUID categoryId, UserId userId) {
        categoryService.deleteCategory(CategoryId.of(categoryId), userId);
    }
}