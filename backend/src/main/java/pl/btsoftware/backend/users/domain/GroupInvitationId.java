package pl.btsoftware.backend.users.domain;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record GroupInvitationId(UUID value) {
    public GroupInvitationId {
        if (value == null) {
            throw new IllegalArgumentException("GroupInvitationId cannot be null");
        }
    }

    public static GroupInvitationId generate() {
        return new GroupInvitationId(UUID.randomUUID());
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }
}
