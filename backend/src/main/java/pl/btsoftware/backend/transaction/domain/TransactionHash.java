package pl.btsoftware.backend.transaction.domain;

public record TransactionHash(String value) {
    public TransactionHash {
        if (value == null || !value.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException("Invalid transaction hash");
        }
    }
}
