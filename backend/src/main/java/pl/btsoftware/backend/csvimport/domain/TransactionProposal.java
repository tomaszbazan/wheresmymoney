package pl.btsoftware.backend.csvimport.domain;

import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record TransactionProposal(TransactionProposalId transactionId, LocalDate transactionDate, String description,
                                  BigDecimal amount, Currency currency,
                                  TransactionType type, CategoryId categoryId) {
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    public TransactionProposal {
        requireNonNull(transactionId, "Transaction id cannot be null");
        requireNonNull(transactionDate, "Transaction date cannot be null");
        requireNonNull(amount, "Amount cannot be null");
        requireNonNull(currency, "Currency cannot be null");
        requireNonNull(type, "Transaction type cannot be null");

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }
}
