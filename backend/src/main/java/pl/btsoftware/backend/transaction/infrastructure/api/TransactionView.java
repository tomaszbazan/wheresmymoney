package pl.btsoftware.backend.transaction.infrastructure.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.Bill;
import pl.btsoftware.backend.transaction.domain.Transaction;

public record TransactionView(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String type,
        BillView bill,
        LocalDate transactionDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static TransactionView from(Transaction transaction, Function<CategoryId, Category> categoryMapper) {
        return new TransactionView(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.amount().value(),
                transaction.type().name(),
                BillView.from(transaction.bill(), categoryMapper),
                transaction.transactionDate(),
                transaction.createdAt(),
                transaction.lastUpdatedAt());
    }

    public record BillView(List<BillItemView> items) {
        public BillView {
            items = List.copyOf(items);
        }

        public static BillView from(Bill bill, Function<CategoryId, Category> categoryMapper) {
            return new BillView(bill.items().stream()
                    .map(item -> BillItemView.from(item, categoryMapper))
                    .toList());
        }
    }
}
