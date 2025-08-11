package pl.btsoftware.backend.transaction.domain;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

public record Tombstone(
        boolean isDeleted,
        OffsetDateTime deletedAt
) {
    public static Tombstone active() {
        return new Tombstone(false, null);
    }

    public static Tombstone deleted() {
        return new Tombstone(true, now(UTC));
    }

    public boolean isActive() {
        return !isDeleted;
    }
}