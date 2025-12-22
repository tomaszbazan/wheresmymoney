package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class TransactionHashCalculator {

    public TransactionHash calculateHash(
            AccountId accountId,
            Money amount,
            String description,
            LocalDate transactionDate,
            TransactionType type
    ) {
        String normalizedDescription = description != null ? description.trim().toLowerCase() : "";
        String concatenated = accountId.value().toString()
                              + amount.value().toString()
                              + amount.currency().name()
                              + normalizedDescription
                              + transactionDate.toString()
                              + type.name();

        return new TransactionHash(sha256(concatenated));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
