package pl.btsoftware.backend.users.domain;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record UserId(String value) {
    public UserId {
        requireNonNull(value, "UserId cannot be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @NotNull @Override
    public String toString() {
        return value;
    }
}
