package pl.btsoftware.backend.users.infrastructure.api;

import java.time.Instant;
import java.util.UUID;
import pl.btsoftware.backend.users.domain.User;

public record UserView(
        String id,
        String email,
        String displayName,
        UUID groupId,
        Instant createdAt,
        Instant lastLoginAt,
        Instant joinedGroupAt) {

    public static UserView from(User user) {
        return new UserView(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.groupId().value(),
                user.createdAt(),
                user.lastLoginAt(),
                user.joinedGroupAt());
    }
}
