package pl.btsoftware.backend.category.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.error.CategoryHasTransactionsException;
import pl.btsoftware.backend.category.domain.error.CategoryHierarchyTooDeepException;
import pl.btsoftware.backend.category.domain.error.CategoryNotFoundException;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

class CategoryServiceTest {

    private CategoryService categoryService;
    private InMemoryCategoryRepository categoryRepository;
    private TransactionQueryFacade transactionQueryFacade;
    private AuditModuleFacade auditModuleFacade;
    private User testUser;
    private GroupId testGroupId;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        var usersModuleFacade = mock(UsersModuleFacade.class);
        transactionQueryFacade = mock(TransactionQueryFacade.class);
        auditModuleFacade = mock(AuditModuleFacade.class);
        categoryService =
                new CategoryService(categoryRepository, usersModuleFacade, transactionQueryFacade, auditModuleFacade);

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
        var deletedCategory = createCategory(CategoryId.generate(), "Transport", CategoryType.EXPENSE, "#2196F3")
                .delete();

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

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"), null);
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

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"), null);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        var storedCategory = categoryRepository.findById(categoryId, testGroupId);
        assertThat(storedCategory).isPresent();
        assertThat(storedCategory.get()).isEqualTo(updatedCategory);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenUpdatingNonExistentCategory() {
        var nonExistentId = CategoryId.generate();
        var command = new UpdateCategoryCommand(nonExistentId, "Updated Food", Color.of("#FF9800"), null);

        assertThatThrownBy(() -> categoryService.updateCategory(command, testUser.id()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldThrowCategoryNotFoundExceptionWhenUpdatingCategoryFromDifferentGroup() {
        var categoryId = CategoryId.generate();
        var differentGroupId = GroupId.generate();
        var category = createCategoryForGroup(categoryId, "Food", CategoryType.EXPENSE, "#FF5722", differentGroupId);
        categoryRepository.store(category);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"), null);

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

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", null, null);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.name()).isEqualTo("Updated Food");
        assertThat(updatedCategory.color()).isEqualTo(Color.of("#FF5722"));
    }

    @Test
    void shouldUpdateOnlyColorWhenNameIsNull() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, null, Color.of("#FF9800"), null);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.name()).isEqualTo("Food");
        assertThat(updatedCategory.color()).isEqualTo(Color.of("#FF9800"));
    }

