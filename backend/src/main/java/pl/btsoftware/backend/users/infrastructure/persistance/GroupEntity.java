package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.Getter;
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
    private Set<UUID> memberIds = new HashSet<>();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GroupEntity() {}

    public GroupEntity(UUID id, String name, String description, Set<UUID> memberIds,
                      UUID createdBy, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberIds = new HashSet<>(memberIds);
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static GroupEntity from(Group group) {
        return new GroupEntity(
                group.id().getValue(),
                group.name(),
                group.description(),
                group.memberIds().stream()
                .map(UserId::getValue)
                .collect(Collectors.toSet()),
                group.createdBy().getValue(),
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