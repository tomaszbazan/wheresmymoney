package pl.btsoftware.backend.transaction.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Function;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.Transaction;

public record TransactionView(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String type,
        String description,
        CategoryView category,
        LocalDate transactionDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
    public static TransactionView from(
            Transaction transaction, Function<CategoryId, Category> categoryMapper) {
        var category = categoryMapper.apply(transaction.categoryId());
        return new TransactionView(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.amount().value(),
                transaction.type().name(),
                transaction.description(),
                CategoryView.from(category),
                transaction.transactionDate(),
                transaction.createdAt(),
                transaction.lastUpdatedAt());
    }

    record CategoryView(UUID id, String name) {
        public static CategoryView from(Category category) {
            return new CategoryView(category.id().value(), category.name());
        }
    }
}
