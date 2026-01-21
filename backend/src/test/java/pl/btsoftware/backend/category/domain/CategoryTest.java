package pl.btsoftware.backend.category.domain;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.error.CategoryNameEmptyException;
import pl.btsoftware.backend.category.domain.error.CategoryNameInvalidCharactersException;
import pl.btsoftware.backend.category.domain.error.CategoryNameTooLongException;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.users.domain.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithValidName() {
        // given
        var name = "Valid Name -_@?!.";

        // when
        var category = Category.create(name, CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));

        // then
        assertThat(category.name()).isEqualTo(name);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Category.create(null, CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class)))
                .isInstanceOf(CategoryNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Category.create("", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class)))
                .isInstanceOf(CategoryNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> Category.create("   ", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class)))
                .isInstanceOf(CategoryNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        assertThatThrownBy(() -> Category.create("a".repeat(101), CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class)))
                .isInstanceOf(CategoryNameTooLongException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameContainsInvalidCharacters() {
        // given
        var invalidName = "Invalid<Name>";

        // when & then
        assertThatThrownBy(() -> Category.create(invalidName, CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class)))
                .isInstanceOf(CategoryNameInvalidCharactersException.class);
    }

    @Test
    void shouldUpdateCategoryNameSuccessfully() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), "Updated Name", null, null);
        var updatedBy = UserId.generate();

        // when
        var updatedCategory = category.updateWith(command, updatedBy);

        // then
        assertThat(updatedCategory.name()).isEqualTo("Updated Name");
    }

    @Test
    void shouldNotUpdateCategoryNameWhenNameIsNull() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), null, null, null);
        var updatedBy = UserId.generate();

        // when
        var updatedCategory = category.updateWith(command, updatedBy);

        // then
        assertThat(updatedCategory.name()).isEqualTo("Original Name");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithEmptyName() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), "", null, null);
        var updatedBy = UserId.generate();

        // when & then
        assertThatThrownBy(() -> category.updateWith(command, updatedBy))
                .isInstanceOf(CategoryNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithBlankName() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), "   ", null, null);
        var updatedBy = UserId.generate();

        // when & then
        assertThatThrownBy(() -> category.updateWith(command, updatedBy))
                .isInstanceOf(CategoryNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithTooLongName() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), "a".repeat(101), null, null);
        var updatedBy = UserId.generate();

        // when & then
        assertThatThrownBy(() -> category.updateWith(command, updatedBy))
                .isInstanceOf(CategoryNameTooLongException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidCharacters() {
        // given
        var category = Category.create("Original Name", CategoryType.EXPENSE, Color.of("#FFFFFF"), Instancio.create(AuditInfo.class));
        var command = new UpdateCategoryCommand(category.id(), "Invalid<Name>", null, null);
        var updatedBy = UserId.generate();

        // when & then
        assertThatThrownBy(() -> category.updateWith(command, updatedBy))
                .isInstanceOf(CategoryNameInvalidCharactersException.class);
    }
}
