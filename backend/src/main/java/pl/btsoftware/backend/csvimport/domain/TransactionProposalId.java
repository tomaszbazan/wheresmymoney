package pl.btsoftware.backend.csvimport.domain;

import java.util.UUID;

public record TransactionProposalId(UUID value) {
    public static TransactionProposalId generate() {
        return new TransactionProposalId(UUID.randomUUID());
    }

    public static TransactionProposalId from(UUID transactionId) {
        return new TransactionProposalId(transactionId);
    }
}
