package pl.btsoftware.backend.transaction.infrastructure.api;

import org.springframework.data.domain.Page;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.util.List;
import java.util.function.Function;

public record TransactionsPaginatedView(
        List<TransactionView> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static TransactionsPaginatedView from(
            Page<Transaction> transactionPage,
            Function<CategoryId, Category> categoryMapper
    ) {
        var transactions = transactionPage.getContent().stream()
                .map(transaction -> TransactionView.from(transaction, categoryMapper))
                .toList();

        return new TransactionsPaginatedView(
                List.copyOf(transactions),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }
}
