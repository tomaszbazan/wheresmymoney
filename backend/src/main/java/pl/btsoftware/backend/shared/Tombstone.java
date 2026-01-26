package pl.btsoftware.backend.shared;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

import java.time.OffsetDateTime;

public record Tombstone(boolean isDeleted, OffsetDateTime deletedAt) {
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
