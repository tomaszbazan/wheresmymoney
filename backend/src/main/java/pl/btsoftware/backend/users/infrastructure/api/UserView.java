package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.User;

import java.time.Instant;

public record UserView(String id, String externalAuthId, String email, String displayName, String groupId,
                       Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {

    public static UserView from(User user) {
        return new UserView(
                user.getId().toString(),
                user.getExternalAuthId().value(),
                user.getEmail(),
                user.getDisplayName(),
                user.getGroupId().toString(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getJoinedGroupAt()
        );
    }
}