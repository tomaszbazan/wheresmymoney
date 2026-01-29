package pl.btsoftware.backend.transaction.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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
        List<BillItemView> billItems,
        LocalDate transactionDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
    public TransactionView {
        billItems = List.copyOf(billItems);
    }

    public static TransactionView from(
            Transaction transaction, Function<CategoryId, Category> categoryMapper) {
        var billItemViews =
                transaction.bill().items().stream()
                        .map(item -> BillItemView.from(item, categoryMapper))
                        .toList();
        return new TransactionView(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.amount().value(),
                transaction.type().name(),
                billItemViews,
                transaction.transactionDate(),
                transaction.createdAt(),
                transaction.lastUpdatedAt());
    }
}
