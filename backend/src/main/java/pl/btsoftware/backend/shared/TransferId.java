package pl.btsoftware.backend.shared;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record TransferId(UUID value) {
    public TransferId {
        requireNonNull(value, "Transfer id cannot be null");
    }

    public static TransferId generate() {
        return new TransferId(randomUUID());
    }

    public static TransferId from(UUID id) {
        return new TransferId(id);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
