package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

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

    public static UserEntity from(User user) {
        return new UserEntity(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.groupId().value(),
                user.createdAt(),
                user.lastLoginAt(),
                user.joinedGroupAt());
    }

    public User toDomain() {
        return new User(
                new UserId(id),
                email,
                displayName,
                new GroupId(groupId),
                createdAt,
                lastLoginAt,
                joinedGroupAt);
    }
}
