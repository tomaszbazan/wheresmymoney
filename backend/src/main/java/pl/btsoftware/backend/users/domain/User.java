package pl.btsoftware.backend.users.domain;

import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;
import java.util.Objects;

public record User(UserId id, String email, String displayName, GroupId groupId, Instant createdAt, Instant lastLoginAt,
                   Instant joinedGroupAt) {
    public User(UserId id, String email, String displayName,
                GroupId groupId, Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {
        validateEmail(email);
        validateDisplayName(displayName);

        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.email = email.trim();
        this.displayName = displayName.trim();
        this.groupId = Objects.requireNonNull(groupId, "GroupId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.lastLoginAt = Objects.requireNonNull(lastLoginAt, "LastLoginAt cannot be null");
        this.joinedGroupAt = Objects.requireNonNull(joinedGroupAt, "JoinedGroupAt cannot be null");
    }

    public static User create(UserId id, String email, String displayName, GroupId groupId) {
        Instant now = Instant.now();
        return new User(
                id,
                email,
                displayName,
                groupId,
                now,
                now,
                now
        );
    }

    public User changeGroup(GroupId newGroupId) {
        return new User(id, email, displayName, newGroupId, createdAt, lastLoginAt, Instant.now());
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new UserEmailEmptyException();
        }
    }

    private void validateDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new DisplayNameEmptyException();
        }
    }
}
