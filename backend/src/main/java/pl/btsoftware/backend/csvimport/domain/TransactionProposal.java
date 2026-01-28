package pl.btsoftware.backend.csvimport.domain;

import static java.util.Objects.requireNonNull;
import static pl.btsoftware.backend.shared.validation.NameValidationRules.MAX_NAME_LENGTH;
import static pl.btsoftware.backend.shared.validation.NameValidator.ALL_NON_VALID_CHARACTERS_PATTERN;

import java.math.BigDecimal;
import java.time.LocalDate;
import pl.btsoftware.backend.csvimport.domain.error.TransactionProposalDescriptionInvalidCharactersException;
import pl.btsoftware.backend.csvimport.domain.error.TransactionProposalDescriptionTooLongException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.shared.validation.NameValidationRules;

public record TransactionProposal(
        TransactionProposalId transactionId,
        LocalDate transactionDate,
        String description,
        BigDecimal amount,
        Currency currency,
        TransactionType type,
        CategoryId categoryId) {
    public TransactionProposal {
        requireNonNull(transactionId, "Transaction id cannot be null");
        requireNonNull(transactionDate, "Transaction date cannot be null");
        requireNonNull(amount, "Amount cannot be null");
        requireNonNull(currency, "Currency cannot be null");
        requireNonNull(type, "Transaction type cannot be null");

        description = sanitizeDescription(description);

        NameValidationRules.validate(
                description,
                null,
                TransactionProposalDescriptionTooLongException::new,
                TransactionProposalDescriptionInvalidCharactersException::new);
    }

    private String sanitizeDescription(String description) {
        if (description == null) {
            return "";
        }

        var cleaned = description.replaceAll(ALL_NON_VALID_CHARACTERS_PATTERN.pattern(), "");

        if (cleaned.length() > MAX_NAME_LENGTH) {
            return cleaned.substring(0, MAX_NAME_LENGTH);
        }

        return cleaned;
    }
}
