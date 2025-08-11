package pl.btsoftware.backend.transaction.infrastructure.api;

import pl.btsoftware.backend.transaction.domain.Transaction;

import java.util.List;

public record TransactionsView(List<TransactionView> transactions) {
    public static TransactionsView from(List<Transaction> transactions) {
        return new TransactionsView(transactions.stream()
                .map(TransactionView::from)
                .toList());
    }
}