package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
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
            group.getId().getValue(),
            group.getName(),
            group.getDescription(),
            group.getMemberIds().stream()
                .map(UserId::getValue)
                .collect(Collectors.toSet()),
            group.getCreatedBy().getValue(),
            group.getCreatedAt()
        );
    }

    public Group toDomain() {
        return new Group(
            new GroupId(id),
            name,
            description,
            memberIds.stream()
                .map(UserId::new)
                .collect(Collectors.toSet()),
            new UserId(createdBy),
            createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UUID> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<UUID> memberIds) {
        this.memberIds = memberIds;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}