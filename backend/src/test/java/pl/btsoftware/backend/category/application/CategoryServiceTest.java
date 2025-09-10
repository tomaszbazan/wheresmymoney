package pl.btsoftware.backend.category.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.error.CategoryNotFoundException;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    private CategoryService categoryService;
    private InMemoryCategoryRepository categoryRepository;
    private UsersModuleFacade usersModuleFacade;
    private User testUser;
    private GroupId testGroupId;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        usersModuleFacade = mock(UsersModuleFacade.class);
        categoryService = new CategoryService(categoryRepository, usersModuleFacade);

        testGroupId = GroupId.generate();
        testUser = createUser(UserId.generate(), testGroupId);
        when(usersModuleFacade.findUserOrThrow(testUser.id())).thenReturn(testUser);
    }

    @Test
    void shouldCreateCategory() {
        var command = new CreateCategoryCommand("Food", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id());

        var createdCategory = categoryService.createCategory(command);

        assertThat(createdCategory).isNotNull();
        assertThat(createdCategory.id()).isNotNull();
        assertThat(createdCategory.name()).isEqualTo("Food");
        assertThat(createdCategory.type()).isEqualTo(CategoryType.EXPENSE);
        assertThat(createdCategory.color()).isEqualTo(Color.of("#FF5722"));
        assertThat(createdCategory.createdBy()).isEqualTo(testUser.id());
        assertThat(createdCategory.ownedBy()).isEqualTo(testGroupId);
        assertThat(createdCategory.isDeleted()).isFalse();
    }

    @Test
    void shouldStoreCategoryInRepository() {
        var command = new CreateCategoryCommand("Salary", CategoryType.INCOME, Color.of("#4CAF50"), testUser.id());

        var createdCategory = categoryService.createCategory(command);

        var storedCategory = categoryRepository.findById(createdCategory.id(), testGroupId);
        assertThat(storedCategory).isPresent();
        assertThat(storedCategory.get()).isEqualTo(createdCategory);
    }

    @Test
    void shouldGetCategoryById() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Transportation", CategoryType.EXPENSE, "#2196F3");
        categoryRepository.store(category);

        var foundCategory = categoryService.getCategoryById(categoryId, testGroupId);

        assertThat(foundCategory).isEqualTo(category);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExist() {
        var nonExistentId = CategoryId.generate();

        assertThatThrownBy(() -> categoryService.getCategoryById(nonExistentId, testGroupId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenCategoryBelongsToDifferentGroup() {
        var categoryId = CategoryId.generate();
        var differentGroupId = GroupId.generate();
        var category = createCategoryForGroup(categoryId, "Food", CategoryType.EXPENSE, "#FF5722", differentGroupId);
        categoryRepository.store(category);

        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId, testGroupId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldGetCategoriesByType() {
        var expenseCategory1 = createCategory(CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722");
        var expenseCategory2 = createCategory(CategoryId.generate(), "Transport", CategoryType.EXPENSE, "#2196F3");
        var incomeCategory = createCategory(CategoryId.generate(), "Salary", CategoryType.INCOME, "#4CAF50");

        categoryRepository.store(expenseCategory1);
        categoryRepository.store(expenseCategory2);
        categoryRepository.store(incomeCategory);

        var expenseCategories = categoryService.getCategoriesByType(CategoryType.EXPENSE, testGroupId);
        var incomeCategories = categoryService.getCategoriesByType(CategoryType.INCOME, testGroupId);

        assertThat(expenseCategories).hasSize(2);
        assertThat(expenseCategories).containsExactlyInAnyOrder(expenseCategory1, expenseCategory2);
        assertThat(incomeCategories).hasSize(1);
        assertThat(incomeCategories).containsExactly(incomeCategory);
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExistForType() {
        var categories = categoryService.getCategoriesByType(CategoryType.EXPENSE, testGroupId);

        assertThat(categories).isEmpty();
    }

    @Test
    void shouldNotReturnDeletedCategoriesWhenGettingByType() {
        var activeCategory = createCategory(CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722");
        var deletedCategory = createCategory(CategoryId.generate(), "Transport", CategoryType.EXPENSE, "#2196F3").delete();

        categoryRepository.store(activeCategory);
        categoryRepository.store(deletedCategory);

        var categories = categoryService.getCategoriesByType(CategoryType.EXPENSE, testGroupId);

        assertThat(categories).hasSize(1);
        assertThat(categories).containsExactly(activeCategory);
    }

    @Test
    void shouldUpdateCategory() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"));
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.id()).isEqualTo(categoryId);
        assertThat(updatedCategory.name()).isEqualTo("Updated Food");
        assertThat(updatedCategory.color()).isEqualTo(Color.of("#FF9800"));
        assertThat(updatedCategory.type()).isEqualTo(CategoryType.EXPENSE);
        assertThat(updatedCategory.lastUpdatedBy()).isEqualTo(testUser.id());
        assertThat(updatedCategory.lastUpdatedAt()).isAfter(originalCategory.lastUpdatedAt());
    }

    @Test
    void shouldStoreUpdatedCategoryInRepository() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"));
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        var storedCategory = categoryRepository.findById(categoryId, testGroupId);
        assertThat(storedCategory).isPresent();
        assertThat(storedCategory.get()).isEqualTo(updatedCategory);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenUpdatingNonExistentCategory() {
        var nonExistentId = CategoryId.generate();
        var command = new UpdateCategoryCommand(nonExistentId, "Updated Food", Color.of("#FF9800"));

        assertThatThrownBy(() -> categoryService.updateCategory(command, testUser.id()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenUpdatingCategoryFromDifferentGroup() {
        var categoryId = CategoryId.generate();
        var differentGroupId = GroupId.generate();
        var category = createCategoryForGroup(categoryId, "Food", CategoryType.EXPENSE, "#FF5722", differentGroupId);
        categoryRepository.store(category);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"));

        assertThatThrownBy(() -> categoryService.updateCategory(command, testUser.id()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldDeleteCategory() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(category);

        categoryService.deleteCategory(categoryId, testUser.id());

        var deletedCategory = categoryRepository.findByIdIncludingDeleted(categoryId, testGroupId);
        assertThat(deletedCategory).isPresent();
        assertThat(deletedCategory.get().isDeleted()).isTrue();
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenDeletingNonExistentCategory() {
        var nonExistentId = CategoryId.generate();

        assertThatThrownBy(() -> categoryService.deleteCategory(nonExistentId, testUser.id()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenDeletingCategoryFromDifferentGroup() {
        var categoryId = CategoryId.generate();
        var differentGroupId = GroupId.generate();
        var category = createCategoryForGroup(categoryId, "Food", CategoryType.EXPENSE, "#FF5722", differentGroupId);
        categoryRepository.store(category);

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, testUser.id()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldNotReturnDeletedCategoryWhenGettingById() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(category);
        categoryService.deleteCategory(categoryId, testUser.id());

        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId, testGroupId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldUpdateOnlyNameWhenColorIsNull() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", null);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.name()).isEqualTo("Updated Food");
        assertThat(updatedCategory.color()).isEqualTo(Color.of("#FF5722"));
    }

    @Test
    void shouldUpdateOnlyColorWhenNameIsNull() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, null, Color.of("#FF9800"));
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.name()).isEqualTo("Food");
        assertThat(updatedCategory.color()).isEqualTo(Color.of("#FF9800"));
    }

    @Test
    void shouldNotUpdateWhenNeitherNameNorColorChange() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Food", Color.of("#FF5722"));
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.lastUpdatedAt()).isEqualTo(originalCategory.lastUpdatedAt());
        assertThat(updatedCategory.lastUpdatedBy()).isEqualTo(originalCategory.lastUpdatedBy());
    }

    private Category createCategory(CategoryId categoryId, String name, CategoryType type, String colorHex) {
        return createCategoryForGroup(categoryId, name, type, colorHex, testGroupId);
    }

    private Category createCategoryForGroup(CategoryId categoryId, String name, CategoryType type, String colorHex, GroupId groupId) {
        var auditInfo = AuditInfo.create(testUser.id(), groupId);
        return new Category(
                categoryId,
                name,
                type,
                Color.of(colorHex),
                auditInfo,
                auditInfo,
                Tombstone.active()
        );
    }

    private User createUser(UserId userId, GroupId groupId) {
        return mock(User.class, invocation -> {
            if ("id".equals(invocation.getMethod().getName())) {
                return userId;
            }
            if ("groupId".equals(invocation.getMethod().getName())) {
                return groupId;
            }
            return null;
        });
    }
}