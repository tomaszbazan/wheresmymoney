package pl.btsoftware.backend.csvimport.domain;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;

import static java.util.Objects.requireNonNull;

public record CategorySuggestion(TransactionId transactionId, CategoryId categoryId, double confidence) {
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 1.0;

    public CategorySuggestion {
        requireNonNull(transactionId, "Transaction ID cannot be null");

        if (confidence < MIN_CONFIDENCE || confidence > MAX_CONFIDENCE) {
            throw new IllegalArgumentException("Confidence must be between " + MIN_CONFIDENCE + " and " + MAX_CONFIDENCE);
        }
    }
}