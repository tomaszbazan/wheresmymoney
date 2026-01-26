package pl.btsoftware.backend.audit.domain;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record AuditLogId(UUID value) {
    public AuditLogId {
        requireNonNull(value, "AuditLogId cannot be null");
    }

    public static AuditLogId generate() {
        return new AuditLogId(randomUUID());
    }

    public static AuditLogId of(UUID value) {
        return new AuditLogId(value);
    }

    @NotNull @Override
    public String toString() {
        return value.toString();
    }
}
