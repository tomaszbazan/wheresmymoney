package pl.btsoftware.backend.csvimport.infrastructure.api;

import pl.btsoftware.backend.csvimport.domain.TransactionProposal;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionProposalView(LocalDate transactionDate, String description, BigDecimal amount,
                                      Currency currency, TransactionType type, UUID categoryId) {
    public static TransactionProposalView from(TransactionProposal proposal) {
        return new TransactionProposalView(proposal.transactionDate(), proposal.description(), proposal.amount(), proposal.currency(), proposal.type(), proposal.categoryId() != null ? proposal.categoryId().value() : null);
    }
}
