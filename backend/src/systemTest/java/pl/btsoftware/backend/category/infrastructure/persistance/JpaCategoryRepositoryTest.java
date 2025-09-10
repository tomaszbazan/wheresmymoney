package pl.btsoftware.backend.category.infrastructure.persistance;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.btsoftware.backend.shared.CategoryType.EXPENSE;
import static pl.btsoftware.backend.shared.CategoryType.INCOME;

@SystemTest
class JpaCategoryRepositoryTest {

    @Autowired
    private CategoryRepository repository;

    private Category createTestCategory(CategoryType type, GroupId groupId, boolean isDeleted) {
        var userId = UserId.generate();
        var auditInfo = AuditInfo.create(userId.value(), groupId.value(), OffsetDateTime.now());
        var tombstone = isDeleted ? Tombstone.deleted() : Tombstone.active();

        return new Category(
                CategoryId.generate(),
                "Test Category",
                type,
                Color.of("#FF0000"),
                auditInfo,
                auditInfo,
                tombstone
        );
    }

    @Nested
    class Store {
        @Test
        void shouldStoreCategory() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, groupId, false);

            // when
            repository.store(category);

            // then
            var storedCategory = repository.findById(category.id(), groupId);
            assertThat(storedCategory).isPresent();
            assertThat(storedCategory.get().id()).isEqualTo(category.id());
            assertThat(storedCategory.get().name()).isEqualTo(category.name());
            assertThat(storedCategory.get().type()).isEqualTo(category.type());
            assertThat(storedCategory.get().color()).isEqualTo(category.color());
            assertThat(storedCategory.get().ownedBy()).isEqualTo(groupId);
            assertThat(storedCategory.get().isDeleted()).isFalse();
        }

        @Test
        void shouldStoreDeletedCategory() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(INCOME, groupId, true);

            // when
            repository.store(category);

            // then
            var storedCategory = repository.findByIdIncludingDeleted(category.id(), groupId);
            assertThat(storedCategory).isPresent();
            assertThat(storedCategory.get().isDeleted()).isTrue();
            assertThat(storedCategory.get().tombstone().deletedAt()).isNotNull();
        }

        @Test
        void shouldUpdateExistingCategory() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, groupId, false);
            repository.store(category);
            var updateCommand = new UpdateCategoryCommand(
                    category.id(),
                    "Updated Name",
                    category.color()
            );

            var updatedCategory = category.updateWith(updateCommand, category.createdBy());

            // when
            repository.store(updatedCategory);

            // then
            var storedCategory = repository.findById(category.id(), groupId);
            assertThat(storedCategory).isPresent();
            assertThat(storedCategory.get().name()).isEqualTo("Updated Name");
        }
    }

    @Nested
    class FindById {
        @Test
        void shouldFindCategoryByIdAndGroup() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, groupId, false);
            repository.store(category);

            // when
            var result = repository.findById(category.id(), groupId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(category.id());
            assertThat(result.get().name()).isEqualTo(category.name());
            assertThat(result.get().type()).isEqualTo(category.type());
            assertThat(result.get().color()).isEqualTo(category.color());
            assertThat(result.get().ownedBy()).isEqualTo(groupId);
        }

        @Test
        void shouldNotFindCategoryFromDifferentGroup() {
            // given
            var originalGroupId = GroupId.generate();
            var differentGroupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, originalGroupId, false);
            repository.store(category);

            // when
            var result = repository.findById(category.id(), differentGroupId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotFindDeletedCategory() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, groupId, true);
            repository.store(category);

            // when
            var result = repository.findById(category.id(), groupId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenCategoryNotExists() {
            // given
            var nonExistentId = CategoryId.generate();
            var groupId = GroupId.generate();

            // when
            var result = repository.findById(nonExistentId, groupId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByIdIncludingDeleted {
        @Test
        void shouldFindActiveCategoryByIdAndGroup() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(INCOME, groupId, false);
            repository.store(category);

            // when
            var result = repository.findByIdIncludingDeleted(category.id(), groupId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(category.id());
            assertThat(result.get().name()).isEqualTo(category.name());
            assertThat(result.get().isDeleted()).isFalse();
        }

        @Test
        void shouldFindDeletedCategoryByIdAndGroup() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(INCOME, groupId, true);
            repository.store(category);

            // when
            var result = repository.findByIdIncludingDeleted(category.id(), groupId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(category.id());
            assertThat(result.get().name()).isEqualTo(category.name());
            assertThat(result.get().isDeleted()).isTrue();
        }

        @Test
        void shouldNotFindCategoryFromDifferentGroup() {
            // given
            var originalGroupId = GroupId.generate();
            var differentGroupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, originalGroupId, false);
            repository.store(category);

            // when
            var result = repository.findByIdIncludingDeleted(category.id(), differentGroupId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenCategoryNotExists() {
            // given
            var nonExistentId = CategoryId.generate();
            var groupId = GroupId.generate();

            // when
            var result = repository.findByIdIncludingDeleted(nonExistentId, groupId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByType {
        @Test
        void shouldFindAllExpenseCategoriesForGroup() {
            // given
            var groupId = GroupId.generate();
            var expenseCategory1 = createTestCategory(EXPENSE, groupId, false);
            var expenseCategory2 = createTestCategory(EXPENSE, groupId, false);
            var incomeCategory = createTestCategory(INCOME, groupId, false);
            var deletedExpenseCategory = createTestCategory(EXPENSE, groupId, true);

            repository.store(expenseCategory1);
            repository.store(expenseCategory2);
            repository.store(incomeCategory);
            repository.store(deletedExpenseCategory);

            // when
            var result = repository.findByType(EXPENSE, groupId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Category::id)
                    .containsExactlyInAnyOrder(expenseCategory1.id(), expenseCategory2.id());
            assertThat(result).allMatch(category -> category.type() == EXPENSE);
            assertThat(result).allMatch(category -> !category.isDeleted());
        }

        @Test
        void shouldFindAllIncomeCategoriesForGroup() {
            // given
            var groupId = GroupId.generate();
            var incomeCategory1 = createTestCategory(INCOME, groupId, false);
            var incomeCategory2 = createTestCategory(INCOME, groupId, false);
            var expenseCategory = createTestCategory(EXPENSE, groupId, false);

            repository.store(incomeCategory1);
            repository.store(incomeCategory2);
            repository.store(expenseCategory);

            // when
            var result = repository.findByType(INCOME, groupId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Category::id)
                    .containsExactlyInAnyOrder(incomeCategory1.id(), incomeCategory2.id());
            assertThat(result).allMatch(category -> category.type() == INCOME);
            assertThat(result).allMatch(category -> !category.isDeleted());
        }

        @Test
        void shouldNotFindCategoriesFromDifferentGroup() {
            // given
            var group1Id = GroupId.generate();
            var group2Id = GroupId.generate();
            var category1 = createTestCategory(EXPENSE, group1Id, false);
            var category2 = createTestCategory(EXPENSE, group2Id, false);

            repository.store(category1);
            repository.store(category2);

            // when
            var result = repository.findByType(EXPENSE, group1Id);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(category1.id());
        }

        @Test
        void shouldNotFindDeletedCategories() {
            // given
            var groupId = GroupId.generate();
            var activeCategory = createTestCategory(EXPENSE, groupId, false);
            var deletedCategory = createTestCategory(EXPENSE, groupId, true);

            repository.store(activeCategory);
            repository.store(deletedCategory);

            // when
            var result = repository.findByType(EXPENSE, groupId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(activeCategory.id());
        }

        @Test
        void shouldReturnEmptyListWhenNoCategoriesOfTypeExist() {
            // given
            var groupId = GroupId.generate();

            // when
            var result = repository.findByType(EXPENSE, groupId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenOnlyDeletedCategoriesOfTypeExist() {
            // given
            var groupId = GroupId.generate();
            var deletedCategory = createTestCategory(EXPENSE, groupId, true);
            repository.store(deletedCategory);

            // when
            var result = repository.findByType(EXPENSE, groupId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class EntityToDomainMapping {
        @Test
        void shouldCorrectlyMapEntityToDomain() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(EXPENSE, groupId, false);
            repository.store(category);

            // when
            var result = repository.findById(category.id(), groupId);

            // then
            assertThat(result).isPresent();
            var mapped = result.get();
            assertThat(mapped.id()).isEqualTo(category.id());
            assertThat(mapped.name()).isEqualTo(category.name());
            assertThat(mapped.type()).isEqualTo(category.type());
            assertThat(mapped.color()).isEqualTo(category.color());
            assertThat(mapped.createdBy()).isEqualTo(category.createdBy());
            assertThat(mapped.ownedBy()).isEqualTo(groupId);
            assertThat(mapped.createdAt()).isNotNull();
            assertThat(mapped.lastUpdatedAt()).isNotNull();
            assertThat(mapped.isDeleted()).isFalse();
        }

        @Test
        void shouldCorrectlyMapDeletedEntityToDomain() {
            // given
            var groupId = GroupId.generate();
            var category = createTestCategory(INCOME, groupId, true);
            repository.store(category);

            // when
            var result = repository.findByIdIncludingDeleted(category.id(), groupId);

            // then
            assertThat(result).isPresent();
            var mapped = result.get();
            assertThat(mapped.isDeleted()).isTrue();
            assertThat(mapped.tombstone().deletedAt()).isNotNull();
        }
    }
}