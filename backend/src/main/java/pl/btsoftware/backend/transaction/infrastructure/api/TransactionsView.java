package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.util.List;
import java.util.function.Function;

public record TransactionsView(List<TransactionView> transactions) {
    public TransactionsView(List<TransactionView> transactions) {
        this.transactions = transactions != null ? List.copyOf(transactions) : List.of();
    }

    public List<TransactionView> transactions() {
        return transactions;
    }
    public static TransactionsView from(List<Transaction> transactions, Function<CategoryId, Category> categoryMapper) {
        return new TransactionsView(transactions.stream()
                .map(transaction -> TransactionView.from(transaction, categoryMapper))
                .toList());
    }
}