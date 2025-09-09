package pl.btsoftware.backend.users.domain;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserId(String value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    @NotNull
    @Override
    public String toString() {
        return value;
    }
}