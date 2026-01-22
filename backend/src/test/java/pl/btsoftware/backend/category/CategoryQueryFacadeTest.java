package pl.btsoftware.backend.category;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.category.application.CategoryService;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryQueryFacadeTest {

    private CategoryQueryFacade categoryQueryFacade;
    private InMemoryCategoryRepository categoryRepository;
    private CategoryService categoryService;
    private UsersModuleFacade usersModuleFacade;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        var transactionQueryFacade = mock(TransactionQueryFacade.class);
        usersModuleFacade = mock(UsersModuleFacade.class);
        categoryService = new CategoryService(categoryRepository, usersModuleFacade, transactionQueryFacade);
        categoryQueryFacade = new CategoryQueryFacade(categoryRepository);
    }

    @Test
    void shouldReturnFalseWhenNoCategoriesExist() {
        var groupId = GroupId.generate();

        var hasCategories = categoryQueryFacade.hasCategories(CategoryType.EXPENSE, groupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldReturnTrueWhenCategoriesExist() {
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(user.id())).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), user.id())
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        categoryService.createCategory(command);

        var hasCategories = categoryQueryFacade.hasCategories(CategoryType.EXPENSE, groupId);

        assertThat(hasCategories).isTrue();
    }

    @Test
    void shouldReturnFalseForDifferentCategoryType() {
        var groupId = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::groupId), groupId)
                .create();
        when(usersModuleFacade.findUserOrThrow(user.id())).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), user.id())
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        categoryService.createCategory(command);

        var hasCategories = categoryQueryFacade.hasCategories(CategoryType.INCOME, groupId);

        assertThat(hasCategories).isFalse();
    }

    @Test
    void shouldReturnFalseForDifferentGroup() {
        var groupId1 = GroupId.generate();
        var groupId2 = GroupId.generate();
        var user = Instancio.of(User.class)
                .set(field(User::groupId), groupId1)
                .create();
        when(usersModuleFacade.findUserOrThrow(user.id())).thenReturn(user);

        var command = Instancio.of(CreateCategoryCommand.class)
                .set(field(CreateCategoryCommand::userId), user.id())
                .set(field(CreateCategoryCommand::type), CategoryType.EXPENSE)
                .create();
        categoryService.createCategory(command);

        var hasCategories = categoryQueryFacade.hasCategories(CategoryType.EXPENSE, groupId2);

        assertThat(hasCategories).isFalse();
    }
}
