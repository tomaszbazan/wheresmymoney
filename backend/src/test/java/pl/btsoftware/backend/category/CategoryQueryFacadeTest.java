package pl.btsoftware.backend.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

import java.util.Set;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;

class CategoryQueryFacadeTest {

    private CategoryQueryFacade categoryQueryFacade;
    private InMemoryCategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        categoryQueryFacade = new CategoryQueryFacade(categoryRepository);
    }

    @Nested
    class AllCategoriesExists {

        @Test
        void shouldReturnFalseWhenNoCategoriesExist() {
            // given
            var groupId = GroupId.generate();
            var categoryId = CategoryId.generate();

            // when
            var allExist = categoryQueryFacade.allCategoriesExists(Set.of(categoryId), groupId);

            // then
            assertThat(allExist).isFalse();
        }

        @Test
        void shouldReturnTrueWhenAllCategoriesExist() {
            // given
            var groupId = GroupId.generate();
            var category1 = Instancio.of(Category.class)
                    .set(field(AuditInfo::fromGroup), groupId)
                    .set(field(Category::tombstone), Tombstone.active())
                    .set(field(Category::name), "Test Category")
                    .create();
            categoryRepository.store(category1);

            var category2 = Instancio.of(Category.class)
                    .set(field(AuditInfo::fromGroup), groupId)
                    .set(field(Category::tombstone), Tombstone.active())
                    .set(field(Category::name), "Test Category")
                    .create();
            categoryRepository.store(category2);

            // when
            var allExist = categoryQueryFacade.allCategoriesExists(Set.of(category1.id(), category2.id()), groupId);

            // then
            assertThat(allExist).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCategoriesAreDeleted() {
            // given
            var groupId = GroupId.generate();
            var category = Instancio.of(Category.class)
                    .set(field(AuditInfo::fromGroup), groupId)
                    .set(field(Category::tombstone), Tombstone.deleted())
                    .set(field(Category::name), "Test Category")
                    .create();
            categoryRepository.store(category);

            // when
            var allExist = categoryQueryFacade.allCategoriesExists(Set.of(category.id()), groupId);

            // then
            assertThat(allExist).isFalse();
        }

        @Test
        void shouldReturnFalseWhenCategoryExistsForDifferentGroup() {
            // given
            var groupId1 = GroupId.generate();
            var groupId2 = GroupId.generate();

            var category = Instancio.of(Category.class)
                    .set(field(AuditInfo::fromGroup), groupId1)
                    .set(field(Category::tombstone), Tombstone.active())
                    .set(field(Category::name), "Test Category")
                    .create();
            categoryRepository.store(category);

            // when
            var allExist = categoryQueryFacade.allCategoriesExists(Set.of(category.id()), groupId2);

            // then
            assertThat(allExist).isFalse();
        }
    }
}
