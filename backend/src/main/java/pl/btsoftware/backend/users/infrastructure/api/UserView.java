package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.User;

import java.time.Instant;

public class UserView {
    private final String id;
    private final String externalAuthId;
    private final String email;
    private final String displayName;
    private final String groupId;
    private final Instant createdAt;
    private final Instant lastLoginAt;
    private final Instant joinedGroupAt;

    public UserView(String id, String externalAuthId, String email, String displayName,
                   String groupId, Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {
        this.id = id;
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.displayName = displayName;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.joinedGroupAt = joinedGroupAt;
    }

    public static UserView from(User user) {
        return new UserView(
            user.getId().toString(),
            user.getExternalAuthId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getGroupId().toString(),
            user.getCreatedAt(),
            user.getLastLoginAt(),
            user.getJoinedGroupAt()
        );
    }

    public String getId() {
        return id;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroupId() {
        return groupId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getJoinedGroupAt() {
        return joinedGroupAt;
    }
}