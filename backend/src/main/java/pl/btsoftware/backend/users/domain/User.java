package pl.btsoftware.backend.users.domain;

import lombok.Getter;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;
import java.util.Objects;

@Getter
public class User {
    private final UserId id;
    private final ExternalAuthId externalAuthId;
    private final String email;
    private final String displayName;
    private final Instant createdAt;
    private final Instant lastLoginAt;
    private GroupId groupId;
    private Instant joinedGroupAt;

    public User(UserId id, ExternalAuthId externalAuthId, String email, String displayName,
                GroupId groupId, Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {
        validateEmail(email);
        validateDisplayName(displayName);
        
        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.externalAuthId = Objects.requireNonNull(externalAuthId, "External auth ID cannot be null");
        Objects.requireNonNull(externalAuthId.value(), "External auth ID value cannot be null");
        this.email = email.trim();
        this.displayName = displayName.trim();
        this.groupId = Objects.requireNonNull(groupId, "GroupId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.lastLoginAt = Objects.requireNonNull(lastLoginAt, "LastLoginAt cannot be null");
        this.joinedGroupAt = Objects.requireNonNull(joinedGroupAt, "JoinedGroupAt cannot be null");
    }

    public static User create(ExternalAuthId externalAuthId, String email, String displayName, GroupId groupId) {
        Instant now = Instant.now();
        return new User(
            UserId.generate(),
            externalAuthId,
            email,
            displayName,
            groupId,
            now,
            now,
            now
        );
    }

    public void changeGroup(GroupId newGroupId) {
        this.groupId = Objects.requireNonNull(newGroupId, "New GroupId cannot be null");
        this.joinedGroupAt = Instant.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}