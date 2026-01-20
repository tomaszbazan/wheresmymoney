package pl.btsoftware.backend.csvimport.domain;

import pl.btsoftware.backend.shared.CategoryId;

public record CategorySuggestion(TransactionProposalId transactionProposalId, CategoryId categoryId,
                                 double confidence) {
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 1.0;

    public CategorySuggestion {
        if (confidence < MIN_CONFIDENCE || confidence > MAX_CONFIDENCE) {
            throw new IllegalArgumentException("Confidence must be between " + MIN_CONFIDENCE + " and " + MAX_CONFIDENCE);
        }
    }
}
