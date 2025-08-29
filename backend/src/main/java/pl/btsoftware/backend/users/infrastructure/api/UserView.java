package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserView(String id, String email, String displayName, UUID groupId,
                       Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {

    public static UserView from(User user) {
        return new UserView(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.groupId().value(),
                user.createdAt(),
                user.lastLoginAt(),
                user.joinedGroupAt()
        );
    }
}