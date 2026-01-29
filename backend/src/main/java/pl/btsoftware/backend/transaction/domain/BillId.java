package pl.btsoftware.backend.transaction.domain;

import java.util.UUID;

public record BillId(UUID value) {
    public static BillId generate() {
        return new BillId(UUID.randomUUID());
    }

    public static BillId of(UUID value) {
        return new BillId(value);
    }
}
