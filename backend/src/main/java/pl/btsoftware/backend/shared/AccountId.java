package pl.btsoftware.backend.shared;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record AccountId(UUID value) {
    public AccountId {
        requireNonNull(value, "Account id cannot be null");
    }

    public static AccountId generate() {
        return new AccountId(randomUUID());
    }

    public static AccountId from(UUID id) {
        return new AccountId(id);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
