package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.btsoftware.backend.users.domain.Group;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "user_id")
    private Set<String> memberIds = new HashSet<>();

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static GroupEntity from(Group group) {
        return new GroupEntity(
                group.id().value(),
                group.name(),
                group.description(),
                group.memberIds().stream()
                        .map(UserId::value)
                .collect(Collectors.toSet()),
                group.createdBy().value(),
                group.createdAt()
        );
    }

    public Group toDomain() {
        Set<UserId> domainMemberIds = memberIds.stream()
                .map(UserId::new)
                .collect(Collectors.toSet());

        if (domainMemberIds.isEmpty()) {
            return Group.createEmptyWithId(new GroupId(id), name, description, new UserId(createdBy), createdAt);
        } else {
            return new Group(
                    new GroupId(id),
                    name,
                    description,
                    domainMemberIds,
                    new UserId(createdBy),
                    createdAt
            );
        }
    }
}
