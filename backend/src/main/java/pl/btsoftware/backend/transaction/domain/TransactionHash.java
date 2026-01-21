package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.transaction.domain.error.TransactionHashInvalidException;

public record TransactionHash(String value) {
    public TransactionHash {
        if (value == null || !value.matches("[a-f0-9]{64}")) {
            throw new TransactionHashInvalidException();
        }
    }
}
