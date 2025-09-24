package pl.btsoftware.backend.users.domain;

import java.util.UUID;

public record GroupId(UUID value) {
    public GroupId {
        if (value == null) {
            throw new IllegalArgumentException("GroupId cannot be null");
        }
    }

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }
}