    @Test
    void shouldNotUpdateWhenNeitherNameNorColorChange() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Food", Color.of("#FF5722"), null);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.lastUpdatedAt()).isEqualTo(originalCategory.lastUpdatedAt());
        assertThat(updatedCategory.lastUpdatedBy()).isEqualTo(originalCategory.lastUpdatedBy());
    }

    @Test
    void shouldUpdateCategoryParent() {
        var categoryId = CategoryId.generate();
        var parentCategoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Restaurants", CategoryType.EXPENSE, "#FF5722");
        var parentCategory = createCategory(parentCategoryId, "Food", CategoryType.EXPENSE, "#FF9800");
        categoryRepository.store(originalCategory);
        categoryRepository.store(parentCategory);

        var command = new UpdateCategoryCommand(categoryId, "Restaurants", Color.of("#FF5722"), parentCategoryId);
        var updatedCategory = categoryService.updateCategory(command, testUser.id());

        assertThat(updatedCategory.parentId()).isEqualTo(parentCategoryId);
        assertThat(updatedCategory.lastUpdatedBy()).isEqualTo(testUser.id());
        assertThat(updatedCategory.lastUpdatedAt()).isAfter(originalCategory.lastUpdatedAt());
    }

    @Test
    void shouldCreateCategoryWithParent() {
        var parentCategoryCommand =
                new CreateCategoryCommand("Food", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id());
        var parentCategory = categoryService.createCategory(parentCategoryCommand);

        var command = new CreateCategoryCommand(
                "Restaurants", CategoryType.EXPENSE, Color.of("#FF9800"), testUser.id(), parentCategory.id());

        var childCategory = categoryService.createCategory(command);

        assertThat(childCategory).isNotNull();
        assertThat(childCategory.parentId()).isEqualTo(parentCategory.id());
        assertThat(childCategory.name()).isEqualTo("Restaurants");
        assertThat(childCategory.type()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldAllowCreatingCategoriesUp5LevelsDeep() {
        var level1 = categoryService.createCategory(
                new CreateCategoryCommand("Level1", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));
        var level2 = categoryService.createCategory(new CreateCategoryCommand(
                "Level2", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level1.id()));
        var level3 = categoryService.createCategory(new CreateCategoryCommand(
                "Level3", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level2.id()));
        var level4 = categoryService.createCategory(new CreateCategoryCommand(
                "Level4", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level3.id()));
        var level5 = categoryService.createCategory(new CreateCategoryCommand(
                "Level5", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level4.id()));

        assertThat(level5.parentId()).isEqualTo(level4.id());
    }

    @Test
    void shouldThrowCategoryHierarchyTooDeepExceptionWhenCreatingCategoryAt6thLevel() {
        var level1 = categoryService.createCategory(
                new CreateCategoryCommand("Level1", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));
        var level2 = categoryService.createCategory(new CreateCategoryCommand(
                "Level2", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level1.id()));
        var level3 = categoryService.createCategory(new CreateCategoryCommand(
                "Level3", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level2.id()));
        var level4 = categoryService.createCategory(new CreateCategoryCommand(
                "Level4", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level3.id()));
        var level5 = categoryService.createCategory(new CreateCategoryCommand(
                "Level5", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level4.id()));

        var level6Command = new CreateCategoryCommand(
                "Level6", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level5.id());

        assertThatThrownBy(() -> categoryService.createCategory(level6Command))
                .isInstanceOf(CategoryHierarchyTooDeepException.class);
    }

    @Test
    void shouldThrowCategoryHierarchyTooDeepExceptionWhenUpdatingCategoryToExceedMaxDepth() {
        var level1 = categoryService.createCategory(
                new CreateCategoryCommand("Level1", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));
        var level2 = categoryService.createCategory(new CreateCategoryCommand(
                "Level2", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level1.id()));
        var level3 = categoryService.createCategory(new CreateCategoryCommand(
                "Level3", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level2.id()));
        var level4 = categoryService.createCategory(new CreateCategoryCommand(
                "Level4", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level3.id()));
        var level5 = categoryService.createCategory(new CreateCategoryCommand(
                "Level5", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level4.id()));

        var separateCategory = categoryService.createCategory(
                new CreateCategoryCommand("Separate", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));

        var updateCommand =
                new UpdateCategoryCommand(separateCategory.id(), "Separate", Color.of("#FF5722"), level5.id());

        assertThatThrownBy(() -> categoryService.updateCategory(updateCommand, testUser.id()))
                .isInstanceOf(CategoryHierarchyTooDeepException.class);
    }

    @Test
    void shouldAllowUpdatingCategoryToValidHierarchyDepth() {
        var level1 = categoryService.createCategory(
                new CreateCategoryCommand("Level1", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));
        var level2 = categoryService.createCategory(new CreateCategoryCommand(
                "Level2", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level1.id()));
        var level3 = categoryService.createCategory(new CreateCategoryCommand(
                "Level3", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id(), level2.id()));

        var separateCategory = categoryService.createCategory(
                new CreateCategoryCommand("Separate", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id()));

        var updateCommand =
                new UpdateCategoryCommand(separateCategory.id(), "Separate", Color.of("#FF5722"), level3.id());
        var updatedCategory = categoryService.updateCategory(updateCommand, testUser.id());

        assertThat(updatedCategory.parentId()).isEqualTo(level3.id());
    }

    private Category createCategory(CategoryId categoryId, String name, CategoryType type, String colorHex) {
        return createCategoryForGroup(categoryId, name, type, colorHex, testGroupId);
    }

    private Category createCategoryForGroup(
            CategoryId categoryId, String name, CategoryType type, String colorHex, GroupId groupId) {
        var auditInfo = AuditInfo.create(testUser.id(), groupId);
        return new Category(categoryId, name, type, Color.of(colorHex), null, auditInfo, auditInfo, Tombstone.active());
    }

    @Test
    void shouldGetCategoriesByTypeIncludingChildCategories() {
        var parentCategory = createCategory(CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722");
        var childCategory1 = createCategoryWithParent(
                CategoryId.generate(), "Restaurants", CategoryType.EXPENSE, "#FF9800", parentCategory.id());
        var childCategory2 = createCategoryWithParent(
                CategoryId.generate(), "Groceries", CategoryType.EXPENSE, "#4CAF50", parentCategory.id());
        var grandChildCategory = createCategoryWithParent(
                CategoryId.generate(), "Fast Food", CategoryType.EXPENSE, "#2196F3", childCategory1.id());

        categoryRepository.store(parentCategory);
        categoryRepository.store(childCategory1);
        categoryRepository.store(childCategory2);
        categoryRepository.store(grandChildCategory);

        var categories = categoryService.getCategoriesByType(CategoryType.EXPENSE, testGroupId);

        assertThat(categories).hasSize(4);
        assertThat(categories)
                .containsExactlyInAnyOrder(parentCategory, childCategory1, childCategory2, grandChildCategory);
    }

    private Category createCategoryWithParent(
            CategoryId categoryId, String name, CategoryType type, String colorHex, CategoryId parentId) {
        var auditInfo = AuditInfo.create(testUser.id(), testGroupId);
        return new Category(
                categoryId, name, type, Color.of(colorHex), parentId, auditInfo, auditInfo, Tombstone.active());
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

    @Test
    void shouldThrowCategoryHasTransactionsExceptionWhenDeletingCategoryWithActiveTransactions() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(category);

        when(transactionQueryFacade.hasTransactions(categoryId, testGroupId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, testUser.id()))
                .isInstanceOf(CategoryHasTransactionsException.class);
    }

    @Test
    void shouldAllowDeletingCategoryWithoutTransactions() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(category);

        when(transactionQueryFacade.hasTransactions(categoryId, testGroupId)).thenReturn(false);

        categoryService.deleteCategory(categoryId, testUser.id());

        var deletedCategory = categoryRepository.findByIdIncludingDeleted(categoryId, testGroupId);
        assertThat(deletedCategory).isPresent();
        assertThat(deletedCategory.get().isDeleted()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoCategoriesExistForType() {
        var hasCategories = categoryService.hasCategories(CategoryType.EXPENSE, testGroupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldReturnTrueWhenCategoriesExistForType() {
        var expenseCategory = createCategory(CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(expenseCategory);

        var hasCategories = categoryService.hasCategories(CategoryType.EXPENSE, testGroupId);

        assertThat(hasCategories).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCategoriesExistButForDifferentType() {
        var incomeCategory = createCategory(CategoryId.generate(), "Salary", CategoryType.INCOME, "#4CAF50");
        categoryRepository.store(incomeCategory);

        var hasCategories = categoryService.hasCategories(CategoryType.EXPENSE, testGroupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldReturnFalseWhenCategoriesExistButForDifferentGroup() {
        var differentGroupId = GroupId.generate();
        var category = createCategoryForGroup(
                CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722", differentGroupId);
        categoryRepository.store(category);

        var hasCategories = categoryService.hasCategories(CategoryType.EXPENSE, testGroupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldReturnFalseWhenOnlyDeletedCategoriesExist() {
        var deletedCategory = createCategory(CategoryId.generate(), "Food", CategoryType.EXPENSE, "#FF5722")
                .delete();
        categoryRepository.store(deletedCategory);

        var hasCategories = categoryService.hasCategories(CategoryType.EXPENSE, testGroupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldLogAuditWhenCreatingCategory() {
        var command = new CreateCategoryCommand("Food", CategoryType.EXPENSE, Color.of("#FF5722"), testUser.id());

        var createdCategory = categoryService.createCategory(command);

        verify(auditModuleFacade)
                .logCategoryCreated(createdCategory.id(), "Food", "EXPENSE", testUser.id(), testGroupId);
    }

    @Test
    void shouldLogAuditWhenUpdatingCategory() {
        var categoryId = CategoryId.generate();
        var originalCategory = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(originalCategory);

        var command = new UpdateCategoryCommand(categoryId, "Updated Food", Color.of("#FF9800"), null);
        categoryService.updateCategory(command, testUser.id());

        verify(auditModuleFacade).logCategoryUpdated(categoryId, "Food", "Updated Food", testUser.id(), testGroupId);
    }

    @Test
    void shouldLogAuditWhenDeletingCategory() {
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Food", CategoryType.EXPENSE, "#FF5722");
        categoryRepository.store(category);

        when(transactionQueryFacade.hasTransactions(categoryId, testGroupId)).thenReturn(false);

        categoryService.deleteCategory(categoryId, testUser.id());

        verify(auditModuleFacade).logCategoryDeleted(categoryId, "Food", testUser.id(), testGroupId);
    }
}
