package pl.btsoftware.backend.audit.domain;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record EntityId(UUID value) {
    public EntityId {
        requireNonNull(value, "EntityId cannot be null");
    }

    public static EntityId from(UUID value) {
        return new EntityId(value);
    }

    @NotNull @Override
    public String toString() {
        return value.toString();
    }
}
