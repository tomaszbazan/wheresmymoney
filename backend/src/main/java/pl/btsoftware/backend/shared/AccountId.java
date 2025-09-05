package pl.btsoftware.backend.shared;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record AccountId(UUID value) {
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId from(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Account id cannot be null");
        }
        return new AccountId(id);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
