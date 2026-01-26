package pl.btsoftware.backend.csvimport.domain;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import pl.btsoftware.backend.csvimport.domain.error.TransactionProposalDescriptionTooLongException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

public record TransactionProposal(
        TransactionProposalId transactionId,
        LocalDate transactionDate,
        String description,
        BigDecimal amount,
        Currency currency,
        TransactionType type,
        CategoryId categoryId) {
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    public TransactionProposal {
        requireNonNull(transactionId, "Transaction id cannot be null");
        requireNonNull(transactionDate, "Transaction date cannot be null");
        requireNonNull(amount, "Amount cannot be null");
        requireNonNull(currency, "Currency cannot be null");
        requireNonNull(type, "Transaction type cannot be null");

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new TransactionProposalDescriptionTooLongException(MAX_DESCRIPTION_LENGTH);
        }
    }
}
