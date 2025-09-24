package pl.btsoftware.backend.shared;

import java.util.UUID;

public record CategoryId(UUID value) {
    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    public static CategoryId of(UUID uuid) {
        return new CategoryId(uuid);
    }
}
