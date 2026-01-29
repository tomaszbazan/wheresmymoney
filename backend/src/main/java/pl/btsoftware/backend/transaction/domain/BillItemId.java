package pl.btsoftware.backend.transaction.domain;

import java.util.UUID;

public record BillItemId(UUID value) {
    public static BillItemId generate() {
        return new BillItemId(UUID.randomUUID());
    }

    public static BillItemId of(UUID value) {
        return new BillItemId(value);
    }
}
