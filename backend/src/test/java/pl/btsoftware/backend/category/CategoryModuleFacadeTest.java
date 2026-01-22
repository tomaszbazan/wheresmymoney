package pl.btsoftware.backend.category;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.btsoftware.backend.category.application.CategoryService;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

class CategoryModuleFacadeTest {

    private CategoryModuleFacade categoryModuleFacade;
    private CategoryService categoryService;
    private UsersModuleFacade usersModuleFacade;

    @BeforeEach
    void setUp() {
        var categoryRepository = new InMemoryCategoryRepository();
        var transactionQueryFacade = Mockito.mock(TransactionQueryFacade.class);
        usersModuleFacade = Mockito.mock(UsersModuleFacade.class);
        categoryService = new CategoryService(categoryRepository, usersModuleFacade, transactionQueryFacade);
        categoryModuleFacade = new CategoryModuleFacade(categoryService, usersModuleFacade);
    }

    @Test
    void shouldCreateCategory() {
        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        var user = Instancio.of(User.class)
                .set(field(User::id), command.userId())
                .create();
        when(usersModuleFacade.findUserOrThrow(command.userId())).thenReturn(user);

        var category = categoryModuleFacade.createCategory(command);

        assertThat(category.name()).isEqualTo(command.name());
        assertThat(category.type()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldGetCategoryById() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), userId)
                .create();
        var category = categoryModuleFacade.createCategory(command);

        var retrievedCategory = categoryModuleFacade.getCategoryById(category.id(), userId);

        assertThat(retrievedCategory.id()).isEqualTo(category.id());
    }

    @Test
    void shouldGetCategoriesByType() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), userId)
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        categoryModuleFacade.createCategory(command);

        var categories = categoryModuleFacade.getCategoriesByType(CategoryType.EXPENSE, userId);

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).type()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void shouldUpdateCategory() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var createCommand = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), userId)
                .create();
        var category = categoryModuleFacade.createCategory(createCommand);

        var updateCommand = new UpdateCategoryCommand(
                category.id(),
                "Updated Category",
                Color.of("#FF0000"),
                null
        );
        var updatedCategory = categoryModuleFacade.updateCategory(updateCommand, userId);

        assertThat(updatedCategory.name()).isEqualTo("Updated Category");
        assertThat(updatedCategory.color().value()).isEqualTo("#FF0000");
    }

    @Test
    void shouldDeleteCategory() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), userId)
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        var category = categoryModuleFacade.createCategory(command);

        categoryModuleFacade.deleteCategory(category.id().value(), userId);

        var categories = categoryModuleFacade.getCategoriesByType(CategoryType.EXPENSE, userId);
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldCheckIfHasCategories() {
        var userId = UserId.generate();
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::id), userId)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(userId)).thenReturn(user);

        assertThat(categoryModuleFacade.hasCategories(CategoryType.EXPENSE, groupId)).isFalse();

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), userId)
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        categoryModuleFacade.createCategory(command);

        assertThat(categoryModuleFacade.hasCategories(CategoryType.EXPENSE, groupId)).isTrue();
    }
}
