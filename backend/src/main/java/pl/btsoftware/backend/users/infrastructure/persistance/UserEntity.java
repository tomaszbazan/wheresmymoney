package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "external_auth_id", nullable = false, unique = true)
    private String externalAuthId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt;

    @Column(name = "joined_group_at", nullable = false)
    private Instant joinedGroupAt;

    public UserEntity() {}

    public UserEntity(UUID id, String externalAuthId, String email, String displayName,
                     UUID groupId, Instant createdAt, Instant lastLoginAt, Instant joinedGroupAt) {
        this.id = id;
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.displayName = displayName;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.joinedGroupAt = joinedGroupAt;
    }

    public static UserEntity from(User user) {
        return new UserEntity(
            user.getId().getValue(),
            user.getExternalAuthId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getGroupId().getValue(),
            user.getCreatedAt(),
            user.getLastLoginAt(),
            user.getJoinedGroupAt()
        );
    }

    public User toDomain() {
        return new User(
            new UserId(id),
            externalAuthId,
            email,
            displayName,
            new GroupId(groupId),
            createdAt,
            lastLoginAt,
            joinedGroupAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getJoinedGroupAt() {
        return joinedGroupAt;
    }

    public void setJoinedGroupAt(Instant joinedGroupAt) {
        this.joinedGroupAt = joinedGroupAt;
    }
}